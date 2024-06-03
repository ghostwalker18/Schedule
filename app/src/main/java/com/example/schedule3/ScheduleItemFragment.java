package com.example.schedule3;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

public class ScheduleItemFragment extends Fragment {

   public ScheduleItemFragment(){}

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_schedule_item, container, false);
   }

   public void setSchedule(JSONObject schedule){

   }
}