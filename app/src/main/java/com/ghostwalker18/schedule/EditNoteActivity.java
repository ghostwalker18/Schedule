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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Calendar;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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
   private Uri photoID;
   private TextView dateTextView;
   private EditNoteModel model;
   private AutoCompleteTextView groupField;
   private AutoCompleteTextView themeField;
   private EditText textField;
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private final ActivityResultLauncher<Void> takePhotoLauncher = registerForActivityResult(
           new ActivityResultContracts.TakePicturePreview(),
           result -> {
              photo = result;
              ImageView preview = findViewById(R.id.photo_preview);
              preview.setImageBitmap(photo);
           }
   );
   private final ActivityResultLauncher<String> galleryPickLauncher = registerForActivityResult(
           new ActivityResultContracts.GetContent(),
           uri -> {
               photoID = uri;
               ImageView preview = findViewById(R.id.photo_preview);
               preview.setImageURI(uri);
           }
   );
   private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
           new ActivityResultContracts.RequestPermission(),
           granted ->{
              if(granted){
                 takePhotoLauncher.launch(null);
              }
           }
   );

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

      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         group = bundle.getString("group");
         date = DateConverters.fromString(bundle.getString("date"));
      }

      dateTextView = findViewById(R.id.date);
      dateTextView.setText(DateConverters.toString(date));
      themeField = findViewById(R.id.theme);
      textField = findViewById(R.id.text);
      groupField = findViewById(R.id.group);

      groupField.setText(group);
      repository.getGroups().observe(this, groups ->{
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 R.layout.autocomplete_item_layout, groups);
         groupField.setAdapter(adapter);
      });

      model = new ViewModelProvider(this).get(EditNoteModel.class);
      model.getDate().observe(this, calendar -> {
         date = calendar;
         dateTextView.setText(DateConverters.toString(date));
      });

      findViewById(R.id.discard).setOnClickListener(v->finish());
      findViewById(R.id.save).setOnClickListener(v->saveNote());
      findViewById(R.id.set_date).setOnClickListener(v->showDateDialog());
      findViewById(R.id.take_photo).setOnClickListener(v->takePhoto());
      findViewById(R.id.choose_photo).setOnClickListener(v->galleryPickLauncher.launch("image/*"));
   }

   /**
    * Этот метод сохраняет заметку в репозитории и закрывает активность.
    */
   private void saveNote(){
      Note note = new Note();
      note.date = date;
      note.group = group;
      note.theme = themeField.getText().toString();
      note.text = textField.getText().toString();
      if(photoID != null){
         note.photoID = photoID.toString();
         this.getContentResolver().takePersistableUriPermission(photoID,
                 Intent.FLAG_GRANT_READ_URI_PERMISSION);
      }
      repository.saveNote(note);
      finish();
   }

   /**
    * Этот метод открывает окно для выбора и установки даты.
    */
   private void showDateDialog(){
      DatePickerFragment datePickerFragment = new DatePickerFragment();
      datePickerFragment.show(getSupportFragmentManager(), "datePicker");
   }

   /**
    * Этот метод открывает камеру устройств, чтобы сделать фото для заметки.
    */
   private void takePhoto(){
      if(getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)){
         if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast toast = Toast.makeText(this,
                    getResources().getText(R.string.permission_for_photo), Toast.LENGTH_SHORT);
            toast.show();
         }
         else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA);
         }
      }
   }

   /**
    * Этот класс отвечает за окно выбора и установки даты.
    */
   public static class DatePickerFragment
           extends DialogFragment
           implements DatePickerDialog.OnDateSetListener {
      private EditNoteModel model;

      @NonNull
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