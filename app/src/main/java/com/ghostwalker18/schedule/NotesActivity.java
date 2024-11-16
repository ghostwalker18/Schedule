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
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import androidx.annotation.NonNull;
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
   private String group;
   private Calendar startDate;
   private Calendar endDate;
   private boolean isEditAvailable, isDeleteAvailable, isShareAvailable = false;
   private NotesModel model;
   private RecyclerView notesListView;
   private NotesFilterFragment filter;
   private Map<Integer, Note> selectedNotes = new HashMap<>();
   private final NotesRepository repository = ScheduleApp.getInstance().getNotesRepository();
   private final NoteAdapter.OnNoteClickListener listener = new NoteAdapter.OnNoteClickListener() {
      @Override
      public void onNoteSelected(Note note, int position) {
         selectedNotes.put(position, note);
         decideMenuOptions();
      }

      @Override
      public void onNoteUnselected(Note note, int position) {
         selectedNotes.remove(position, note);
         decideMenuOptions();
      }
   };

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_notes_activity, menu);
      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      menu.findItem(R.id.action_edit).setVisible(isEditAvailable);
      menu.findItem(R.id.action_delete).setVisible(isDeleteAvailable);
      menu.findItem(R.id.action_share).setVisible(isShareAvailable);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(@NonNull MenuItem item) {
      if(item.getItemId() == R.id.action_share){
         return shareNotes();
      }
      if(item.getItemId() == R.id.action_delete){
         return deleteNotes();
      }
      if(item.getItemId() == R.id.action_edit){
         return openEditNote();
      }
      return super.onOptionsItemSelected(item);
   }

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_notes);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      ActionBar actionBar = getSupportActionBar();
      if(actionBar != null){
         actionBar.setDisplayHomeAsUpEnabled(true);
      }

      model = new ViewModelProvider(this).get(NotesModel.class);

      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         group = bundle.getString("group");
         startDate = DateConverters.fromString(bundle.getString("date"));
         endDate = startDate;
         if(savedInstanceState == null){
            model.setGroup(group);
            model.setStartDate(startDate);
            model.setEndDate(endDate);
         }
      }

      notesListView = findViewById(R.id.notes);
      model.getNotes().observe(this,
              notes -> notesListView.setAdapter(new NoteAdapter(notes, listener)));

      filter = new NotesFilterFragment();
      findViewById(R.id.filter).setOnClickListener(v->openFilterFragment());

      findViewById(R.id.edit_note).setOnClickListener(v-> openAddNote());

      EditText search = findViewById(R.id.search);
      search.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*Not required*/}

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {/*Not required*/}

         @Override
         public void afterTextChanged(Editable editable) {
            String keyword = editable.toString().trim();
            if(keyword.equals(""))
               keyword = null;
            model.setKeyword(keyword);
         }
      });
   }

   /**
    * Этот метод открывает активность для редактирования или добавления заметки.
    */
   private void openAddNote(){
      Intent intent = new Intent(this, EditNoteActivity.class);
      Bundle bundle = new Bundle();
      bundle.putString("group", group);
      if(startDate != null){
         bundle.putString("date", DateConverters.toString(startDate));
      }
      intent.putExtras(bundle);
      startActivity(intent);
   }

   /**
    * Этот метод окрывает панель фильтра.
    */
   private void openFilterFragment(){
      getSupportFragmentManager()
              .beginTransaction()
              .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
              .replace(R.id.notes_container, filter)
              .commit();
   }

   /**
    * Этот метод позволяет поделиться выбранными заметками.
    * @return
    */
   private boolean shareNotes(){
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      String notes = "";
      for(Note note : selectedNotes.values()){
         notes += note.toString() + "\n";
      }
      intent.putExtra(Intent.EXTRA_TEXT, notes);
      Intent shareIntent = Intent.createChooser(intent, null);
      startActivity(shareIntent);
      selectedNotes = new HashMap<>();
      decideMenuOptions();
      return true;
   }

   /**
    * Этот метод позволяет удалить выбранные заметки.
    * @return
    */
   private boolean deleteNotes(){
      repository.deleteNotes(selectedNotes.values());
      selectedNotes = new HashMap<>();
      decideMenuOptions();
      return true;
   }

   /**
    * Этот метод позволяет, если выбранна одна заметка,
    * открыть экран приложения для ее редактирования.
    * @return
    */
   private boolean openEditNote(){
      Intent intent = new Intent(this, EditNoteActivity.class);
      intent.putExtra("noteID", selectedNotes.entrySet().iterator().next().getValue().id);
      startActivity(intent);
      selectedNotes = new HashMap<>();
      decideMenuOptions();
      return true;
   }

   /**
    * Этот метод позволяет определить, какие опции должны быть в меню.
    */
   private void decideMenuOptions(){
      isEditAvailable = (selectedNotes.size() == 1);
      isShareAvailable = (selectedNotes.size() > 0);
      isDeleteAvailable = (selectedNotes.size() > 0);
      invalidateMenu();
   }
}