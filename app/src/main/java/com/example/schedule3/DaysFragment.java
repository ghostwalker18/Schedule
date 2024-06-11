package com.example.schedule3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import java.util.Date;
import java.util.Vector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

public class DaysFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
   private SharedPreferences prefs;
   private AppDatabase db;
   private View view;
   private ScheduleState state;
   private Spinner groupSpinner;
   private LiveData<String[]> groups;
   private Spinner teacherSpinner;
   private LiveData<String[]> teachers;

   private Vector<ScheduleItemFragment> days = new Vector<>();

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      db = ScheduleApp.getInstance().getDatabase();
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      prefs.registerOnSharedPreferenceChangeListener(this);
      state = new ViewModelProvider(requireActivity()).get(ScheduleState.class);
      state.setCalendar(new Date());
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

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_days, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      this.view = view;
      setUpTeacherSearch(this.view, prefs);
      setUpGroupSearch(this.view);
      view.findViewById(R.id.forward_button).setOnClickListener(v -> state.goNextWeek());
      view.findViewById(R.id.back_button).setOnClickListener(v -> state.goPreviousWeek());
      if(savedInstanceState == null){
         for(ScheduleItemFragment day: days){
            getParentFragmentManager()
                    .beginTransaction()
                    .add(R.id.days_container, day)
                    .commit();
         }
      }
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      switch (s){
         case "addTeacherSearch":
            setUpTeacherSearch(view, prefs);
      }
   }

   private void setUpGroupSearch(View view){
      groupSpinner = view.findViewById(R.id.group);
      groupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String group = groupSpinner.getSelectedItem().toString();
            state.setGroup(group);
         }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {

         }
      });
      groups = db.lessonDao().getGroups();
      groups.observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
         groupSpinner.setAdapter(adapter);
      });
   }

   private void setUpTeacherSearch(View view, SharedPreferences prefs){
      boolean addTeacherSearch = prefs.getBoolean("addTeacherSearch", false);
      teacherSpinner = view.findViewById(R.id.teacher);
      teacherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
         @Override
         public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String teacher = teacherSpinner.getSelectedItem().toString();
            state.setTeacher(teacher);
         }

         @Override
         public void onNothingSelected(AdapterView<?> adapterView) {

         }
      });
      teachers = db.lessonDao().getTeachers();
      teachers.observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
         teacherSpinner.setAdapter(adapter);
      });
      if(addTeacherSearch){
         view.findViewById(R.id.teacherLabel).setVisibility(View.VISIBLE);
         teacherSpinner.setVisibility(View.VISIBLE);
      }
      else{
         view.findViewById(R.id.teacherLabel).setVisibility(View.GONE);
         teacherSpinner.setVisibility(View.GONE);
      }
   }
}
