package com.example.schedule3;

import android.app.Application;
import android.content.SharedPreferences;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

public class ScheduleApp extends Application implements SharedPreferences
        .OnSharedPreferenceChangeListener{
    public static final String mondayTimesURL = "https://r1.nubex.ru/s1748-17b/47698615b7_fit-in~1280x800~filters:no_upscale()__f44488_08.jpg";
    public static final String otherTimesURL = "https://r1.nubex.ru/s1748-17b/320e9d2d69_fit-in~1280x800~filters:no_upscale()__f44489_bb.jpg";
    private static ScheduleApp instance;
    private SharedPreferences preferences;
    private AppDatabase database;
    private ScheduleRepository repository;
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        switch (key){
            case "theme":
                String theme = sharedPreferences.getString(key, "");
                setTheme(theme);
                break;
            case "language":
                String localeCode = sharedPreferences.getString(key, "en");
                setLocale(localeCode);
                break;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        database = Room.databaseBuilder(this, AppDatabase.class, "database")
                .createFromAsset("testDB1")
                .build();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        repository = new ScheduleRepository(this);
        repository.update();
        String theme = preferences.getString("theme", "");
        setTheme(theme);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    public static ScheduleApp getInstance(){
        return instance;
    }

    public AppDatabase getDatabase(){
        return database;
    }

    public ScheduleRepository getRepository(){
        return repository;
    }

    private void setTheme(String theme){
        switch (theme){
            case "system":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                break;
            case "night":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "day":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    private void setLocale(String localeCode){
        LocaleListCompat localeListCompat;
        if(localeCode.equals("system")){
            localeListCompat = LocaleListCompat.getEmptyLocaleList();
        }
        else
            localeListCompat = LocaleListCompat.create(new Locale(localeCode));
        AppCompatDelegate.setApplicationLocales(localeListCompat);
    }
}