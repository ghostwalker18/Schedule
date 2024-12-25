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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

/**
 * Этот класс служит для отображения панели фильтров заметок.
 *
 * @author Ипатов Никита
 * @since 3.0
 * @see DatePickerFragment
 */
public class NotesFilterFragment
        extends Fragment {
   public interface VisibilityListener {
      void onFragmentShow();
      void onFragmentHide();
   }
   private final ScheduleRepository repository = ScheduleApp.getInstance().getScheduleRepository();
   private NotesModel model;
   private TextView startDateField;
   private TextView endDateField;
   private AutoCompleteTextView groupField;
   private VisibilityListener listener;

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      model = new ViewModelProvider(requireActivity()).get(NotesModel.class);
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_filter, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState){
      super.onViewCreated(view, savedInstanceState);

      view.findViewById(R.id.set_start_date).setOnClickListener(v -> setStartDate());
      view.findViewById(R.id.set_end_date).setOnClickListener(v -> setEndDate());
      view.findViewById(R.id.close).setOnClickListener(v -> close());
      view.findViewById(R.id.group_clear).setOnClickListener(v -> {
         groupField.setText("");
         model.setGroup(null);
      });

      groupField = view.findViewById(R.id.group);
      groupField.setText(model.getGroup());
      repository.getGroups().observe(getViewLifecycleOwner(), groups -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                 R.layout.autocomplete_item_layout, groups);
         groupField.setAdapter(adapter);
      });
      groupField.setOnItemClickListener((adapterView, view1, i, l) -> {
         model.setGroup(adapterView.getItemAtPosition(i).toString());
         InputMethodManager in = (InputMethodManager)getContext()
                 .getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view1.getApplicationWindowToken(), 0);
      });

      startDateField = view.findViewById(R.id.start_date);
      model.getStartDate().observe(getViewLifecycleOwner(), date ->
              startDateField.setText(DateConverters.toString(date))
      );

      endDateField = view.findViewById(R.id.end_date);
      model.getEndDate().observe(getViewLifecycleOwner(), date ->
              endDateField.setText(DateConverters.toString(date))
      );
   }

   @Override
   public void onAttach(@NonNull Context context) {
      super.onAttach(context);
      listener.onFragmentShow();
   }

   /**
    * Этот метод задает слушателя события сокрытия фрагмента с экрана.
    */
   public void setListener(VisibilityListener listener){
      this.listener = listener;
   }

   /**
    * Этот метод служит для сокрытия фргмента с экрана.
    */
   private void close(){
      getParentFragmentManager()
              .beginTransaction()
              .setCustomAnimations(R.anim.slide_in, R.anim.slide_out)
              .remove(this)
              .commit();
      listener.onFragmentHide();
   }

   /**
    * Этот метод открывает ввод для задания начальной даты выдачи заметок.
    */
   private void setStartDate(){
      DatePickerFragment datePickerFragment = new DatePickerFragment("start");
      datePickerFragment.show(getChildFragmentManager(), "1");
   }

   /**
    * Этот метод открывает ввод для задания конечной даты вывода заметок.
    */
   private void setEndDate(){
      DatePickerFragment datePickerFragment = new DatePickerFragment("end");
      datePickerFragment.show(getChildFragmentManager(), "2");
   }

   /**
    * Этот класс служит для задания начальной/конечной даты выдачи заметок.
    */
   public static class DatePickerFragment
           extends DialogFragment
           implements DatePickerDialog.OnDateSetListener {
      private final String dateType;
      private NotesModel model;

      public DatePickerFragment(String dateType){
         this.dateType = dateType;
      }

      @NonNull
      @Override
      public Dialog onCreateDialog(Bundle savedInstanceState) {
         model = new ViewModelProvider(requireActivity()).get(NotesModel.class);
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

         switch(dateType){
            case "start":
               model.setStartDate(c);
               break;
            case "end":
               model.setEndDate(c);
               break;
         }
      }
   }
}