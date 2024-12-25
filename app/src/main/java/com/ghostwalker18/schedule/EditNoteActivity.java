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
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.util.Calendar;
import java.util.Random;
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
 *
 * @author Ипатов Никита
 * @since 3.0
 * @see EditNoteModel
 * @see PreviewFragment
 */
public class EditNoteActivity
        extends AppCompatActivity {
   private Uri photoUri;
   private boolean isSaved = false;
   private final Random nameSuffixGenerator = new Random();
   private TextView dateTextView;
   private EditNoteModel model;
   private AutoCompleteTextView groupField;
   private AutoCompleteTextView themeField;
   private EditText textField;
   private PreviewFragment preview;
   private final ActivityResultLauncher<Uri> takePhotoLauncher = registerForActivityResult(
           new ActivityResultContracts.TakePicture(),
           result -> {
              model.addPhotoID(photoUri);
              MediaScannerConnection.scanFile(this,
                      new String[]{photoUri.getEncodedPath()}, new String[]{"image/jpeg"}, null);
           }
   );
   private final ActivityResultLauncher<String> galleryPickLauncher = registerForActivityResult(
           new ActivityResultContracts.GetContent(),
           uri -> model.addPhotoID(uri)
   );
   private final ActivityResultLauncher<String> cameraPermissionLauncher = registerForActivityResult(
           new ActivityResultContracts.RequestPermission(),
           granted -> {
              if(granted){
                 File directory = new File(Environment.getExternalStoragePublicDirectory(
                         Environment.DIRECTORY_PICTURES).getAbsoluteFile(), "ScheduleNotes");
                 if(!directory.exists())
                    directory.mkdirs();
                 File newFile = new File(directory, makeNotePhotoName());
                 while(newFile.exists())
                    newFile = new File(directory, makeNotePhotoName());
                 photoUri = Uri.fromFile(newFile);
                 Uri contentUri = FileProvider.getUriForFile(this,
                         "com.ghostwalker18.schedule.timefilesprovider", newFile);
                 takePhotoLauncher.launch(contentUri);
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
      if (actionBar != null)
         actionBar.setDisplayHomeAsUpEnabled(true);

      model = new ViewModelProvider(this).get(EditNoteModel.class);

      Bundle bundle = getIntent().getExtras();
      if(bundle != null){
         if(bundle.getInt("noteID") != 0){
            model.setNoteID(bundle.getInt("noteID"));
            actionBar.setTitle(R.string.edit_note);
         }

         if(bundle.getString("group") != null)
            model.setGroup(bundle.getString("group"));
         if(bundle.getString("date") != null)
            model.setDate(DateConverters.fromString(bundle.getString("date")));
      }

      dateTextView = findViewById(R.id.date);
      model.getDate().observe(this,
              date -> dateTextView.setText(DateConverters.toString(date)));

      themeField = findViewById(R.id.theme);
      model.getTheme().observe(this, theme -> themeField.setText(theme));
      model.getThemes().observe(this, themes -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 R.layout.autocomplete_item_layout, themes);
         themeField.setAdapter(adapter);
      });

      textField = findViewById(R.id.text);
      model.getText().observe(this, text -> textField.setText(text));

      groupField = findViewById(R.id.group);
      model.getGroup().observe(this, group -> groupField.setText(group));
      model.getGroups().observe(this, groups ->{
         ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                 R.layout.autocomplete_item_layout, groups);
         groupField.setAdapter(adapter);
      });
      groupField.setOnItemClickListener((adapterView, view, i, l) ->
              model.setGroup(groupField.getText().toString()));

      preview = (PreviewFragment) getSupportFragmentManager().findFragmentById(R.id.preview);
      preview.setEditable(true);
      preview.setListener(uri -> model.removePhotoID(uri));
      model.getPhotoIDs().observe(this, photoIDs -> {
         preview.setImageIDs(photoIDs);
      });

      findViewById(R.id.group_clear).setOnClickListener(v -> model.setGroup(""));
      findViewById(R.id.theme_clear).setOnClickListener(v -> model.setTheme(""));
      findViewById(R.id.text_clear).setOnClickListener(v -> model.setText(""));
      findViewById(R.id.discard).setOnClickListener(v -> exitActivity());
      findViewById(R.id.save).setOnClickListener(v -> saveNote());
      findViewById(R.id.set_date).setOnClickListener(v -> showDateDialog());
      findViewById(R.id.take_photo).setOnClickListener(v -> takePhoto());
      findViewById(R.id.choose_photo).setOnClickListener(v -> galleryPickLauncher.launch("image/*"));
   }

   @Override
   protected void onDestroy() {
      for(Uri photoUri : model.getPhotoIDs().getValue()){
         if(photoUri != null && photoUri.getEncodedPath() != null && !isSaved){
            File photoFile = new File(photoUri.getEncodedPath());
            photoFile.delete();
         }
      }
      super.onDestroy();
   }

   /**
    * Этот метод сохраняет заметку в репозитории и закрывает активность.
    */
   private void saveNote(){
      model.setTheme(themeField.getText().toString());
      model.setText(textField.getText().toString());
      model.saveNote();
      isSaved = true;
      finish();
   }

   /**
    * Этот метод позволяет сгенерировать имя для сделанного фото для заметки.
    * @return имя файла для фото
    */
   private String makeNotePhotoName(){
      String res = "";
      res = res + DateConverters.DATE_FORMAT_PHOTO.format(model.getDate().getValue().getTime()) + "_";
      res += nameSuffixGenerator.nextInt(10000);
      res += ".jpg";
      return res;
   }

   /**
    * Этот метод позволяет закрыть активность и освободить ресурсы.
    */
   private void exitActivity(){
      for(Uri photoUri : model.getPhotoIDs().getValue()){
         if(photoUri != null && photoUri.getEncodedPath() != null){
            File photoFile = new File(photoUri.getEncodedPath());
            photoFile.delete();
         }
      }
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