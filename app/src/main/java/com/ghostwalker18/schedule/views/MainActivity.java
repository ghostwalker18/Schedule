/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ghostwalker18.schedule.views;

import android.content.Intent;

import com.ghostwalker18.schedule.DownloadDialog;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.ScheduleRepository;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.FileProvider;
import androidx.lifecycle.Lifecycle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

/**
 * Этот класс представляет собой основной экран приложения.
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see DaysFragment
 * @see TimesFragment
 */
public class MainActivity
        extends AppCompatActivity {
    private ViewPager2 pager;
    private DaysFragment daysFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pager = findViewById(R.id.pager);
        pager.setAdapter(new SectionPagerAdapter(getSupportFragmentManager(), getLifecycle()));
        TabLayout tabLayout = findViewById(R.id.tabs);
        new TabLayoutMediator(tabLayout, pager, (tab, position) -> {
            switch(position){
                case 0:
                    tab.setText(getResources().getText(R.string.days_tab));
                    break;
                case 1:
                    tab.setText(getResources().getText(R.string.times_tab));
                    break;
            }
        }).attach();
        if(savedInstanceState == null){
            daysFragment = (DaysFragment)((SectionPagerAdapter)pager
                    .getAdapter()).createFragment(0);
        }
        else{
            for(Fragment fragment : getSupportFragmentManager().getFragments()){
                if(fragment instanceof DaysFragment)
                    daysFragment = (DaysFragment)fragment;
            }
        }
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_main_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if(item.getItemId() == R.id.action_share){
            switch (pager.getCurrentItem()){
                case 0:
                    return shareSchedule();
                case 1:
                    return shareTimes();
            }
        }
        if(item.getItemId() == R.id.action_download){
            switch (pager.getCurrentItem()){
                case 0:
                    return downloadScheduleFile();
                case 1:
                    return downloadTimesFiles();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Этот метод используется для того, чтобы поделиться расписанием из открытых элементов
     * в доступных приложениях.
     */
    private boolean shareSchedule(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        StringBuilder schedule = new StringBuilder();
        for(ScheduleItemFragment day : daysFragment.getDays()){
            if(day.isOpened()){
                schedule.append(day.getSchedule());
            }
        }
        if(schedule.toString().equals("")){
            Toast.makeText(this, R.string.nothing_to_share, Toast.LENGTH_SHORT).show();
            return true;
        }
        intent.putExtra(Intent.EXTRA_TEXT, schedule.toString());
        Intent shareIntent = Intent.createChooser(intent, null);
        startActivity(shareIntent);
        return true;
    }

    /**
     * Этот метод используется для того, чтобы поделиться файлами расписания
     * звонков в доступных приложениях.
     */
    private  boolean shareTimes(){
        File mondayTimes = new File(getApplication().getFilesDir(), "mondayTimes.jpg");
        File otherTimes = new File(getApplication().getFilesDir(), "otherTimes.jpg");

        if(mondayTimes.exists() && otherTimes.exists()){
            ArrayList<Uri> imageUris = new ArrayList<>();

            Uri mondayTimesURI = FileProvider.getUriForFile(this,
                    "com.ghostwalker18.schedule.timefilesprovider", mondayTimes);
            imageUris.add(mondayTimesURI);

            Uri otherTimesURI = FileProvider.getUriForFile(this,
                    "com.ghostwalker18.schedule.timefilesprovider", otherTimes);
            imageUris.add(otherTimesURI);

            Intent shareIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
            shareIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            shareIntent.setType("image/*");
            startActivity(Intent.createChooser(shareIntent, null));
        }
        else{
            Toast toast = Toast.makeText(this, R.string.nothing_to_share, Toast.LENGTH_SHORT);
            toast.show();
        }
        return true;
    }

    /**
     * Этот метод используется для скачивания файлов расписания и помещения их в
     * папку загрузок.
     */
    private boolean downloadScheduleFile(){
        new Thread(() -> {
            List<String> links = new ArrayList<>();
            String downloadFor = PreferenceManager
                    .getDefaultSharedPreferences(this)
                    .getString("downloadFor", "all");
            if(downloadFor.equals("all") || downloadFor.equals("first")){
                List<String> linksForFirstCorpusSchedule = ScheduleApp.getInstance()
                        .getScheduleRepository()
                        .getLinksForFirstCorpusSchedule();
                links.addAll(linksForFirstCorpusSchedule);
            }
            if(downloadFor.equals("all") || downloadFor.equals("second")){
                List<String> linksForSecondCorpusSchedule = ScheduleApp.getInstance()
                        .getScheduleRepository()
                        .getLinksForSecondCorpusSchedule();
                links.addAll(linksForSecondCorpusSchedule);
            }

            DownloadDialog downloadDialog = new DownloadDialog();
            Bundle args = new Bundle();
            args.putInt("number_of_files", links.size());
            args.putStringArray("links", links.toArray(new String[0]));
            String mimeType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            args.putString("mime_type", mimeType);
            String downloadTitle = getString(R.string.days_tab);
            args.putString("download_title", downloadTitle);
            downloadDialog.setArguments(args);
            downloadDialog.show(getSupportFragmentManager(), "download");
        }).start();
        return true;
    }

    /**
     * Этот метод используется для скачивания файлов расписания звонков и помещения
     * их в папку загрузок.
     */
    private boolean downloadTimesFiles(){
        String[] links = new String[]{ScheduleRepository.MONDAY_TIMES_URL,
                ScheduleRepository.OTHER_TIMES_URL};
        DownloadDialog downloadDialog = new DownloadDialog();
        Bundle args = new Bundle();
        args.putInt("number_of_files", links.length);
        args.putStringArray("links", links);
        String mimeType = "image/jpg";
        args.putString("mime_type", mimeType);
        String downloadTitle = getString(R.string.times_tab);
        args.putString("download_title", downloadTitle);
        downloadDialog.setArguments(args);
        downloadDialog.show(getSupportFragmentManager(), "download");
        return true;
    }

    /**
     * Этот класс используется для реализации вкладок расписаний.
     *
     * @author Ипатов Никита
     * @since 3.2
     */
    public static class SectionPagerAdapter
            extends FragmentStateAdapter{
        DaysFragment daysFragment = new DaysFragment();
        TimesFragment timesFragment = new TimesFragment();
        public SectionPagerAdapter(@NonNull FragmentManager fragmentManager,
                                   @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position){
                case 0: return daysFragment;
                case 1: return timesFragment;
                default: return new Fragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}