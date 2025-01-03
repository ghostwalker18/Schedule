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
 * @version  3.1
 */
public class ScheduleApp
        extends Application
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static ScheduleApp instance;
    private SharedPreferences preferences;
    private AppDatabase database;
    private ScheduleRepository scheduleRepository;
    private NotesRepository notesRepository;

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
        scheduleRepository = new ScheduleRepository(this, database,
                new NetworkService(this, ScheduleRepository.BASE_URI, preferences));
        scheduleRepository.update();
        notesRepository = new NotesRepository(database);
        String theme = preferences.getString("theme", "");
        setTheme(theme);
        preferences.registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * Этот метод позволяет получить доступ к экзэмпляру приложения
     * @return приложение
     */
    public static ScheduleApp getInstance(){
        return instance;
    }

    /**
     * Этот метод позволяет получить репозиторий заметок приложения.
     * @return репозиторий заметок
     */
    public NotesRepository getNotesRepository(){
        return notesRepository;
    }

    /**
     * Этот метод позволяет получить репозиторий расписания приложения.
     * @return репозиторий расписания
     */
    public ScheduleRepository getScheduleRepository(){
        return scheduleRepository;
    }

    /**
     * Этот метод позволяет установить тему приложения
     * @param theme код темы (system, day, night)
     */
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

    /**
     * Этот метод позволяет установить язык приложения
     * @param localeCode код языка
     */
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