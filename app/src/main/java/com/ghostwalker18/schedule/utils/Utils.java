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

package com.ghostwalker18.schedule.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Calendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * В этом классе содержаться различные вспомогательные методы, использующиеся по всему приложению.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public class Utils {

   /**
    * Это перечисление показывает доступность занятия для посещения - прошло, идет, не началось
    */
   public enum LessonAvailability {
      ENDED, STARTED, NOT_STARTED
   }

   /**
    * Этот метод позволяет определить, доступно ли занятие для посещения на текущий момент времени.
    *
    * @param lessonTimes время проведения занятия
    * @param lessonDate дата занятия
    * @return доступность для посещения
    */
   @Nullable
   public static synchronized LessonAvailability isLessonAvailable(Calendar lessonDate, String lessonTimes){
      try{
         Calendar currentTime = Calendar.getInstance();
         String startTime = lessonTimes.split("-")[0];
         String endTime = lessonTimes.split("-")[1];

         Calendar start = (Calendar) lessonDate.clone();
         start.set(Calendar.HOUR, Integer.parseInt(startTime.split("\\.")[0]));
         start.set(Calendar.MINUTE, Integer.parseInt(startTime.split("\\.")[1]));

         Calendar end = (Calendar) lessonDate.clone();
         end.set(Calendar.HOUR, Integer.parseInt(endTime.split("\\.")[0]));
         end.set(Calendar.MINUTE, Integer.parseInt(endTime.split("\\.")[1]));

         if(currentTime.before(start))
            return LessonAvailability.NOT_STARTED;
         else if(currentTime.before(end))
            return LessonAvailability.STARTED;
         else
            return LessonAvailability.ENDED;
      }
      catch (Exception e){
         return null;
      }
   }

   /**
    * Этот метод используется для проверки, является ли заданная дата сегодняшним днем.
    * @param date дата для проверки
    * @return сегодня ли дата
    */
   public static synchronized boolean isDateToday(@NonNull Calendar date){
      Calendar rightNow = Calendar.getInstance();
      return rightNow.get(Calendar.YEAR) == date.get(Calendar.YEAR)
              && rightNow.get(Calendar.MONTH) == date.get(Calendar.MONTH)
              && rightNow.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
   }

   /**
    * Этот метод используется для генерации даты для заголовка UI элемента.
    * @param date дата
    * @return представление даты в формате ХХ/ХХ
    */
   @NonNull
   public static String generateDateForTitle(@NonNull Calendar date){
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
      return dayString + "/" + monthString;
   }

   /**
    * Этот метод позволяет получить имя скачиваемого файла из ссылки на него.
    *
    * @param link ссылка на файл
    * @return имя файла
    */
   public static String getNameFromLink(@NonNull String link){
      String[] parts = link.split("/");
      return parts[parts.length - 1];
   }

   /**
    * Этот метод создает ZIP-архив из указанных файлов.
    * @param sourceFiles файлы для архивации
    * @param archive файл архива
    */
   public static void zip(@NonNull File[] sourceFiles, @NonNull File archive){
      int BUFFER = 4096;
      try(ZipOutputStream out = new ZipOutputStream(
              new BufferedOutputStream(
                      Files.newOutputStream(archive.toPath()
                      )
              ))
      ){
         byte[] data = new byte[BUFFER];

         for (File sourceFile : sourceFiles) {
            try (FileInputStream fi = new FileInputStream(sourceFile);
                 BufferedInputStream origin = new BufferedInputStream(fi, BUFFER)
            ) {
               ZipEntry entry = new ZipEntry(sourceFile.getName());
               out.putNextEntry(entry);

               int count;
               while ((count = origin.read(data, 0, BUFFER)) != -1) {
                  out.write(data, 0, count);
               }
            }
         }
      } catch (Exception ignores){/**/}
   }

   /**
    * Этот метод распаковывает указанный ZIP-архив.
    * @param archive архив для распаковки
    * @param outputDirectory место для извлеченных файлов
    */
   public static void unzip(@NonNull File archive, @NonNull File outputDirectory)
           throws IOException {
      if(!outputDirectory.exists())
         outputDirectory.mkdirs();
      try(ZipInputStream zin = new ZipInputStream(Files.newInputStream(archive.toPath()))
      ){
         ZipEntry ze;
         while ((ze = zin.getNextEntry()) != null) {
               try(FileOutputStream fout = new FileOutputStream(
                       new File(outputDirectory, ze.getName())
               )
               ){
                  for (int c = zin.read(); c != -1; c = zin.read()) {
                     fout.write(c);
                  }
                  zin.closeEntry();
               }
         }
      }
   }
}