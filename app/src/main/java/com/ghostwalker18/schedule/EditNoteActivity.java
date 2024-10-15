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

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import java.util.Calendar;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * Этот класс представляет собой экран редактирования или добавления новой заметки
 * @author Ипатов Никита
 * @since 3.0
 */
public class EditNoteActivity
        extends AppCompatActivity {
   private Calendar date = Calendar.getInstance();
   private String group;
   private Bitmap photo;
   private TextView dateTextView;
   private EditNoteModel model;
   private AutoCompleteTextView theme;
   private EditText text;
   private ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_edit_note);
      Toolbar toolbar = findViewById(R.id.toolbar);
      setSupportActionBar(toolbar);
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         actionBar.setDisplayHomeAsUpEnabled(true);
      };

      dateTextView = findViewById(R.id.date);
      dateTextView.setText("dick");
      theme = findViewById(R.id.theme);
      text = findViewById(R.id.text);

      model = new ViewModelProvider(this).get(EditNoteModel.class);
      model.getDate().observe(this, calendar -> {
         date = calendar;
         dateTextView.setText(DateConverters.toString(date));
      });

      findViewById(R.id.discard).setOnClickListener(v->finish());
      findViewById(R.id.save).setOnClickListener(v->saveNote());
      findViewById(R.id.set_date).setOnClickListener(v->showDateDialog());
      findViewById(R.id.take_photo).setOnClickListener(v->takePhoto());
   }

   private void saveNote(){
      Note note = new Note();
      note.date = date;
      note.group = group;
      note.theme = theme.toString();
      note.text = text.toString();
      repository.saveNote(note);
   }

   private void showDateDialog(){
      DatePickerFragment datePickerFragment = new DatePickerFragment();
      datePickerFragment.show(getSupportFragmentManager(), "datePicker");
   }

   private void takePhoto(){
      if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
         if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                 != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
            startActivity(intent);
         }
      }
   }

   public static class DatePickerFragment
           extends DialogFragment
           implements DatePickerDialog.OnDateSetListener {
      private EditNoteModel model;
      @Override
      public Dialog onCreateDialog(Bundle savedInstanceState) {
         model = new ViewModelProvider(requireActivity()).get(EditNoteModel.class);
         // Use the current date as the default date in the picker.
         final Calendar c = Calendar.getInstance();
         int year = c.get(Calendar.YEAR);
         int month = c.get(Calendar.MONTH);
         int day = c.get(Calendar.DAY_OF_MONTH);

         // Create a new instance of DatePickerDialog and return it.
         return new DatePickerDialog(requireContext(), this, year, month, day);
      }

      @Override
      public void onDateSet(DatePicker view, int year, int month, int day) {
         Calendar c = Calendar.getInstance();
         c.set(year, month, day);
         model.setDate(c);
      }
   }
}