package com.example.schedule3;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

public class DaysFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
   private SharedPreferences prefs;
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private ScheduleState state;
   private View view;
   private ProgressBar updateScheduleProgress;
   private TextView updateScheduleStatus;
   private AutoCompleteTextView groupSearch;
   private LiveData<String[]> groups;
   private AutoCompleteTextView teacherSearch;
   private LiveData<String[]> teachers;

   private List<ScheduleItemFragment> days = new ArrayList<>();

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      prefs.registerOnSharedPreferenceChangeListener(this);
      state = new ViewModelProvider(requireActivity()).get(ScheduleState.class);
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
            setUpTeacherSearch(view, prefs);
      }
   }

   private void setUpGroupSearch(View view){
      groupSearch = view.findViewById(R.id.group);
      groupSearch.setOnItemClickListener((adapterView, view1, i, l) -> {
         String group = adapterView.getItemAtPosition(i).toString();
         state.setGroup(group);
         InputMethodManager in = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view1.getApplicationWindowToken(), 0);
      });
      groups = repository.getGroups();
      String savedGroup = repository.getSavedGroup();
      groupSearch.setText(savedGroup);
      state.setGroup(savedGroup);
      groups.observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
         groupSearch.setAdapter(adapter);
         state.setGroup(groupSearch.getText().toString());
      });
   }

   public List<ScheduleItemFragment> getDays(){
      return days;
   }

   private void setUpTeacherSearch(View view, SharedPreferences prefs){
      boolean addTeacherSearch = prefs.getBoolean("addTeacherSearch", false);
      teacherSearch = view.findViewById(R.id.teacher);
      teacherSearch.setOnItemClickListener((adapterView, view1, i, l) -> {
         String teacher = adapterView.getItemAtPosition(i).toString();
         state.setTeacher(teacher);
         InputMethodManager in = (InputMethodManager)getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
         in.hideSoftInputFromWindow(view1.getApplicationWindowToken(), 0);
      });
      teachers = repository.getTeachers();
      teachers.observe(getViewLifecycleOwner(), strings -> {
         ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, strings);
         teacherSearch.setAdapter(adapter);
      });
      if(addTeacherSearch){
         view.findViewById(R.id.teacherLabel).setVisibility(View.VISIBLE);
         teacherSearch.setVisibility(View.VISIBLE);
      }
      else{
         view.findViewById(R.id.teacherLabel).setVisibility(View.GONE);
         teacherSearch.setVisibility(View.GONE);
      }
   }
}