package com.example.schedule3;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

class ScheduleApp extends Application implements SharedPreferences
        .OnSharedPreferenceChangeListener{

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {

    }
}
