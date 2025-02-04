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
import java.io.File;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    * Используется для выбора файла из устройства пользователя.
    */
   private final ActivityResultLauncher<String[]> documentPicker = registerForActivityResult(
           new ActivityResultContracts.OpenDocument(),
           fileName -> {
              String importPolicy = importModeSpinner.getSelectedItem().toString();
              String dataType = dataTypesSpinner.getSelectedItem().toString();
              try{
                 if(fileName != null)
                    ScheduleApp.getInstance().getDatabase().importDBFile(this,
                         new File(fileName.getEncodedPath()), dataType, importPolicy);
              } catch (Exception e){
                 Toast toast = Toast.makeText(this, R.string.import_db_error, Toast.LENGTH_SHORT);
                 toast.show();
              }
           }
   );
   /**
    * Используется для запуска share intent и последущего удаления временного файла.
    */
   private final ActivityResultLauncher<Intent> shareDBFile = registerForActivityResult(
           new ActivityResultContracts.StartActivityForResult(),
           result -> AppDatabase.deleteExportDB(this)
   );

   @Override
   protected void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_import);
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
      String dataType = dataTypesSpinner.getSelectedItem().toString();
      File file = ScheduleApp.getInstance().getDatabase().exportDBFile(this, dataType);
      Intent shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.putExtra(Intent.EXTRA_STREAM, file);
      shareIntent.setType("application/octet-stream");
      shareDBFile.launch(Intent.createChooser(shareIntent, null));
   }

   /**
    * Этот метод используется для импорта БД приложения.
    */
   private void importDB(){
      documentPicker.launch(new String[]{"application/octet-stream"});
   }
}