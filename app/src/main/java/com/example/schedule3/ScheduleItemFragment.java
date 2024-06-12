package com.example.schedule3;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import java.util.Calendar;
import java.util.HashMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class ScheduleItemFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener {
   private static HashMap<Integer, Integer> weekdaysNumbers = new HashMap<>();
   static {
      weekdaysNumbers.put(R.string.monday, Calendar.MONDAY);
      weekdaysNumbers.put(R.string.tuesday, Calendar.TUESDAY);
      weekdaysNumbers.put(R.string.wednesday, Calendar.WEDNESDAY);
      weekdaysNumbers.put(R.string.thursday, Calendar.THURSDAY);
      weekdaysNumbers.put(R.string.friday, Calendar.FRIDAY);
   }
   private SharedPreferences preferences;
   private View view;
   private Button button;
   private TableLayout table;
   private ScheduleState state;
   private MutableLiveData<Calendar> date = new MutableLiveData<>();
   private LiveData<Lesson[]> lessons = new MutableLiveData<>();
   private int dayOfWeekID;
   private boolean isOpened = false;
   private String mode;



   public static ScheduleItemFragment newInstance(int dayOfWeekId) {
      Bundle args = new Bundle();
      args.putInt("dayOfWeek", dayOfWeekId);
      ScheduleItemFragment fragment = new ScheduleItemFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      state = new ViewModelProvider(requireActivity()).get(ScheduleState.class);
      dayOfWeekID = getArguments().getInt("dayOfWeek");
      preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
      button = view.findViewById(R.id.button);
      button.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
      table = view.findViewById(R.id.schedule);
      state.getCalendar().observe(getViewLifecycleOwner(), calendar -> {
         date.setValue(new Calendar.Builder()
                 .setWeekDate(state.getYear(), state.getWeek(), weekdaysNumbers.get(dayOfWeekID))
                 .build());
      });
      state.getGroup().observe(getViewLifecycleOwner(), group -> {
         lessons = state.getLessons(date.getValue());
         lessons.observe(getViewLifecycleOwner(), lessons -> {populateTable(table, lessons);});
      });
      state.getTeacher().observe(getViewLifecycleOwner(), teacher -> {
         lessons = state.getLessons(date.getValue());
         lessons.observe(getViewLifecycleOwner(), lessons -> {populateTable(table, lessons);});
      });
      date.observe(getViewLifecycleOwner(), date -> {
         if(isDateToday(date)){
            isOpened = true;
         };
         button.setText(generateTitle(date, dayOfWeekID));
         lessons = state.getLessons(date);
         lessons.observe(getViewLifecycleOwner(), lessons -> {populateTable(table, lessons);});
         showTable();
      });
      setUpMode();
   }

   private void setUpMode(){
      mode = preferences.getString("scheduleStyle", "");
      switch (mode){
         case "in_fragment":
            button.setOnClickListener(this::showSchedule);
            break;
         case "in_activity":
            button.setOnClickListener(this::openScheduleInActivity);
            break;
      }
   }

   private void showSchedule(View view){
      isOpened = !isOpened;
      showTable();
   }

   private void openScheduleInActivity(View view){
      startActivity(new Intent(this.getActivity(), ScheduleItemActivity.class));
   }

   private void showTable(){
      if(isOpened && mode.equals("in_fragment")){
         button.setCompoundDrawablesWithIntrinsicBounds(null,
                 null,
                 getResources().getDrawable(R.drawable.baseline_keyboard_arrow_up_24),
                 null);
         table.setVisibility(View.VISIBLE);
      }
      else{
         button.setCompoundDrawablesWithIntrinsicBounds(null,
                 null,
                 getResources().getDrawable(R.drawable.baseline_keyboard_arrow_down_24),
                 null);
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

   private void populateTable(TableLayout table, Lesson[] lessons){
      table.removeViews(1, table.getChildCount() - 1);
      for(Lesson lesson : lessons){
         addLesson(table, lesson);
      }
   }
   private void addLesson(TableLayout table, Lesson lesson){
      LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      TableRow tr = (TableRow) inflater.inflate(R.layout.schedule_row, null);
      ((TextView)tr.findViewById(R.id.number)).setText(lesson.lessonNumber.toString());
      ((TextView)tr.findViewById(R.id.subject)).setText(lesson.subject);
      ((TextView)tr.findViewById(R.id.teacher)).setText(lesson.teacher);
      ((TextView)tr.findViewById(R.id.room)).setText(Integer.toString(lesson.roomNumber));
      table.addView(tr,new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      switch (s){
         case "scheduleStyle":
            setUpMode();
            break;
      }
   }
}