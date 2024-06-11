package com.example.schedule3;

import java.util.Calendar;
import java.util.Date;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScheduleState extends ViewModel{
   private AppDatabase db;
   private MutableLiveData<String> group = new MutableLiveData<>();
   private MutableLiveData<String> teacher = new MutableLiveData<>();
   private MutableLiveData<Calendar> calendar = new MutableLiveData<>();
   public ScheduleState(){
      db = ScheduleApp.getInstance().getDatabase();
   }
   public void setCalendar(Date currentDate){
      Calendar date = new Calendar.Builder().setInstant(currentDate).build();
      calendar.setValue(date);
   }

   public LiveData<Calendar> getCalendar(){
      return calendar;
   }

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

   public LiveData<Lesson[]>getLessons(Calendar date){
      if(teacher.getValue() != null && group.getValue() != null)
         return db.lessonDao().getLessonsForGroupWithTeacher(date, group.getValue(), teacher.getValue());
      else if(teacher.getValue() != null)
            return db.lessonDao().getLessonsForTeacher(date, teacher.getValue());
         else if (group.getValue() != null)
            return db.lessonDao().getLessonsForGroup(date, group.getValue());
               else return new MutableLiveData<>(new Lesson[]{});
   }
}
