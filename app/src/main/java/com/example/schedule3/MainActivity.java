package com.example.schedule3;

import android.content.Context;
import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private ViewPager pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(new SectionsPagerAdapter(getSupportFragmentManager()));
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
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
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, "schedule_stub");
                    Intent shareIntent = Intent.createChooser(intent, null);
                    startActivity(shareIntent);
                    return true;
                case 1:
                    ArrayList<Uri> imageUris = new ArrayList<Uri>();
                    File mondayTimes = new File(getApplication().getFilesDir(), "mondayTimes.jpg");
                    Uri mondayTimesURI = FileProvider.getUriForFile(this, "com.example.schedule3.timefilesprovider", mondayTimes);
                    imageUris.add(mondayTimesURI);
                    File otherTimes = new File(getApplication().getFilesDir(), "otherTimes.jpg");
                    Uri otherTimesURI = FileProvider.getUriForFile(this, "com.example.schedule3.timefilesprovider", otherTimes);
                    imageUris.add(otherTimesURI);

                    Intent shareIntent2 = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    shareIntent2.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
                    shareIntent2.setType("image/*");
                    startActivity(Intent.createChooser(shareIntent2, null));
                    return true;
                default:
                    return true;
            }
        }
        return super.onOptionsItemSelected(item);
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