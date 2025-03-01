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

package com.ghostwalker18.schedule.models;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import com.ghostwalker18.schedule.database.AppDatabase;
import com.ghostwalker18.schedule.converters.IConverter;
import com.ghostwalker18.schedule.database.LessonDao;
import com.ghostwalker18.schedule.network.NetworkService;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.network.ScheduleNetworkAPI;
import com.ghostwalker18.schedule.converters.XMLStoLessonsConverter;
import com.github.pjfanning.xlsx.StreamingReader;
import com.github.pjfanning.xlsx.exceptions.OpenException;
import com.github.pjfanning.xlsx.exceptions.ParseException;
import com.github.pjfanning.xlsx.exceptions.ReadException;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Этот класс представляет собой репозиторий данных приложения о расписании.
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see LessonDao
 * @see IConverter
 * @see NetworkService
 */
public class ScheduleRepository{
    public static final String BASE_URI = "https://ptgh.onego.ru/9006/";
    private static final String MAIN_SELECTOR = "h2:contains(Расписание занятий и объявления:) + div > table > tbody";
    private static final String MONDAY_TIMES_PATH = "mondayTimes.jpg";
    private static final String OTHER_TIMES_PATH = "otherTimes.jpg";
    public static final String MONDAY_TIMES_URL =
            "https://r1.nubex.ru/s1748-17b/47698615b7_fit-in~1280x800~filters:no_upscale()__f44488_08.jpg";
    public static final String OTHER_TIMES_URL =
            "https://r1.nubex.ru/s1748-17b/320e9d2d69_fit-in~1280x800~filters:no_upscale()__f44489_bb.jpg";
    private final AppDatabase db;
    private final IConverter converter = new XMLStoLessonsConverter();
    private final SharedPreferences preferences;
    private final ScheduleNetworkAPI api;
    private final Context context;
    private final MutableLiveData<Bitmap> mondayTimes = new MutableLiveData<>();
    private final MutableLiveData<Bitmap> otherTimes = new MutableLiveData<>();
    private final MutableLiveData<Status> status = new MutableLiveData<>();
    private CompletableFuture<UpdateResult> updateResult;
    private final ExecutorService updateExecutorService = Executors.newFixedThreadPool(4);
    private final List<CompletableFuture<UpdateResult>> updateFutures = new ArrayList<>();
    private boolean allJobsDone = true;

    /**
     * Это перечисление показывает, чем завершилось обновление репозитория.
     */
    public enum UpdateResult {
        SUCCESS, FAIL;

        public static UpdateResult fromInt(int i){
            if (i == 0) {
                return SUCCESS;
            }
            return FAIL;
        }

        public static int toInt(UpdateResult result){
            if(result == SUCCESS)
                return 0;
            return 1;
        }
    }

    /**
     * Этот класс используетс для отображения статуса обновления репозитория.
     */
    public static class Status{
        public final String text;
        public final int progress;

        public Status(String text, int progress){
            this.text = text;
            this.progress = progress;
        }
    }

    public ScheduleRepository(Context app, AppDatabase db, @NonNull NetworkService networkService){
        this.db = db;
        context = app;
        api = networkService.getScheduleAPI();
        preferences = PreferenceManager.getDefaultSharedPreferences(app);
    }

    /**
     * Этот метод обновляет репозиторий приложения.
     * Метод использует многопоточность и может вызывать исключения в других потоках.
     * Требуется интернет соединение.
     */
    public void update(){
        String downloadFor = preferences.getString("downloadFor", "all");
        if(allJobsDone) {
            allJobsDone = false;
            updateFutures.clear();
            if (downloadFor.equals("all") || downloadFor.equals("first"))
                updateFutures.add(
                        CompletableFuture.supplyAsync(this::updateFirstCorpus, updateExecutorService)
                );
            if (downloadFor.equals("all") || downloadFor.equals("second"))
                updateFutures.add(
                        CompletableFuture.supplyAsync(this::updateSecondCorpus, updateExecutorService)
                );
            updateFutures.add(
                    CompletableFuture.supplyAsync(this::updateTimes, updateExecutorService)
            );

            updateResult = CompletableFuture.allOf(updateFutures.toArray(new CompletableFuture[0]))
                    .thenApplyAsync(ignored -> {
                        allJobsDone = true;
                        for(CompletableFuture<UpdateResult> future : updateFutures){
                            if(future.getNow(UpdateResult.FAIL) == UpdateResult.FAIL){
                                preferences.edit()
                                        .putInt("previous_update_result",
                                                UpdateResult.toInt(UpdateResult.FAIL))
                                        .apply();
                                return UpdateResult.FAIL;
                            }
                        }
                        preferences.edit()
                                .putInt("previous_update_result",
                                        UpdateResult.toInt(UpdateResult.SUCCESS))
                                .apply();
                    return UpdateResult.SUCCESS;
            }, updateExecutorService);
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
     * Этот метод используется для получения результата обновления репозитория.
     */
    public CompletableFuture<UpdateResult> onUpdateCompleted(){
        return updateResult;
    }

    /**
     * Этот метод используется для получения результата предыдущего обновления репозитория.
     */
    public UpdateResult getUpdateResult(){
        return UpdateResult.fromInt(preferences.getInt("previous_update_result", 0));
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
     * Этот метод позволяет получить последнюю дату,
     * для которой для заданной группы указано расписание.
     * @param group группа
     * @return последняя дата, для которой существует расписание
     */
    public Calendar getLastKnownLessonDate(String group){
        return db.lessonDao().getLastKnownLessonDate(group);
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
     * Этот метод получает ссылки с сайта ПАСТ,
     * по которым доступно расписание для корпуса на Мурманской улице.
     *
     * @return список ссылок
     */
   public List<String> getLinksForFirstCorpusSchedule(){
        List<String> links = new ArrayList<>();
        try{
            Document doc = api.getMainPage().execute().body();
            Elements linkElements = doc
                    .select(MAIN_SELECTOR).get(0)
                    .select("tr").get(1)
                    .select("td").get(0)
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
     * Этот метод получает ссылки с сайта ПАСТ,
     * по которым доступно расписание для корпуса на Первомайском проспекте.
     *
     * @return список ссылок
     */
    public  List<String> getLinksForSecondCorpusSchedule(){
        List<String> links = new ArrayList<>();
        try{
            Document doc = api.getMainPage().execute().body();
            Elements linkElements = doc
                    .select(MAIN_SELECTOR).get(0)
                    .select("tr").get(1)
                    .select("td").get(1)
                    .select("a");
            for(Element linkElement : linkElements){
                if(linkElement.attr("href").endsWith(".xlsx"))
                    links.add(linkElement.attr("href"));
            }
            return links;
        }
        catch (Exception e){
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
     * Этот метод используется для обновления изображений расписания звонков
     */
    private UpdateResult updateTimes(){
        File mondayTimesFile = new File(context.getFilesDir(), MONDAY_TIMES_PATH);
        File otherTimesFile = new File(context.getFilesDir(), OTHER_TIMES_PATH);
        if(!preferences.getBoolean("doNotUpdateTimes", true) ||
                !mondayTimesFile.exists() || !otherTimesFile.exists()){
            api.getMondayTimes().enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    try(ResponseBody body = response.body();
                        FileOutputStream outputStream = context
                                .openFileOutput(MONDAY_TIMES_PATH, Context.MODE_PRIVATE)
                    ){
                        Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                        mondayTimes.postValue(bitmap);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (Exception ignored) {/*Not required*/}
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call,
                                      @NonNull Throwable t) {/*Not required*/}
            });
            api.getOtherTimes().enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    try(ResponseBody body = response.body();
                        FileOutputStream outputStream = context
                                .openFileOutput(OTHER_TIMES_PATH, Context.MODE_PRIVATE)
                    ){
                        Bitmap bitmap = BitmapFactory.decodeStream(body.byteStream());
                        otherTimes.postValue(bitmap);
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    } catch (Exception ignored) {/*Not required*/}
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call,
                                      @NonNull Throwable t) {/*Not required*/}
            });
        }
        else {
            Bitmap bitmap1 = BitmapFactory.decodeFile(mondayTimesFile.getAbsolutePath());
            mondayTimes.postValue(bitmap1);
            Bitmap bitmap2 = BitmapFactory.decodeFile(otherTimesFile.getAbsolutePath());
            otherTimes.postValue(bitmap2);
        }
        return UpdateResult.SUCCESS;
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями для первого корпуса
     */
    private UpdateResult updateFirstCorpus(){
        return updateSchedule(this::getLinksForFirstCorpusSchedule, converter::convertFirstCorpus);
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями для второго корпуса
     */
    private UpdateResult updateSecondCorpus(){
        return updateSchedule(this::getLinksForSecondCorpusSchedule, converter::convertSecondCorpus);
    }

    /**
     * Этот метод используется для обновления БД приложения занятиями
     * @param linksGetter метод для получения ссылок на файлы расписания
     * @param parser парсер файлов расписания
     */
    private UpdateResult updateSchedule(Callable<List<String>> linksGetter, IConverter.IConversion parser){
        List<String> scheduleLinks;
        try {
            scheduleLinks = linksGetter.call();
        } catch (Exception e){
            return UpdateResult.FAIL;
        }
        if(scheduleLinks.isEmpty()){
            status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
            return UpdateResult.FAIL;
        }
        final List<UpdateResult> successCounter = new ArrayList<>();
        final CountDownLatch latch = new CountDownLatch(scheduleLinks.size());
        for(String link : scheduleLinks){
            status.postValue(new Status(context.getString(R.string.schedule_download_status), 10));
            api.getScheduleFile(link).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
                    ZipSecureFile.setMinInflateRatio(0.0005);
                    status.postValue(new Status(context.getString(R.string.schedule_opening_status), 33));
                    try(ResponseBody body = response.body();
                        Workbook excelFile = StreamingReader.builder()
                                .rowCacheSize(10)
                                .bufferSize(10485670)
                                .open(body.byteStream())
                    ){
                        status.postValue(new Status(context.getString(R.string.schedule_parsing_status), 50));
                        List<Lesson> lessons = parser.convert(excelFile);
                        db.lessonDao().insertMany(lessons);
                        status.postValue(new Status(context.getString(R.string.processing_completed_status), 100));
                        successCounter.add(UpdateResult.SUCCESS);
                    }
                    catch(OpenException | ReadException | ParseException e) {
                        status.postValue(new Status(context.getString(R.string.schedule_opening_error), 0));
                    }
                    catch(Exception e) {
                        status.postValue(new Status(context.getString(R.string.schedule_parsing_error), 0));
                    }
                    finally {
                        latch.countDown();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call,
                                      @NonNull Throwable t) {
                    latch.countDown();
                    status.postValue(new Status(context.getString(R.string.schedule_download_error), 0));
                }
            });
        }
        try{
            latch.await();
            if(successCounter.size() == scheduleLinks.size())
                return UpdateResult.SUCCESS;
            else
                return UpdateResult.FAIL;
        }
        catch (Exception e) {
            return UpdateResult.FAIL;
        }
    }
}