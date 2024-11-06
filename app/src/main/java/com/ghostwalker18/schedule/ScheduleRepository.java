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

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.github.pjfanning.xlsx.StreamingReader;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * Этот класс представляет собой репозиторий данных приложения.
 *
 * @author  Ипатов Никита
 */
public class ScheduleRepository{
    private final AppDatabase db;
    private final IConverter converter = new XMLStoLessonsConverter();
    private final SharedPreferences preferences;
    private final ScheduleNetworkAPI api;
    private final Context context;
    private static final String BASE_URI = "https://ptgh.onego.ru/9006/";
    private static final String MAIN_SELECTOR = "h2:contains(Расписание занятий и объявления:) + div > table > tbody";
    private static final String MONDAY_TIMES_PATH = "mondayTimes.jpg";
    private static final String OTHER_TIMES_PATH = "otherTimes.jpg";
    private final MutableLiveData<Bitmap> mondayTimes = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> otherTimes = new MutableLiveData<>();
    private final MutableLiveData<Status> status = new MutableLiveData<>();
    private final ExecutorService updateExecutorService = Executors.newFixedThreadPool(4);
    private final List<Future<?>> updateFutures = new ArrayList<>();

    /**
     * Этот класс используетс для отображения статуса обновления репозитория.
     */
    public static class Status{
        public String text;
        public int progress;

        public Status(String text, int progress){
            this.text = text;
            this.progress = progress;
        }
    }

    public ScheduleRepository(Context app,  AppDatabase db){
       this.db = db;
       context = app;
       api = new Retrofit.Builder()
               .baseUrl(BASE_URI)
               .callbackExecutor(Executors.newFixedThreadPool(4))
               .build()
               .create(ScheduleNetworkAPI.class);
       preferences = PreferenceManager.getDefaultSharedPreferences(app);
    }

    /**
     * Этот метод обновляет репозиторий приложения.
     * Метод использует многопоточность и может вызывать исключения в других потоках.
     * Требуется интернет соединение.
     */
    public void update(){
        boolean allJobsDone = true;
        for(Future<?> future : updateFutures){
            allJobsDone &= future.isDone();
        }
        if(allJobsDone){
            updateFutures.clear();
            updateFutures.add(updateExecutorService.submit(this::updateFirstCorpus));
            updateFutures.add(updateExecutorService.submit(this::updateSecondCorpus));
            updateFutures.add(updateExecutorService.submit(this::updateTimes));
        }

    }

    /**
     * Этот метод используется для получения состояния,
     * в котором находится процесс обновления репозитория.
     *
     * @return статус состояния
     */
    public LiveData<Status> getStatus(){
       return status;
   }

    /**
     * Этот метод возвращает все группы, упоминаемые в расписании.
     *
     * @return список групп
     */
    public LiveData<String[]> getGroups(){
      return db.lessonDao().getGroups();
   }

    /**
     * Этот метод позволяет получить имена всех преподавателей, упоминаемых в расписании.
     *
     * @return список учителей
     */

    public LiveData<String[]> getTeachers(){
      return db.lessonDao().getTeachers();
    }

    /**
     * Этот метод позволяет получить список всех предметов в расписании для заданной группы.
     * @param group группа
     * @return список предметов
     */
    public LiveData<String[]> getSubjects(String group) {
        return db.lessonDao().getSubjectsForGroup(group);
    }

    /**
     * Этот метод возращает список занятий в этот день у группы у данного преподавателя.
     * Если группа не указана, то возвращается список занятий у преподавателя в этот день.
     * Если учитель не указан, то возвращается список занятй у группы в этот день.
     *
     * @param date день
     * @param teacher преподаватель
     * @param group группа
     * @return список занятий
     */
    public LiveData<Lesson[]> getLessons(String group, String teacher, Calendar date) {
       if (teacher != null && group != null)
          return db.lessonDao().getLessonsForGroupWithTeacher(date, group, teacher);
       else if (teacher != null)
          return db.lessonDao().getLessonsForTeacher(date, teacher);
       else if (group != null)
          return db.lessonDao().getLessonsForGroup(date, group);
       else return new MutableLiveData<>(new Lesson[]{});
    }

    /**
     * Этот метод используется для получения буфферизированого файла изображения
     * расписания звонков на понедельник.
     *
     * @return фото расписания звонков на понедельник
     */
    public LiveData<Bitmap> getMondayTimes(){
         return mondayTimes;
   }

    /**
     * Этот метод используется для получения буфферизированого файла изображения
     * расписания звонков со вторника по пятницу.
     *
     * @return фото расписания звонков со вторника по пятницу
     */
    public LiveData<Bitmap> getOtherTimes(){
         return otherTimes;
   }

    /**
     * Этот метод позволяет сохранить заметку.
     */
    public void saveNote(Note note){
       db.noteDao().insert(note);
   }

    /**
     * Этот метод позволяет обновить заметку.
     *
     * @param note заметка
     */
    public void updateNote(Note note){
       db.noteDao().update(note);
   }

    /**
     * Этот метод позволяет получить заметку по ее ID.
     *
     * @param id первичный ключ
     * @return заметка
     */
    public LiveData<Note> getNote(Integer id) {
       return db.noteDao().getNote(id);
    }

    /**
     * Этот метод позволяет получить заметки для заданных группы и временного промежутка.
     *
     * @param group
     * @param dates
     * @return
     */
    public LiveData<Note[]> getNotes(String group, Calendar[] dates){
        if(dates.length == 1)
            return db.noteDao().getNotes(dates[0], group);
        return db.noteDao().getNotesForDays(dates, group);
    }

    /**
     * Этот метод позволяет получить заметки для заданного ключевого слова и группы.
     *
     * @param group группа
     * @param keyword ключевое слово
     * @return список заметок
     */
    public LiveData<Note[]> getNotes(String group, String keyword){
        return db.noteDao().getNotesByKeyword(keyword, group);
    }

    /**
     * Этот метод позволяет удалить выбранные заметки из БД.
     *
     * @param notes заметки для удаления
     */
   public void deleteNotes(@NonNull Collection<Note> notes){
       for(Note note : notes)
           db.noteDao().delete(note);
   }

    /**
     * Этот метод получает ссылки с сайта ПАСТ,
     * по которым доступно расписание для корпуса на Мурманской улице.
     *
     * @return список ссылок
     */
   public List<String> getLinksForFirstCorpusSchedule(){
        List<String> links = new ArrayList<>();
        try{
            Document doc = Jsoup.connect(BASE_URI).get();
            Elements linkElements = doc.select(MAIN_SELECTOR).get(0)
                    .select("tr").get(1)
                    .select("td").get(0)
                    .select("a");
            for(Element linkElement : linkElements){
                if(linkElement.attr("href").endsWith(".xlsx"))
                    links.add(linkElement.attr("href"));
            }
            return links;
        }
        catch(IOException e){
            return links;
        }
   }

    /**
     * Этот метод получает ссылки с сайта ПАСТ,
     * по которым доступно расписание для корпуса на Первомайском проспекте.
     *
     * @return список ссылок
     */
    public  List<String> getLinksForSecondCorpusSchedule(){
        List<String> links = new ArrayList<>();
        try{
            Document doc = Jsoup.connect(BASE_URI).get();
            Elements linkElements = doc.select(MAIN_SELECTOR).get(0)
                    .select("tr").get(1)
                    .select("td").get(1)
                    .select("a");
            for(Element linkElement : linkElements){
                if(linkElement.attr("href").endsWith(".xlsx"))
                    links.add(linkElement.attr("href"));
            }
            return links;
        }
        catch (IOException e){
            return links;
        }
    }

    /**
     * Этот метод предназначен для сохранения последней выбранной группы перед закрытием приложения.
     *
     * @param group группа для сохранения
     */
    public void saveGroup(String group) {
        preferences.edit()
                .putString("savedGroup", group)
                .apply();
    }

    /**
     * Этот метод возвращает сохраненную группу.
     *
     * @return группа
     */
    public String getSavedGroup(){
        return preferences.getString("savedGroup", null);
    }

    /**
     * Этот метод позволяет получить имя скачиваемого файла из ссылки на него.
     *
     * @param link ссылка на файл
     * @return имя файла
     */
    public static String getNameFromLink(String link){
        String[] parts = link.split("/");
        return parts[parts.length - 1];
    }

    /**
     * Этот метод используется для обновления изображений расписания звонков
     */
    private void updateTimes(){
        File mondayTimesFile = new File(context.getFilesDir(), MONDAY_TIMES_PATH);
        File otherTimesFile = new File(context.getFilesDir(), OTHER_TIMES_PATH);
        if(!preferences.getBoolean("doNotUpdateTimes", true) ||
                !mondayTimesFile.exists() || !otherTimesFile.exists()){
            Call<ResponseBody> mondayTimesResponse = api.getMondayTimes();
            mondayTimesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if(response.body() != null){
                        Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        response.body().close();
                        mondayTimes.postValue(bitmap);
                        try (FileOutputStream outputStream = context.openFileOutput(MONDAY_TIMES_PATH,
                                Context.MODE_PRIVATE)){
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        } catch (IOException ignored) {/*Not required*/}
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {/*Not required*/}
            });
            Call<ResponseBody> otherTimesResponse = api.getOtherTimes();
            otherTimesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if(response.body() != null){
                        Bitmap bitmap = BitmapFactory.decodeStream(response.body().byteStream());
                        otherTimes.postValue(bitmap);
                        response.body().close();
                        try (FileOutputStream outputStream = context.openFileOutput(OTHER_TIMES_PATH,
                                Context.MODE_PRIVATE)) {
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        } catch (IOException ignored) {/*Not required*/}
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {/*Not required*/}
            });
        }
        else {
            Bitmap bitmap1 = BitmapFactory.decodeFile(mondayTimesFile.getAbsolutePath());
            mondayTimes.postValue(bitmap1);
            Bitmap bitmap2 = BitmapFactory.decodeFile(otherTimesFile.getAbsolutePath());
            otherTimes.postValue(bitmap2);
        }
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями для первого корпуса
     */
    private void updateFirstCorpus(){
        List<String> scheduleLinks = getLinksForFirstCorpusSchedule();
        if(scheduleLinks.isEmpty())
            status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
        for(String link : scheduleLinks){
            status.postValue(new Status(context.getString(R.string.schedule_download_status), 10));
            api.getScheduleFile(link).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if(response.body() != null){
                        status.postValue(new Status(context.getString(R.string.schedule_parsing_status), 33));
                        ZipSecureFile.setMinInflateRatio(0.0075);
                        try(Workbook excelFile = StreamingReader.builder()
                                .rowCacheSize(10)
                                .bufferSize(4096)
                                .open(response.body().byteStream())){
                            List<Lesson> lessons = converter.convertFirstCorpus(excelFile);
                            db.lessonDao().insertMany(lessons);
                            status.postValue(new Status(context.getString(R.string.processing_completed_status), 100));
                        }
                        catch (Exception e){
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
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями для второго корпуса
     */
    private void updateSecondCorpus(){
        List<String> scheduleLinks = getLinksForSecondCorpusSchedule();
        if(scheduleLinks.isEmpty())
            status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
        for(String link : scheduleLinks){
            status.postValue(new Status(context.getString(R.string.schedule_download_status), 10));
            api.getScheduleFile(link).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                    if(response.body() != null){
                        status.postValue(new Status(context.getString(R.string.schedule_parsing_status), 33));
                        ZipSecureFile.setMinInflateRatio(0.0075);
                        try(Workbook excelFile = StreamingReader.builder()
                                .rowCacheSize(10)
                                .bufferSize(4096)
                                .open(response.body().byteStream())){
                            List<Lesson> lessons = converter.convertSecondCorpus(excelFile);
                            db.lessonDao().insertMany(lessons);
                            status.postValue(new Status(context.getString(R.string.processing_completed_status), 100));
                        }
                        catch (Exception e){
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
    }
}