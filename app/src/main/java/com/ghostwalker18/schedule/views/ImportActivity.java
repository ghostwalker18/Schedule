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
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.database.AppDatabase;
import com.ghostwalker18.schedule.utils.Utils;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;

/**
 * Этот класс используется для отображенияя экрана импорта и экспорта БД приложения.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public class ImportActivity
        extends AppCompatActivity {
   private Spinner operationTypeSpinner;
   private Spinner dataTypesSpinner;
   private Spinner importModeSpinner;
   private Button doOperationButton;
   /**
    * Используется после экспорта БД.
    */
   private final ActivityResultLauncher<Intent> shareDBFile = registerForActivityResult(
           new ActivityResultContracts.StartActivityForResult(),
           result -> {
              AppDatabase.deleteExportDB(this);
              File exportedFile = new File(getCacheDir(), "database/pcme_schedule.zip");
              if(exportedFile.exists())
                 exportedFile.delete();
           }
   );
   /**
    * Используется для импорта БД.
    */
   private final ActivityResultLauncher<String[]> documentPicker = registerForActivityResult(
           new ActivityResultContracts.OpenDocument(),
           fileName -> new Thread(() -> {
              String[] importPolicyValues = getResources().getStringArray(R.array.import_mode_values);
              String importPolicy = importPolicyValues[importModeSpinner.getSelectedItemPosition()];
              String[] dataTypeValues = getResources().getStringArray(R.array.data_types_values);
              String dataType = dataTypeValues[dataTypesSpinner.getSelectedItemPosition()];
              File databaseCache = new File(getCacheDir(), "database");
              if (!databaseCache.exists())
                 databaseCache.mkdir();
              File archive = new File(databaseCache, "pcme_schedule.zip");
              File importedFile = null;
              if (fileName != null) {
                 //First, copy file that we got to cache directory to get access to it
                 try (InputStream origin = getContentResolver().openInputStream(fileName);
                      BufferedInputStream in = new BufferedInputStream(origin, 4096);
                      OutputStream out = Files.newOutputStream(archive.toPath())
                 ) {
                    byte[] data = new byte[4096];
                    int count;
                    while ((count = in.read(data, 0, 4096)) != -1)
                       out.write(data, 0, count);

                    Utils.unzip(archive, databaseCache);
                    importedFile = new File(databaseCache, "export_database.db");
                    ScheduleApp.getInstance().getDatabase().importDBFile(this,
                            importedFile, dataType, importPolicy);
                 } catch (Exception e) {
                    runOnUiThread(()->{
                       Toast toast = Toast.makeText(this, R.string.import_db_error,
                               Toast.LENGTH_SHORT);
                       toast.show();
                    });
                 } finally {
                    archive.delete();
                    if(importedFile != null)
                     importedFile.delete();
                 }
              }
           }).start()
   );

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_import);
      Toolbar myToolbar = findViewById(R.id.toolbar);
      setSupportActionBar(myToolbar);
      ActionBar actionBar = getSupportActionBar();
      if (actionBar != null) {
         actionBar.setDisplayHomeAsUpEnabled(true);
      }
      doOperationButton = findViewById(R.id.do_operation);
      doOperationButton.setOnClickListener(v -> exportDB());
      operationTypeSpinner = findViewById(R.id.operation_type);
      dataTypesSpinner = findViewById(R.id.data_types);
      importModeSpinner = findViewById(R.id.import_policy_type);
      operationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String operation = getResources().getStringArray(R.array.operation_type_values)[i];
            if(operation.equals("import")){
               findViewById(R.id.import_options_panel).setVisibility(View.VISIBLE);
               doOperationButton.setText(R.string.import_data);
               doOperationButton.setOnClickListener(v -> importDB());
            } else {
               findViewById(R.id.import_options_panel).setVisibility(View.GONE);
               doOperationButton.setText(R.string.export_data);
               doOperationButton.setOnClickListener(v -> exportDB());
            }
         }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {/*Not required*/}
      });
   }

   /**
    * Этот метод используется для экспорта БД приложения.
    */
   private void exportDB(){
      new Thread(() -> {
         try{
            String[] dataTypeValues = getResources().getStringArray(R.array.data_types_values);
            String dataType = dataTypeValues[dataTypesSpinner.getSelectedItemPosition()];
            File file = ScheduleApp.getInstance().getDatabase().exportDBFile(this, dataType);
            File databaseCache = new File(getCacheDir(), "database");
            if(!databaseCache.exists())
               databaseCache.mkdir();
            File exportedFile = new File(databaseCache, "pcme_schedule.zip");
            if(exportedFile.exists()){
               exportedFile.delete();
               exportedFile.createNewFile();
            }
            Utils.zip(new File[]{file}, exportedFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM,
                    FileProvider.getUriForFile(this,
                            "com.ghostwalker18.schedule.timefilesprovider",
                            exportedFile));
            shareIntent.setType("application/zip");
            shareDBFile.launch(Intent.createChooser(shareIntent, null));
         } catch (Exception ignored){/*Not required*/}
      }).start();
   }

   /**
    * Этот метод используется для импорта БД приложения.
    */
   private void importDB(){
      new Thread(() -> documentPicker.launch(new String[]{"application/zip"})).start();
   }
}