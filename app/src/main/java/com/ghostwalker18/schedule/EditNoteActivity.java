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
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

/**
 * Этот класс представляет собой экран редактирования или добавления новой заметки
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class EditNoteActivity
        extends AppCompatActivity {
   private Bitmap photo;
   private TextView dateTextView;
   private EditNoteModel model;
   private AutoCompleteTextView groupField;
   private AutoCompleteTextView themeField;
   private EditText textField;
   private ImageView preview;
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
           uri -> model.setPhotoID(uri)
   );
   private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
           new ActivityResultContracts.RequestPermission(),
           granted ->{
              if(granted)
                 takePhotoLauncher.launch(null);
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

      model = new ViewModelProvider(this).get(EditNoteModel.class);

      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         if(bundle.getInt("noteID") != 0)
            model.setNoteID(bundle.getInt("id"));
         if(bundle.getString("group") != null)
            model.setGroup(bundle.getString("group"));
         if(bundle.getString("date") != null)
            model.setDate(DateConverters.fromString(bundle.getString("date")));
      };

      dateTextView = findViewById(R.id.date);
      model.getDate().observe(this, date -> {
         dateTextView.setText(DateConverters.toString(date));
      });

      themeField = findViewById(R.id.theme);
      model.getTheme().observe(this, theme -> themeField.setText(theme));
      model.getThemes().observe(this, themes -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 R.layout.autocomplete_item_layout, themes);
         themeField.setAdapter(adapter);
      });

      textField = findViewById(R.id.text);
      model.getText().observe(this, text -> {
         textField.setText(text);
      });

      groupField = findViewById(R.id.group);
      model.getGroup().observe(this, group -> groupField.setText(group));
      model.getGroups().observe(this, groups ->{
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 R.layout.autocomplete_item_layout, groups);
         groupField.setAdapter(adapter);
      });
      groupField.setOnItemClickListener((adapterView, view, i, l) ->
              model.setGroup(groupField.getText().toString()));

      preview = findViewById(R.id.photo_preview);
      model.getPhotoID().observe(this, photoID -> {
         ContentResolver contentResolver = this.getContentResolver();
         try {
            preview.setImageBitmap(BitmapFactory.decodeStream(
                    contentResolver.openInputStream(photoID)));
         } catch (Exception ignored) {}
      });

      findViewById(R.id.discard).setOnClickListener(v -> finish());
      findViewById(R.id.save).setOnClickListener(v -> saveNote());
      findViewById(R.id.set_date).setOnClickListener(v -> showDateDialog());
      findViewById(R.id.take_photo).setOnClickListener(v -> takePhoto());
      findViewById(R.id.choose_photo).setOnClickListener(v -> galleryPickLauncher.launch("image/*"));
   }

   /**
    * Этот метод сохраняет заметку в репозитории и закрывает активность.
    */
   private void saveNote(){
      model.setTheme(themeField.getText().toString());
      model.setText(textField.getText().toString());
      model.saveNote();
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