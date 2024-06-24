package com.example.schedule3;

import java.util.Calendar;
import java.util.Date;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.preference.PreferenceManager;

public class ScheduleState extends ViewModel{
   private MutableLiveData<String> group = new MutableLiveData<>();
   private MutableLiveData<String> teacher = new MutableLiveData<>();
   private MutableLiveData<Calendar> calendar = new MutableLiveData<>(
           new Calendar.Builder()
                   .setInstant(new Date())
                   .build());

   public void goNextWeek(){
      Calendar date = calendar.getValue();
      date.add(Calendar.WEEK_OF_YEAR, 1);
      calendar.setValue(date);
   }

   public void goPreviousWeek(){
      Calendar date = calendar.getValue();
      date.add(Calendar.WEEK_OF_YEAR, -1);
      calendar.setValue(date);
   }

   public LiveData<Calendar> getCalendar(){
      return calendar;
   }

   public int getYear(){
      return calendar.getValue().get(Calendar.YEAR);
   }

   public int getWeek(){
      return calendar.getValue().get(Calendar.WEEK_OF_YEAR);
   }

   public void setGroup(String group){
      this.group.setValue(group);
   };

   public LiveData<String> getGroup(){
      return group;
   }

   public void setTeacher(String teacher){
      this.teacher.setValue(teacher);
   }

   public LiveData<String> getTeacher(){
      return teacher;
   }

   @Override
   protected void onCleared() {
      super.onCleared();
   }
}