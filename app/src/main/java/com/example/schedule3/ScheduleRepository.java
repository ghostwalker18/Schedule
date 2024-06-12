package com.example.schedule3;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import java.io.File;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import retrofit2.Retrofit;

class ScheduleRepository {
   private AppDatabase db;
   private SharedPreferences preferences;
   private Retrofit api;
   private String mondayTimesURL = "https://ptgh.onego.ru/9006/#gallery-1";
   private String mondayTimesPath = "images/mondayTimes.jpg";
   private String otherTimesURL = "https://ptgh.onego.ru/9006/#gallery-2";
   private String otherTimesPath = "images/otherTimes.jpg";

   public ScheduleRepository(Application app){
      db = ScheduleApp.getInstance().getDatabase();
      preferences = PreferenceManager.getDefaultSharedPreferences(app);
   }

   public void update(){
      File mondayTimes = new File(mondayTimesPath);
      File otherTimes = new File(otherTimesPath);
      if(!preferences.getBoolean("doNotUpdateTimes", true || !mondayTimes.exists() || !otherTimes.exists())){

      }
   }

   public LiveData<Lesson[]> getLessons(String group, String teacher, Calendar date) {
      if (teacher != null && group != null)
         return db.lessonDao().getLessonsForGroupWithTeacher(date, group, teacher);
      else if (teacher != null)
         return db.lessonDao().getLessonsForTeacher(date, teacher);
      else if (group != null)
         return db.lessonDao().getLessonsForGroup(date, group);
      else return new MutableLiveData<>(new Lesson[]{});
   }

   public LiveData<Bitmap> getMondayTimes(){
         return null;
   }

   public LiveData<Bitmap> getOtherTimes(){
         return null;
   }
}
