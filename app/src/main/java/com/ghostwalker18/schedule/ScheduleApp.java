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

import android.app.Application;
import android.content.SharedPreferences;

import com.google.android.material.color.DynamicColors;

import java.util.Locale;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;

/**
 * <h1>Schedule</h1>
 * <p>
 *      Программа представляет собой мобильную реализацию приложения расписания ПАСТ.
 * </p>
 *
 * @author  Ипатов Никита
 * @version  1.3
 */
public class ScheduleApp
        extends Application
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String BASE_URI = "https://ptgh.onego.ru/9006/";
    public static final String MONDAY_TIMES_URL =
            "https://r1.nubex.ru/s1748-17b/47698615b7_fit-in~1280x800~filters:no_upscale()__f44488_08.jpg";
    public static final String OTHER_TIMES_URL =
            "https://r1.nubex.ru/s1748-17b/320e9d2d69_fit-in~1280x800~filters:no_upscale()__f44489_bb.jpg";
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
        DynamicColors.applyToActivitiesIfAvailable(this);
        instance = this;
        database = AppDatabase.getInstance(this);
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        repository = new ScheduleRepository(this, database, new NetworkService(this, BASE_URI));
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