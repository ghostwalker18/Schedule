package com.example.schedule3;

import android.app.Application;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class ScheduleRepository {
   private AppDatabase db;
   private SharedPreferences preferences;
   private ScheduleNetworkAPI api;
   private String baseUri = "https://ptgh.onego.ru/";
   private String mondayTimesPath = "images/mondayTimes.jpg";
   private String otherTimesPath = "images/otherTimes.jpg";

   private MutableLiveData<Bitmap> mondayTimes;
   private MutableLiveData<Bitmap> otherTimes;

   public ScheduleRepository(Application app){
      db = ScheduleApp.getInstance().getDatabase();
      api = new Retrofit.Builder()
              .baseUrl(baseUri)
              .build()
              .create(ScheduleNetworkAPI.class);
      preferences = PreferenceManager.getDefaultSharedPreferences(app);
   }

   public void update(){
      File mondayTimesFile = new File(mondayTimesPath);
      File otherTimesFile = new File(otherTimesPath);
      if(!preferences.getBoolean("doNotUpdateTimes", true || !mondayTimesFile.exists() || !otherTimesFile.exists())){
         Call<ResponseBody> mondayTimesResponse = api.getMondayTimes();
         mondayTimesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
               new Thread(
                       () -> {
                          Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                          response.body().close();
                          mondayTimes.setValue(bitmap);
                          try {
                             new FileOutputStream(mondayTimesFile).write(bitmap.getRowBytes());
                          } catch (IOException e) {
                             throw new RuntimeException(e);
                          }
                       }
               ).start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
         });
         Call<ResponseBody> otherTimesResponse = api.getOtherTimes();
         otherTimesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
               new Thread(
                       () -> {
                          Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                          response.body().close();
                          otherTimes.setValue(bitmap);
                          try {
                             new FileOutputStream(otherTimesFile).write(bitmap.getRowBytes());
                          } catch (IOException e) {
                             throw new RuntimeException(e);
                          }
                       }
               ).start();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {

            }
         });
      }
      else{
            new Thread(() -> {
               try {
                  Bitmap bitmap1 = BitmapFactory.decodeStream(new FileInputStream(mondayTimesFile));
                  mondayTimes.setValue(bitmap1);
                  Bitmap bitmap2 = BitmapFactory.decodeStream(new FileInputStream(mondayTimesFile));
                  otherTimes.setValue(bitmap2);
               } catch (IOException e) {
                  throw new RuntimeException(e);
               }
            }).start();
      }
   }

   public LiveData<String[]> getGroups(){
      return db.lessonDao().getGroups();
   }

   public LiveData<String[]> getTeachers(){
      return db.lessonDao().getTeachers();
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
         return mondayTimes;
   }

   public LiveData<Bitmap> getOtherTimes(){
         return otherTimes;
   }
}
