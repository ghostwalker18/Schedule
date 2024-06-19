package com.example.schedule3;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

class ScheduleRepository{
   private final AppDatabase db;
   private final SharedPreferences preferences;
   private final ScheduleNetworkAPI api;
   private final Context context;
   private final String baseUri = "https://ptgh.onego.ru/9006/";
   private final String mainSelector = "h2:contains(Расписание занятий и объявления:) + div > table > tbody";
   private final String mondayTimesPath = "mondayTimes.jpg";
   private final String otherTimesPath = "otherTimes.jpg";
   private final MutableLiveData<Bitmap> mondayTimes = new MutableLiveData<>();
   private final MutableLiveData<Bitmap> otherTimes = new MutableLiveData<>();

   public ScheduleRepository(Application app){
      db = ScheduleApp.getInstance().getDatabase();
      context = ScheduleApp.getInstance();
      api = new Retrofit.Builder()
              .baseUrl(baseUri)
              .build()
              .create(ScheduleNetworkAPI.class);
      preferences = PreferenceManager.getDefaultSharedPreferences(app);
   }

   public void update(){
      //updating times files
      File mondayTimesFile = new File(context.getFilesDir(), mondayTimesPath);
      File otherTimesFile = new File(context.getFilesDir(), otherTimesPath);
      if(!preferences.getBoolean("doNotUpdateTimes", true) || !mondayTimesFile.exists() || !otherTimesFile.exists()){
         Call<ResponseBody> mondayTimesResponse = api.getMondayTimes();
         mondayTimesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
               new Thread(
                       () -> {
                          if(response.body() != null){
                              Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                              response.body().close();
                              mondayTimes.postValue(bitmap);
                              try (FileOutputStream outputStream = context.openFileOutput(mondayTimesPath,
                                      Context.MODE_PRIVATE)){
                                  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                              } catch (IOException e) {
                                  throw new RuntimeException(e);
                              }
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
                          if(response.body() != null){
                              Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                              otherTimes.postValue(bitmap);
                              response.body().close();
                              try (FileOutputStream outputStream = context.openFileOutput(otherTimesPath,
                                      Context.MODE_PRIVATE)) {
                                  bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                              } catch (IOException e) {
                                  throw new RuntimeException(e);
                              }
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
                  Bitmap bitmap1 = BitmapFactory.decodeFile(mondayTimesFile.getAbsolutePath());
                  mondayTimes.postValue(bitmap1);
                  Bitmap bitmap2 = BitmapFactory.decodeFile(otherTimesFile.getAbsolutePath());
                  otherTimes.postValue(bitmap2);
            }).start();
      };
      //updating schedule database
      new Thread(() -> {
            List<String> scheduleLinks = getLinksForSchedule();
            for(String link : scheduleLinks){
                api.getScheduleFile(link).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if(response.body() != null){
                            try(XSSFWorkbook excelFile = new XSSFWorkbook(response.body().byteStream())){
                                List<Lesson> lessons = XMLStoLessonsConverter.convert(excelFile);
                                db.lessonDao().insertMany(lessons);
                            }
                            catch (IOException e){
                                throw new RuntimeException();
                            }
                            response.body().close();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
      }).start();
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

   public  List<String> getLinksForSchedule(){
       List<String> links = new ArrayList<>();
       try{
           Document doc = Jsoup.connect(baseUri).get();
           Elements linkElements = doc.select(mainSelector).get(0)
                   .select("tr").get(1)
                   .select("td").get(1)
                   .select("p > strong > span > a");
           for(Element linkElement : linkElements){
               links.add(linkElement.attr("href"));
           }
           return links;
       }
       catch (IOException e){
           return links;
       }
   }
}