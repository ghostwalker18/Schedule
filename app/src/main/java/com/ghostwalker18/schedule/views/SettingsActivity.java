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

package com.ghostwalker18.schedule.views;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.notifications.NotificationManagerWrapper;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.Objects;

/**
 * Этот класс представляет собой экран настроек приложения
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see ScheduleApp
 */
public class SettingsActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar myToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        findViewById(R.id.share_app).setOnClickListener(v -> startActivity(
                new Intent(this, ShareAppActivity.class)));
        findViewById(R.id.data_transfer).setOnClickListener(v -> startActivity(
                new Intent(this, ImportActivity.class)));
        findViewById(R.id.copyright).setOnLongClickListener(this::sendEmailToDeveloper);
    }

    /**
     * Этот метод используется, чтобы связаться с разработчиком приложения.
     */
    private boolean sendEmailToDeveloper(View view){
        try{
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:")); // only email apps should handle this
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{ScheduleApp.DEVELOPER_EMAIL});
            intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.email_subject));
            startActivity(Intent.createChooser(intent, getString(R.string.connect_to_developer)));
        } catch (ActivityNotFoundException e){
            Toast toast = new Toast(this);
            toast.setText(R.string.no_email_client_found);
            toast.show();
        } catch (Exception ignored){/*Not required*/}
        return true;
    }

    public static class SettingsFragment
            extends PreferenceFragmentCompat
            implements SharedPreferences.OnSharedPreferenceChangeListener{
        private SharedPreferences preferences;
        private final ActivityResultLauncher<String> notificationPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                granted -> {
                    if(!granted){
                        preferences.edit()
                                .putBoolean("schedule_notifications", false)
                                .putBoolean("update_notifications", false)
                                .apply();
                    }
                }
        );

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            preferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.preferences, rootKey);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String key) {
            if(Objects.equals(key, "schedule_notifications")
                    || Objects.equals(key, "update_notifications")){
                if (ActivityCompat.checkSelfPermission(
                        getContext(),
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                    if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                        Toast toast = Toast.makeText(getActivity(),
                                getResources().getText(R.string.notifications_permission_reqired),
                                Toast.LENGTH_SHORT);
                        toast.show();
                    } else {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                    }
                } else{
                    if(Objects.equals(key, "schedule_notifications")
                            && preferences.getBoolean("schedule_notifications", false)
                            && !NotificationManagerWrapper.getInstance(getContext())
                            .isNotificationChannelEnabled(
                                    getString(
                                            //from non public strings
                                            R.string.notifications_notification_schedule_update_channel_id
                                    )
                            )){
                        Toast toast = Toast.makeText(getActivity(),
                                String.format(
                                        (String) getResources()
                                                .getText(R.string.notifications_channnel_enable_required),
                                        getResources()
                                                .getText(R.string.notifications_notification_schedule_update_channel_name)
                                ),
                                Toast.LENGTH_SHORT);
                        toast.show();
                        preferences.edit()
                                .putBoolean("schedule_notifications", false)
                                .apply();}
                    if(Objects.equals(key, "update_notifications")
                            && preferences.getBoolean("update_notifications", false)
                            && !NotificationManagerWrapper.getInstance(getContext())
                            .isNotificationChannelEnabled(
                                    getString(
                                            //from non public strings
                                            R.string.notifications_notification_app_update_channel_id
                                    )
                            )){
                        Toast toast = Toast.makeText(getActivity(),
                                String.format(
                                        (String) getResources()
                                                .getText(R.string.notifications_channnel_enable_required),
                                        getResources()
                                                .getText(R.string.notifications_notification_app_update_channel_name)
                                ),
                                Toast.LENGTH_SHORT);
                        toast.show();
                        preferences.edit()
                                .putBoolean("update_notifications", false)
                                .apply();
                    }
                }
            }
        }
    }
}