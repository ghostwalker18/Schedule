package com.example.schedule3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Date;
import java.util.Observer;
import java.util.Vector;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class DaysFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
   private SharedPreferences prefs;
   private View view;
   private ScheduleState state;
   private Vector<ScheduleItemFragment> days = new Vector<>();

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      prefs.registerOnSharedPreferenceChangeListener(this);
      state = new ScheduleState(new Date());
      days.add(ScheduleItemFragment.newInstance(
              state.getYear(),
              state.getWeek(),
              R.string.monday));
      days.add(ScheduleItemFragment.newInstance(
              state.getYear(),
              state.getWeek(),
              R.string.tuesday));
      days.add(ScheduleItemFragment.newInstance(
              state.getYear(),
              state.getWeek(),
              R.string.wednesday));
      days.add(ScheduleItemFragment.newInstance(
              state.getYear(),
              state.getWeek(),
              R.string.thursday));
      days.add(ScheduleItemFragment.newInstance(
              state.getYear(),
              state.getWeek(),
              R.string.friday));
      for (ScheduleItemFragment day:days) {
         state.addObserver((Observer)day);
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
      view.findViewById(R.id.forward_button).setOnClickListener(v -> {state.goNextWeek();});
      view.findViewById(R.id.back_button).setOnClickListener(v -> {state.goPreviousWeek();});
      for(ScheduleItemFragment day: days){
         getParentFragmentManager()
                 .beginTransaction()
                 .add(R.id.days_container, day)
                 .commit();
      }
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      switch (s){
         case "addTeacherSearch":
            setUpTeacherSearch(view, prefs);
      }
   }

   private void setUpTeacherSearch(View view, SharedPreferences prefs){
      boolean addTeacherSearch = prefs.getBoolean("addTeacherSearch", false);
      if(addTeacherSearch){
         view.findViewById(R.id.teacherLabel).setVisibility(View.VISIBLE);
         view.findViewById(R.id.teacher).setVisibility(View.VISIBLE);
      }
      else{
         view.findViewById(R.id.teacherLabel).setVisibility(View.GONE);
         view.findViewById(R.id.teacher).setVisibility(View.GONE);
      }
   }
}
