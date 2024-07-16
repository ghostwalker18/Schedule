/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ghostwalker18.schedule;

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
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class ScheduleRepository{
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
   private final MutableLiveData<Status> status = new MutableLiveData<>();


    public static class Status{
       public String text;
       public int progress;

       public Status(String text, int progress){
           this.text = text;
           this.progress = progress;
       }
   }

   public ScheduleRepository(Application app){
      db = ScheduleApp.getInstance().getDatabase();
      context = ScheduleApp.getInstance();
      api = new Retrofit.Builder()
              .baseUrl(baseUri)
              .callbackExecutor(Executors.newSingleThreadExecutor())
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
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.body() != null){
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    response.body().close();
                    mondayTimes.postValue(bitmap);
                    try (FileOutputStream outputStream = context.openFileOutput(mondayTimesPath,
                            Context.MODE_PRIVATE)){
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (IOException e) {}
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
         });
         Call<ResponseBody> otherTimesResponse = api.getOtherTimes();
         otherTimesResponse.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if(response.body() != null){
                    Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                    otherTimes.postValue(bitmap);
                    response.body().close();
                    try (FileOutputStream outputStream = context.openFileOutput(otherTimesPath,
                            Context.MODE_PRIVATE)) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (IOException e) {}
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {}
         });
      }
      else{
            new Thread(() -> {
                  Bitmap bitmap1 = BitmapFactory.decodeFile(mondayTimesFile.getAbsolutePath());
                  mondayTimes.postValue(bitmap1);
                  Bitmap bitmap2 = BitmapFactory.decodeFile(otherTimesFile.getAbsolutePath());
                  otherTimes.postValue(bitmap2);
            }).start();
      }
      //updating schedule database
      new Thread(() -> {
            List<String> scheduleLinks = getLinksForSchedule();
            if(scheduleLinks.size() == 0)
                status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
            for(String link : scheduleLinks){
                status.postValue(new Status(context.getString(R.string.schedule_download_status), 10));
                api.getScheduleFile(link).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if(response.body() != null){
                            status.postValue(new Status(context.getString(R.string.schedule_parsing_status), 33));
                            try(XSSFWorkbook excelFile = new XSSFWorkbook(response.body().byteStream())){
                                List<Lesson> lessons = XMLStoLessonsConverter.convertFirstCorpus(excelFile);
                                db.lessonDao().insertMany(lessons);
                                status.postValue(new Status(context.getString(R.string.processing_completed_status), 100));
                            }
                            catch (IOException e){
                                status.postValue(new Status(context.getString(R.string.schedule_parsing_error), 0));
                            }
                            response.body().close();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
                    }
                });
            }
      }).start();
   }

   public LiveData<Status> getStatus(){
       return status;
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

    public void saveGroup(String group) {
        preferences.edit()
                .putString("savedGroup", group)
                .apply();
    }

    public String getSavedGroup(){
        return preferences.getString("savedGroup", null);
    }
}