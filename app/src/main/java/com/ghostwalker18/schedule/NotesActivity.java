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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
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
   private boolean isEditAvailable, isDeleteAvailable, isShareAvailable;
   private boolean isEditChanged = true, isDeleteChanged = true, isSharedChanged = true;
   private NotesModel model;
   private RecyclerView notesListView;
   private NotesFilterFragment filter;
   private Map<Integer, Note> selectedNotes = new ConcurrentHashMap<>();
   private final NotesRepository repository = ScheduleApp.getInstance().getNotesRepository();
   private final NoteAdapter.OnNoteClickListener listener = new NoteAdapter.OnNoteClickListener() {
      @Override
      public void onNoteSelected(Note note, int position) {
         selectedNotes.put(position, note);
         findViewById(R.id.selectionPanel).setVisibility(View.VISIBLE);
         findViewById(R.id.search_bar).setVisibility(View.GONE);
         ((TextView) findViewById(R.id.selectedCount)).setText(String.valueOf(selectedNotes.size()));
         decideMenuOptions();
      }

      @Override
      public void onNoteUnselected(Note note, int position) {
         selectedNotes.remove(position, note);
         if(selectedNotes.size() == 0){
            findViewById(R.id.selectionPanel).setVisibility(View.GONE);
            findViewById(R.id.search_bar).setVisibility(View.VISIBLE);
         }
         ((TextView) findViewById(R.id.selectedCount)).setText(String.valueOf(selectedNotes.size()));
         decideMenuOptions();
      }
   };

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      getMenuInflater().inflate(R.menu.menu_notes_activity, menu);

      ImageView editItemView = (ImageView) menu.findItem(R.id.action_edit).getActionView();
      editItemView.setImageResource(R.drawable.baseline_edit_document_36);
      editItemView.setPadding(20,10,20,10);
      editItemView.setOnClickListener(view -> openEditNote());

      ImageView deleteItemView = (ImageView) menu.findItem(R.id.action_delete).getActionView();
      deleteItemView.setImageResource(R.drawable.baseline_delete_36);
      deleteItemView.setPadding(20,10,20,10);
      deleteItemView.setOnClickListener(view -> deleteNotes());

      ImageView shareItemView = (ImageView) menu.findItem(R.id.action_share).getActionView();
      shareItemView.setImageResource(R.drawable.baseline_share_36);
      shareItemView.setPadding(20,10,20,10);
      shareItemView.setOnClickListener(view -> shareNotes());
      return true;
   }

   @Override
   public boolean onPrepareOptionsMenu(Menu menu) {
      if(isEditChanged){
         toggleMenuItem(menu, R.id.action_edit, isEditAvailable);
         isEditChanged = false;
      }
      if(isDeleteChanged){
         toggleMenuItem(menu, R.id.action_delete, isDeleteAvailable);
         isDeleteChanged = false;
      }
      if(isSharedChanged){
         toggleMenuItem(menu, R.id.action_share, isShareAvailable);
         isSharedChanged = false;
      }
      return true;
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
              notes -> {
                  if(notes.length == 0)
                     findViewById(R.id.no_notes_notification).setVisibility(View.VISIBLE);
                  else
                     findViewById(R.id.no_notes_notification).setVisibility(View.GONE);
                  notesListView.setAdapter(new NoteAdapter(notes, listener));
              });

      filter = new NotesFilterFragment();
      filter.setListener(new NotesFilterFragment.VisibilityListener() {
         @Override
         public void onFragmentShow() {
            NoteAdapter adapter = (NoteAdapter) notesListView.getAdapter();
            if(adapter != null)
               adapter.setClickable(false);
         }

         @Override
         public void onFragmentHide() {
            NoteAdapter adapter = (NoteAdapter) notesListView.getAdapter();
            if(adapter != null)
               adapter.setClickable(true);
         }
      });
      findViewById(R.id.filter).setOnClickListener(v->openFilterFragment());

      findViewById(R.id.edit_note).setOnClickListener(v-> openAddNote());

      EditText search = findViewById(R.id.search);
      search.addTextChangedListener(new TextWatcherAdapter() {
         @Override
         public void afterTextChanged(Editable editable) {
            String keyword = editable.toString().trim();
            if(keyword.equals(""))
               keyword = null;
            model.setKeyword(keyword);
         }
      });

      findViewById(R.id.selectionCancel).setOnClickListener(view -> resetSelection());
   }

   /**
    * Этот метод отвечает за появление/скрытие элемента меню.
    */
   private void toggleMenuItem(Menu menu, int menuItemID, boolean isAvailable){
      AnimatorSet open = (AnimatorSet) AnimatorInflater
              .loadAnimator(this, R.animator.menu_item_appear);
      AnimatorSet close = (AnimatorSet) AnimatorInflater
              .loadAnimator(this, R.animator.menu_item_disappear);
      MenuItem menuItem = menu.findItem(menuItemID);
      if(isAvailable){
         open.setTarget(menuItem.getActionView());
         open.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
               super.onAnimationStart(animation);
               menuItem.setVisible(true);
            }
         });
         open.start();
      } else {
         close.setTarget(menuItem.getActionView());
         close.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
               super.onAnimationEnd(animation);
               menuItem.setVisible(false);
            }
         });
         close.start();
      }
   }

   /**
    * Этот метод сбрасывает выделение всех заметок.
    */
   private void resetSelection(){
      for(int position : selectedNotes.keySet()){
         NoteViewHolder item = (NoteViewHolder) notesListView
                 .findViewHolderForAdapterPosition(position);
         if(item != null)
            item.setSelected(false);
         listener.onNoteUnselected(selectedNotes.get(position), position);
      }
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
    */
   private void shareNotes(){
      Intent intent = new Intent(Intent.ACTION_SEND);
      intent.setType("text/plain");
      StringBuilder notes = new StringBuilder();
      for(Note note : selectedNotes.values()){
         notes.append(note.toString()).append("\n");
      }
      intent.putExtra(Intent.EXTRA_TEXT, notes.toString());
      Intent shareIntent = Intent.createChooser(intent, null);
      startActivity(shareIntent);
      resetSelection();
      decideMenuOptions();
   }

   /**
    * Этот метод позволяет удалить выбранные заметки.
    */
   private void deleteNotes(){
      repository.deleteNotes(selectedNotes.values());
      resetSelection();
      decideMenuOptions();
   }

   /**
    * Этот метод позволяет, если выбранна одна заметка,
    * открыть экран приложения для ее редактирования.
    */
   private void openEditNote(){
      Intent intent = new Intent(this, EditNoteActivity.class);
      intent.putExtra("noteID", selectedNotes.entrySet().iterator().next().getValue().id);
      startActivity(intent);
      resetSelection();
      decideMenuOptions();
   }

   /**
    * Этот метод позволяет определить, какие опции должны быть в меню.
    */
   private void decideMenuOptions(){
      isEditChanged = (isEditAvailable != (selectedNotes.size() == 1));
      isSharedChanged = (isShareAvailable != (selectedNotes.size() > 0));
      isDeleteChanged = (isDeleteAvailable != (selectedNotes.size() > 0));
      isEditAvailable = (selectedNotes.size() == 1);
      isShareAvailable = (selectedNotes.size() > 0);
      isDeleteAvailable = (selectedNotes.size() > 0);
      invalidateMenu();
   }
}