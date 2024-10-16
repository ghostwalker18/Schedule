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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Этот классс представляет собой экран приложения, на котором отображаются заметки к занятиям.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class NotesActivity
        extends AppCompatActivity {
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private String group;
   private Calendar startDate;
   private Calendar endDate;
   private NotesModel model;
   private RecyclerView notesListView;
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
      model = new ViewModelProvider(this).get(NotesModel.class);
      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         group = bundle.getString("group");
         startDate = DateConverters.fromString(bundle.getString("date"));
         endDate = startDate;
         model.setGroup(group);
         model.setStartDate(startDate);
         model.setEndDate(endDate);
      }
      notesListView = findViewById(R.id.notes);
      model.getNotes().observe(this, notes -> {
         notesListView.setAdapter(new NoteAdapter(this, notes));
      });
      findViewById(R.id.edit_note).setOnClickListener(v->openEditNoteActivity());
   }

   private void openEditNoteActivity(){
      Intent intent = new Intent(this, EditNoteActivity.class);
      Bundle bundle = new Bundle();
      bundle.putString("group", group);
      if(startDate != null){
         bundle.putString("date", DateConverters.toString(startDate));
      }
      intent.putExtras(bundle);
      startActivity(intent);
   }
}