package com.example.schedule3;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class DaysFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {
   private SharedPreferences prefs;
   private View view;
   private ScheduleState state;

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
      prefs.registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      view = inflater.inflate(R.layout.fragment_days, container, false);
      setUpTeacherSearch(view, prefs);
      return view;
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
