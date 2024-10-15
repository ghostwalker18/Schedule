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

import android.content.Intent;
import android.os.Bundle;

import java.util.Calendar;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

/**
 * Этот классс представляет собой экран приложения, на котором отображаются заметки к занятиям.
 * @author Ипатов Никита
 * @since 3.0
 */
public class NotesActivity
        extends AppCompatActivity {
   private String group;
   private Calendar date;
   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_notes);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         actionBar.setDisplayHomeAsUpEnabled(true);
      }
      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         group = bundle.getString("group");
         date = DateConverters.fromString(bundle.getString("date"));
      }
      findViewById(R.id.edit_note).setOnClickListener(v->openEditNoteActivity());
   }

   private void openEditNoteActivity(){
      Intent intent = new Intent(this, EditNoteActivity.class);
      Bundle bundle = new Bundle();
      bundle.putString("group", group);
      if(date != null){
         bundle.putString("date", DateConverters.toString(date));
      }
      intent.putExtras(bundle);
      startActivity(intent);
   }
}