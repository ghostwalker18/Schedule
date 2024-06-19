package com.example.schedule3;

import android.app.DownloadManager;
import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
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

public class MainActivity extends AppCompatActivity {
    private ViewPager pager;
    private DaysFragment daysFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
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

    private boolean shareSchedule(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        String schedule = "";
        for(ScheduleItemFragment day : daysFragment.getDays()){
            if(day.isOpened()){
                schedule += day.getSchedule();
            }
        }
        intent.putExtra(Intent.EXTRA_TEXT, schedule);
        Intent shareIntent = Intent.createChooser(intent, null);
        startActivity(shareIntent);
        return true;
    }

    private  boolean shareTimes(){
        File mondayTimes = new File(getApplication().getFilesDir(), "mondayTimes.jpg");
        File otherTimes = new File(getApplication().getFilesDir(), "otherTimes.jpg");

        if(mondayTimes.exists() && otherTimes.exists()){
            ArrayList<Uri> imageUris = new ArrayList<Uri>();

            Uri mondayTimesURI = FileProvider.getUriForFile(this, "com.example.schedule3.timefilesprovider", mondayTimes);
            imageUris.add(mondayTimesURI);

            Uri otherTimesURI = FileProvider.getUriForFile(this, "com.example.schedule3.timefilesprovider", otherTimes);
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

    private boolean downloadScheduleFile(){
        new Thread(() -> {
            List<String> links = ScheduleApp.getInstance().getRepository().getLinksForSchedule();
            DownloadManager downloadManager = getApplication().getSystemService(DownloadManager.class);
            for(String link : links){
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(link))
                        .setMimeType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setTitle(getString(R.string.app_name))
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, getString(R.string.app_name));
                downloadManager.enqueue(request);
            }
        }).start();
        return true;
    };

    private boolean downloadTimesFiles(){
        String[] links = new String[]{ScheduleApp.mondayTimesURL, ScheduleApp.otherTimesURL};
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

        public Fragment getItem(int position){
            switch(position){
                case 0:
                    return daysFragment;
                case 1:
                    return timesFragment;
            }
            return null;
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