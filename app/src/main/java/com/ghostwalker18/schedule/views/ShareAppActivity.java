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

import android.content.Intent;
import android.os.Bundle;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import io.appmetrica.analytics.AppMetrica;

/**
 * Этот класс представляет собой экран, где пользователь может поделиться ссылкой на приложение.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class ShareAppActivity
        extends AppCompatActivity {

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_share_app);
      Toolbar myToolbar = findViewById(R.id.toolbar);
      setSupportActionBar(myToolbar);
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         actionBar.setDisplayHomeAsUpEnabled(true);
      }
      findViewById(R.id.share_link).setOnClickListener(v -> shareLink());

      if(ScheduleApp.getInstance().isAppMetricaActivated())
         AppMetrica.reportEvent("Поделились приложением");
   }

   /**
    * Этот метод используется, чтобы поделиться ссылокой на расписание в RuStore.
    */
   private void shareLink(){
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getText(R.string.rustore_link));
      startActivity(shareIntent);
   }
}