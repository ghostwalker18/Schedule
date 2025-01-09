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

import android.app.UiModeManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import com.ghostwalker18.schedule.ScheduleWidget;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Этот класс представляет собой экран настроек виджета приложения
 *
 * @author  Ипатов Никита
 * @since 2.3
 * @see ScheduleWidget
 */
public class WidgetSettingsActivity
        extends AppCompatActivity
        implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
   private SettingsFragment fragment;
   private SharedPreferences preferences;
   private ImageView preview;
   private int widgetID = AppWidgetManager.INVALID_APPWIDGET_ID;
   private Intent resultValue;

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_widget_settings);
      Button addButton = findViewById(R.id.add);
      addButton.setOnClickListener(this);

      Intent intent = getIntent();
      Bundle extras = intent.getExtras();
      if (extras != null) {
         widgetID = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                 AppWidgetManager.INVALID_APPWIDGET_ID);
      }
      if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID) {
         finish();
      }

      fragment = new SettingsFragment(widgetID);
      if (savedInstanceState == null) {
         getSupportFragmentManager()
                 .beginTransaction()
                 .replace(R.id.settings, fragment)
                 .commit();
      }
      preferences = getSharedPreferences("WIDGET_" + widgetID, Context.MODE_PRIVATE);
      preferences.registerOnSharedPreferenceChangeListener(this);

      preview = findViewById(R.id.widget_preview);

      resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      setResult(RESULT_CANCELED, resultValue);
   }

   @Override
   public void onClick(View view) {
      setResult(RESULT_OK, resultValue);
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
      preferences.edit()
              .putBoolean("isEdited", true)
              .commit();
      ScheduleWidget.updateAppWidget(this, appWidgetManager, widgetID);
      finish();
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      String previewName= "widget";

      boolean isDynamicColors = preferences.getBoolean("dynamic_colors", false);
      if(isDynamicColors)
         previewName += "_dynamic";

      String theme = preferences.getString("theme", "system");
      switch (theme){
         case "night":
            previewName += "_dark";
            break;
         case "day":
            previewName += "_light";
            break;
         case "system":
            UiModeManager uiModeManager = (UiModeManager)getSystemService(Context.UI_MODE_SERVICE);
            int currentNightMode = uiModeManager.getNightMode();
            switch (currentNightMode) {
               case UiModeManager.MODE_NIGHT_YES:
                  previewName += "_dark";
                  break;
               default:
                  previewName += "_light";
                  break;
            }
            break;
      }

      String day = preferences.getString("day", "today");
      switch (day){
         case "today":
            previewName += "_today";
            break;
         case "tomorrow":
            previewName += "_tomorrow";
            break;
      }

      int imageId = getResources().getIdentifier(previewName, "drawable", getPackageName());
      preview.setImageResource(imageId);
   }

   public static class SettingsFragment
           extends PreferenceFragmentCompat
           implements SharedPreferences.OnSharedPreferenceChangeListener {
      private final ScheduleRepository repository = ScheduleApp.getInstance().getScheduleRepository();
      public int widgetId;
      private SharedPreferences preferences;
      private ListPreference groupChoicePreference;

      public SettingsFragment(int widgetId){
         super();
         this.widgetId = widgetId;
      }

      public SettingsFragment(){super();}

      @Override
      public void onCreate(@Nullable Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         if(savedInstanceState != null)
            widgetId = savedInstanceState.getInt("id");
      }

      @Override
      public void onSaveInstanceState(@NonNull Bundle outState) {
         outState.putInt("id", widgetId);
         super.onSaveInstanceState(outState);
      }

      @Override
      public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
         repository.update();

         setPreferencesFromResource(R.xml.widget_preferences, rootKey);
         getPreferenceManager().setSharedPreferencesName("WIDGET_" + widgetId);
         preferences = getPreferenceManager().getSharedPreferences();
         preferences.registerOnSharedPreferenceChangeListener(this);

         ListPreference dayChoicePreference = getPreferenceScreen().findPreference("day");
         dayChoicePreference.setValue("today");
         dayChoicePreference.setSummary(dayChoicePreference.getEntry());

         ListPreference themeChoicePreference = getPreferenceScreen().findPreference("theme");
         themeChoicePreference.setValue("system");
         themeChoicePreference.setSummary(themeChoicePreference.getEntry());

         groupChoicePreference = getPreferenceScreen().findPreference("group");
         repository.getGroups().observe(this, groups ->{
            List<String> groupsNew = new ArrayList<>(Arrays.asList(groups));
            groupsNew.sort(Comparator.naturalOrder());

            groupsNew.add(0, getString(R.string.last_chosen));
            String[] arrayEntries = new String[groupsNew.size()];
            groupsNew.toArray(arrayEntries);
            groupChoicePreference.setEntries(arrayEntries);

            groupsNew.remove(0);

            groupsNew.add(0, "last");
            String[] arrayEntryValues = new String[groupsNew.size()];
            groupsNew.toArray(arrayEntryValues);
            groupChoicePreference.setEntryValues(arrayEntryValues);

            groupChoicePreference.setSummary(groupChoicePreference.getEntry());
         });
      }

      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
         Preference preference = getPreferenceScreen().findPreference(s);
         if(preference instanceof ListPreference){
            ListPreference listPreference = (ListPreference) preference;
            listPreference.setSummary(listPreference.getEntry());
         }
      }
   }
}