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

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Этот класс представляет собой экран настроек виджета приложения
 *
 * @author  Ипатов Никита
 */
public class WidgetSettingsActivity
        extends AppCompatActivity
        implements View.OnClickListener {
   private SettingsFragment fragment;
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

      resultValue = new Intent();
      resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
      setResult(RESULT_CANCELED, resultValue);
   }

   @Override
   public void onClick(View view) {
      setResult(RESULT_OK, resultValue);
      AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
      ScheduleWidget.updateAppWidget(this, appWidgetManager, widgetID);
      finish();
   }

   public static class SettingsFragment extends PreferenceFragmentCompat {
      public int widgetId;

      public SettingsFragment(int widgetId){
         super();
         this.widgetId = widgetId;
      }
      @Override
      public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
         setPreferencesFromResource(R.xml.widget_preferences, rootKey);
         getPreferenceManager().setSharedPreferencesName("WIDGET_" + widgetId);
      }
   }
}