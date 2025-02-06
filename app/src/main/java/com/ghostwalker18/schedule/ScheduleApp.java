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
import android.util.Log;
import com.ghostwalker18.schedule.database.AppDatabase;
import com.ghostwalker18.schedule.models.NotesRepository;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import com.ghostwalker18.schedule.network.NetworkService;
import com.ghostwalker18.schedule.notifications.NotificationManagerWrapper;
import com.ghostwalker18.schedule.notifications.ScheduleUpdateNotificationWorker;
import com.ghostwalker18.schedule.utils.AndroidUtils;
import com.google.android.material.color.DynamicColors;
import com.google.firebase.FirebaseApp;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;
import androidx.preference.PreferenceManager;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.AppMetricaConfig;
import ru.rustore.sdk.pushclient.common.logger.DefaultLogger;
import ru.rustore.sdk.universalpush.RuStoreUniversalPushClient;
import ru.rustore.sdk.universalpush.firebase.provides.FirebasePushProvider;
import ru.rustore.sdk.universalpush.rustore.providers.RuStorePushProvider;

/**
 * <h1>Schedule</h1>
 * <p>
 *      Программа представляет собой мобильную реализацию приложения расписания ПАСТ.
 * </p>
 *
 * @author  Ипатов Никита
 * @version  4.1
 */
public class ScheduleApp
        extends Application
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    public static final String DEVELOPER_EMAIL = "ghostwalker18@mail.ru";
    private static ScheduleApp instance;
    private boolean isAppMetricaActivated = false;
    private SharedPreferences preferences;
    private AppDatabase database;
    private ScheduleRepository scheduleRepository;
    private NotesRepository notesRepository;
    private final RuStoreUniversalPushClient pushClient = RuStoreUniversalPushClient.INSTANCE;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
        boolean enabled;
        switch (key){
            case "theme":
                String theme = sharedPreferences.getString(key, "");
                setTheme(theme);
                break;
            case "language":
                String localeCode = sharedPreferences.getString(key, "en");
                setLocale(localeCode);
                break;
            case "update_notifications":
                enabled = sharedPreferences.getBoolean("update_notifications", false);
                if(enabled){
                    pushClient.subscribeToTopic("update_notifications");
                }
                else
                    pushClient.unsubscribeFromTopic("update_notifications");
                break;
            case "schedule_notifications":
                enabled = sharedPreferences.getBoolean("schedule_notifications", false);
                if(enabled){
                    Constraints constraints = new Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.UNMETERED)
                            .build();
                    PeriodicWorkRequest request = new PeriodicWorkRequest
                            .Builder(ScheduleUpdateNotificationWorker.class, 30, TimeUnit.MINUTES)
                            .addTag("update_schedule")
                            .setConstraints(constraints)
                            .build();
                    WorkManager.getInstance(this).enqueue(request);
                }
                else{
                    WorkManager.getInstance(this).cancelAllWorkByTag("update_schedule");
                }
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
        //scheduleRepository.update();
        notesRepository = new NotesRepository(database);
        String theme = preferences.getString("theme", "");
        setTheme(theme);
        preferences.registerOnSharedPreferenceChangeListener(this);

        //Initializing of third-party analytics and push services.
        try{
            String appMetricaApiKey = getString(R.string.app_metrica_api_key); //from non-public strings
            AppMetricaConfig config = AppMetricaConfig.newConfigBuilder(appMetricaApiKey).build();
            // Initializing the AppMetrica SDK.
            AppMetrica.activate(this, config);
            isAppMetricaActivated = true;
            FirebaseApp.initializeApp(this);
            // Initializing the RuStore Push SDK.
            initPushes();
        } catch(Exception e){/*Not required*/}
        //Change app settings in order to be same as system settings
        AndroidUtils.checkNotificationsPermissions(this, preferences);
        AndroidUtils.clearPOICache(this);
    }

    /**
     * Этот метод позволяет получить доступ к экзэмпляру приложения.
     * @return приложение
     */
    public static ScheduleApp getInstance(){
        return instance;
    }

    /**
     * Этот метод позволяет узнать активирована ли AppMetrica.
     */
    public boolean isAppMetricaActivated(){
        return isAppMetricaActivated;
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
     * Этот метод используется для получения БД приложения.
     * @return синглтон БД
     */
    public AppDatabase getDatabase(){
        return database;
    }

    /**
     * Этот метод используется для инициализации доставки Push-уведомлений RuStore и Firebase.
     */
    private void initPushes() {
        pushClient.init(
                this,
                new RuStorePushProvider(this, getString(R.string.rustore_api_key), //from non-public strings
                        new DefaultLogger()),
                new FirebasePushProvider(this),
                null
        );
        pushClient.getTokens()
                .addOnSuccessListener(result -> Log.w(
                        "AppPushes", "getToken onSuccess = " + result))
                .addOnFailureListener(throwable -> Log.e(
                        "AppPushes", "getToken onFailure", throwable));

        //Do not forget to add same calls in NotificationLocaleUpdater for locale changes updates
        NotificationManagerWrapper.getInstance(this).createNotificationChannel(
                getString(R.string.notifications_notification_app_update_channel_id),
                getString(R.string.notifications_notification_app_update_channel_name),
                getString(R.string.notifications_notification_app_update_channel_descr)
        );
        NotificationManagerWrapper.getInstance(this).createNotificationChannel(
                getString(R.string.notifications_notification_schedule_update_channel_id),
                getString(R.string.notifications_notification_schedule_update_channel_name),
                getString(R.string.notifications_notification_schedule_update_channel_descr)
        );
        if(preferences.getBoolean("update_notifications", false))
            pushClient.subscribeToTopic("update_notificatons");
        if(preferences.getBoolean("schedule_notifications", false))
            pushClient.subscribeToTopic("schedule_notifications");
    }

    /**
     * Этот метод позволяет установить тему приложения
     * @param theme код темы (system, day, night)
     */
    private void setTheme(@NonNull String theme){
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
    private void setLocale(@NonNull String localeCode){
        LocaleListCompat localeListCompat;
        if(localeCode.equals("system"))
            localeListCompat = LocaleListCompat.getEmptyLocaleList();
        else
            localeListCompat = LocaleListCompat.create(new Locale(localeCode));
        AppCompatDelegate.setApplicationLocales(localeListCompat);
    }
}