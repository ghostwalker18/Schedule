package com.example.schedule3;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

public class ScheduleItemFragment extends Fragment {

   public static ScheduleItemFragment newInstance(int year, int week, String dayOfWeek) {
      Bundle args = new Bundle();
      args.putInt("year", year);
      args.putInt("week", week);
      args.putString("dayOfWeek", dayOfWeek);
      ScheduleItemFragment fragment = new ScheduleItemFragment();
      fragment.setArguments(args);
      return fragment;
   }
   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_schedule_item, container, false);
   }

   public void setSchedule(JSONObject schedule){

   }
}