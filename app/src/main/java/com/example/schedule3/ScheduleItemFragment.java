package com.example.schedule3;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;

import org.json.JSONObject;

public class ScheduleItemFragment extends Fragment implements Observer {
   private static HashMap<Integer, Integer> weekdaysNumbers = new HashMap<>();
   static {
      weekdaysNumbers.put(R.string.monday, Calendar.MONDAY);
      weekdaysNumbers.put(R.string.tuesday, Calendar.TUESDAY);
      weekdaysNumbers.put(R.string.wednesday, Calendar.WEDNESDAY);
      weekdaysNumbers.put(R.string.thursday, Calendar.THURSDAY);
      weekdaysNumbers.put(R.string.friday, Calendar.FRIDAY);
   }
   private View view;
   private TableLayout table;
   private Calendar date;
   private int year;
   private int week;
   private int dayOfWeekID;
   private boolean isOpened = false;

   public static ScheduleItemFragment newInstance(int year, int week, int dayOfWeekId) {
      Bundle args = new Bundle();
      args.putInt("year", year);
      args.putInt("week", week);
      args.putInt("dayOfWeek", dayOfWeekId);
      ScheduleItemFragment fragment = new ScheduleItemFragment();
      fragment.setArguments(args);
      return fragment;
   }
   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      dayOfWeekID = getArguments().getInt("dayOfWeek");
      week = getArguments().getInt("week");
      year = getArguments().getInt("year");
      date = new Calendar.Builder().setWeekDate(year, week, weekdaysNumbers.get(dayOfWeekID)).build();
      if(isDateToday(date)){
         isOpened = true;
      };
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_schedule_item, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      this.view = view;
      Button button = view.findViewById(R.id.button);
      button.setText(generateTitle(date, dayOfWeekID));
      button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
      table = view.findViewById(R.id.schedule);
      if(isOpened){
         table.setVisibility(View.VISIBLE);
      }
      else{
         table.setVisibility(View.GONE);
      }
   }

   private String generateTitle(Calendar date,  int dayOfWeekId){
      String dayOfWeek = getResources().getString(dayOfWeekId);
      //Month is a number in 0 - 11
      int month = date.get(Calendar.MONTH) + 1;
      //Formatting month number with leading zero
      String monthString = String.valueOf(month);
      if(month < 10){
         monthString = "0" + monthString;
      }
      int day = date.get(Calendar.DAY_OF_MONTH);
      String dayString = String.valueOf(day);
      //Formatting day number with leading zero
      if(day < 10){
         dayString = "0" + dayString;
      }
      String label = dayOfWeek + " (" + dayString  + "/" + monthString + ")";
      if(isDateToday(date)){
         label += " - Сегодня";
      }
      return label;
   }

   private boolean isDateToday(Calendar date){
      Calendar rightNow = Calendar.getInstance();
      if(rightNow.get(Calendar.YEAR) == date.get(Calendar.YEAR)
              && rightNow.get(Calendar.MONTH) == date.get(Calendar.MONTH)
              && rightNow.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH)){
         return true;
      }
      return false;
   }

   @Override
   public void update(Observable observable, Object o) {

   }
}