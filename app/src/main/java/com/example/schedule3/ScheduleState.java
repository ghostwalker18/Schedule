package com.example.schedule3;

import android.os.Build;

import java.util.Calendar;
import java.util.Date;
import java.util.Observable;

import androidx.annotation.RequiresApi;

public class ScheduleState extends Observable {
   private String group;
   private String teacher;
   private int year;
   private int week;
   private Calendar calendar;
   @RequiresApi(api = Build.VERSION_CODES.O)
   public ScheduleState(Date currentDate){
      calendar = new Calendar.Builder().setInstant(currentDate).build();
      year = calendar.get(Calendar.YEAR);
      week = calendar.get(Calendar.WEEK_OF_YEAR);
   }

   public void goNextWeek(){
      calendar.add(Calendar.WEEK_OF_YEAR, 1);
      year = calendar.get(Calendar.YEAR);
      week = calendar.get(Calendar.WEEK_OF_YEAR);
      setChanged();
      notifyObservers();
   }

   public void goPreviousWeek(){
      calendar.add(Calendar.WEEK_OF_YEAR, -1);
      year = calendar.get(Calendar.YEAR);
      week = calendar.get(Calendar.WEEK_OF_YEAR);
      setChanged();
      notifyObservers();
   }

   public int getYear(){
      return year;
   }

   public int getWeek(){
      return week;
   }

   public void setGroup(String group){
      this.group = group;
      setChanged();
      notifyObservers();
   };

   public String getGroup(){
      return group;
   }

   public void setTeacher(String teacher){
      this.teacher = teacher;
      setChanged();
      notifyObservers();
   }

   public String getTeacher(){
      return teacher;
   }
}
