package com.example.schedule3;

import android.app.Application;
import android.content.SharedPreferences;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;
import androidx.room.Room;

public class ScheduleApp extends Application implements SharedPreferences
        .OnSharedPreferenceChangeListener{
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
}