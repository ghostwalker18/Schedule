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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import com.ghostwalker18.schedule.TextWatcherAdapter;
import com.ghostwalker18.schedule.viewmodels.ScheduleModel;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

/**
 * Этот класс представляет собой элемент интерфейса, используемый для
 * отображения расписания занятий.
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see ScheduleRepository
 * @see ScheduleModel
 */
public class DaysFragment
        extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
   private SharedPreferences prefs;
   private final ScheduleRepository repository = ScheduleApp.getInstance().getScheduleRepository();
   private ScheduleModel state;
   private View view;
   private ProgressBar updateScheduleProgress;
   private TextView updateScheduleStatus;
   private AutoCompleteTextView groupSearch;
   private AutoCompleteTextView teacherSearch;
   private final List<ScheduleItemFragment> days = new ArrayList<>();

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      prefs = PreferenceManager.getDefaultSharedPreferences(requireContext());
      prefs.registerOnSharedPreferenceChangeListener(this);
      state = new ViewModelProvider(requireActivity()).get(ScheduleModel.class);
      if(savedInstanceState == null){
         days.add(ScheduleItemFragment.newInstance(
                 R.string.monday));
         days.add(ScheduleItemFragment.newInstance(
                 R.string.tuesday));
         days.add(ScheduleItemFragment.newInstance(
                 R.string.wednesday));
         days.add(ScheduleItemFragment.newInstance(
                 R.string.thursday));
         days.add(ScheduleItemFragment.newInstance(
                 R.string.friday));
      }
      else{
         getChildFragmentManager()
                 .getFragments()
                 .forEach((item)->days.add((ScheduleItemFragment)item));
      }
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_days, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      this.view = view;
      setUpGroupSearch();
      setUpTeacherSearch();
      enableTeacherSearch();
      view.findViewById(R.id.forward_button).setOnClickListener(v -> state.goNextWeek());
      view.findViewById(R.id.forward_button).setOnLongClickListener(v -> {
         DatePickerFragment dialog = new DatePickerFragment();
         dialog.show(getChildFragmentManager(), "date");
         return true;
      });
      view.findViewById(R.id.back_button).setOnClickListener(v -> state.goPreviousWeek());
      view.findViewById(R.id.back_button).setOnLongClickListener(v -> {
         DatePickerFragment dialog = new DatePickerFragment();
         dialog.show(getChildFragmentManager(), "date");
         return true;
      });
      view.findViewById(R.id.refresh_button).setOnClickListener(v -> repository.update());
      updateScheduleProgress = view.findViewById(R.id.updateScheduleProgress);
      updateScheduleStatus = view.findViewById(R.id.updateScheduleStatus);
      repository.getStatus().observe(getViewLifecycleOwner(), status -> {
         updateScheduleProgress.setProgress(status.progress);
         updateScheduleStatus.setText(status.text);
      });
      if(savedInstanceState == null){
         for(ScheduleItemFragment day: days){
            getChildFragmentManager()
                    .beginTransaction()
                    .add(R.id.days_container, day)
                    .commit();
         }
      }
   }

   @Override
   public void onStop() {
      super.onStop();
      repository.saveGroup(state.getGroup().getValue());
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      switch (s){
         case "addTeacherSearch":
            enableTeacherSearch();
      }
   }

   /**
    * Этот метод используется для получения доступа к элементам расписания.
    * @return список отдельных элементов расписания
    */
   public List<ScheduleItemFragment> getDays(){
      return days;
   }

   /**
    * Этот метод используется для настройки элемента поиска по группе.
    */
   private void setUpGroupSearch(){
      groupSearch = view.findViewById(R.id.group);
      groupSearch.setOnItemClickListener((adapterView, view1, i, l) -> {
         String group = adapterView.getItemAtPosition(i).toString();
         state.setGroup(group);
         InputMethodManager in = (InputMethodManager)requireContext()
                 .getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view1.getApplicationWindowToken(), 0);
      });
      groupSearch.addTextChangedListener(new TextWatcherAdapter() {
         @Override
         public void afterTextChanged(Editable editable) {
            if(editable.toString().equals(""))
               state.setGroup(null);
         }
      });
      String savedGroup = repository.getSavedGroup();
      groupSearch.setText(savedGroup);
      state.setGroup(savedGroup);

      ImageButton groupClear = view.findViewById(R.id.group_clear);
      groupClear.setOnClickListener((v)->{
         state.setGroup(null);
         groupSearch.setText("");
      });

      repository.getGroups().observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                 R.layout.autocomplete_item_layout, strings);
         groupSearch.setAdapter(adapter);
         state.setGroup(groupSearch.getText().toString());
      });
   }

   /**
    * Этот метод используется для настройки элемента поиска по преподавателю.
    */
   private void setUpTeacherSearch(){
      teacherSearch = view.findViewById(R.id.teacher);
      teacherSearch.setOnItemClickListener((adapterView, view1, i, l) -> {
         String teacher = adapterView.getItemAtPosition(i).toString();
         state.setTeacher(teacher);
         InputMethodManager in = (InputMethodManager)requireContext()
                 .getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view1.getApplicationWindowToken(), 0);
      });
      teacherSearch.addTextChangedListener(new TextWatcherAdapter() {
         @Override
         public void afterTextChanged(Editable editable) {
            if(editable.toString().equals(""))
               state.setTeacher(null);
         }
      });

      ImageButton teacherClear = view.findViewById(R.id.clear_teacher);
      teacherClear.setOnClickListener((v)->{
         state.setTeacher(null);
         teacherSearch.setText("");
      });

      repository.getTeachers().observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                 R.layout.autocomplete_item_layout, strings);
         teacherSearch.setAdapter(adapter);
      });
   }

   /**
    * Этот метод используется для отображения элемента поиска по преподавателю
    * согласно настройкам приложения.
    */
   private void enableTeacherSearch(){
      boolean addTeacherSearch = prefs.getBoolean("addTeacherSearch", false);
      if(addTeacherSearch){
         view.findViewById(R.id.teacher_label).setVisibility(View.VISIBLE);
         view.findViewById(R.id.clear_teacher).setVisibility(View.VISIBLE);
         teacherSearch.setVisibility(View.VISIBLE);
      }
      else{
         view.findViewById(R.id.teacher_label).setVisibility(View.GONE);
         view.findViewById(R.id.clear_teacher).setVisibility(View.GONE);
         state.setTeacher(null);
         teacherSearch.setText("");
         teacherSearch.setVisibility(View.GONE);
      }
   }

   /**
    * Этот класс используется для выбора даты (недели) для отображения расписания.
    *
    * @author Ипатов Никита
    * @since 4.1
    */
   public static class DatePickerFragment
           extends DialogFragment
           implements DatePickerDialog.OnDateSetListener {
      private ScheduleModel model;

      @NonNull
      @Override
      public Dialog onCreateDialog(Bundle savedInstanceState) {
         model = new ViewModelProvider(requireActivity()).get(ScheduleModel.class);
         // Use the current date as the default date in the picker.
         final Calendar c = Calendar.getInstance();
         int year = c.get(Calendar.YEAR);
         int month = c.get(Calendar.MONTH);
         int day = c.get(Calendar.DAY_OF_MONTH);

         return new DatePickerDialog(requireContext(), this, year, month, day);
      }
      @Override
      public void onDateSet(DatePicker datePicker, int year, int month, int day) {
         Calendar date = Calendar.getInstance();
         date.set(year, month, day);
         model.goToDate(date);
      }
   }
}