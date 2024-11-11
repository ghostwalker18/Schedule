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

package com.ghostwalker18.schedule;

import android.app.DownloadManager;
import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * Этот класс представляет собой основной экран приложения.
 *
 * @author  Ипатов Никита
 */
public class MainActivity
        extends AppCompatActivity {
    private ViewPager pager;
    private DaysFragment daysFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pager = findViewById(R.id.pager);
        pager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
        if(savedInstanceState == null){
            daysFragment = (DaysFragment)((SectionsPagerAdapter)pager.getAdapter()).getItem(0);
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
        String schedule = "";
        for(ScheduleItemFragment day : daysFragment.getDays()){
            if(day.isOpened()){
                schedule += day.getSchedule();
            }
        }
        if(schedule.equals("")){
            Toast.makeText(this, R.string.nothing_to_share, Toast.LENGTH_SHORT).show();
            return true;
        }
        intent.putExtra(Intent.EXTRA_TEXT, schedule);
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
            List<String> linksForFirstCorpusSchedule = ScheduleApp.getInstance()
                    .getScheduleRepository()
                    .getLinksForFirstCorpusSchedule();
            List<String> linksForSecondCorpusSchedule = ScheduleApp.getInstance()
                    .getScheduleRepository()
                    .getLinksForSecondCorpusSchedule();
            List<String> links = new ArrayList<>();
            links.addAll(linksForFirstCorpusSchedule);
            links.addAll(linksForSecondCorpusSchedule);
            DownloadManager downloadManager = getApplication().getSystemService(DownloadManager.class);
            for(String link : links){
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link))
                        .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle(getString(R.string.schedule))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                ScheduleRepository.getNameFromLink(link));
                downloadManager.enqueue(request);
            }
        }).start();
        return true;
    }

    /**
     * Этот метод используется для скачивания файлов расписания звонков и помещения
     * их в папку загрузок.
     */
    private boolean downloadTimesFiles(){
        String[] links = new String[]{ScheduleApp.MONDAY_TIMES_URL, ScheduleApp.OTHER_TIMES_URL};
        new Thread(() -> {
            DownloadManager downloadManager = getApplication().getSystemService(DownloadManager.class);
            for(String link : links){
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link))
                        .setMimeType("image/jpg")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle(getString(R.string.times_tab))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.times_tab));
                downloadManager.enqueue(request);
            }
        }).start();
        return true;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {
        private final DaysFragment daysFragment;
        private final TimesFragment timesFragment;
        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
            daysFragment = new DaysFragment();
            timesFragment = new TimesFragment();
        }

        @Override
        public int getCount(){
            return 2;
        }

        @NonNull
        public Fragment getItem(int position){
            switch(position){
                case 0:
                    return daysFragment;
                case 1:
                    return timesFragment;
                default:
                    return new Fragment();
            }
        }

        @Override
        public CharSequence getPageTitle(int position){
            switch(position){
                case 0:
                    return getResources().getText(R.string.days_tab);
                case 1:
                    return getResources().getText(R.string.times_tab);
            }
            return null;
        }
    }
}