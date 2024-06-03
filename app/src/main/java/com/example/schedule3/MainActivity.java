package com.example.schedule3;

import android.content.Intent;
import com.google.android.material.tabs.TabLayout;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.appcompat.widget.ShareActionProvider;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SectionIndexer;

import androidx.viewpager.widget.ViewPager;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private SectionsPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        pagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        ViewPager pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        TabLayout tabLayout = (TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(pager);
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
