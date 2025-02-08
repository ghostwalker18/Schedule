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

package com.ghostwalker18.schedule.converters;

import com.ghostwalker18.schedule.models.Lesson;
import com.ghostwalker18.schedule.models.Note;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования Calendar в String для БД и наоборот
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see Lesson
 * @see Note
 */
public class DateConverters {
   public static final SimpleDateFormat DATE_FORMAT_NOTIFICATION = new SimpleDateFormat(
           "dd.MM",
           new Locale("ru")
   );
   public static final SimpleDateFormat DATE_FORMAT_PHOTO = new SimpleDateFormat(
           "dd_MM_yyyy",
           new Locale("ru"));
   private static final SimpleDateFormat DATE_FORMAT_DB = new SimpleDateFormat(
           "yyyy-MM-dd",
           new Locale("ru"));
   private static final SimpleDateFormat DATE_FORMAT_FIRST_CORPUS = new SimpleDateFormat(
           "d MMMM yyyy",
           new Locale("ru"));
   private static final SimpleDateFormat DATE_FORMAT_SECOND_CORPUS = new SimpleDateFormat(
           "dd.MM.yyyy",
           new Locale("ru"));

   /**
    * Этот метод преобразует Calendar сущности в String для БД.
    *
    * @param date  the entity attribute value to be converted
    * @return converted data
    */
   @Nullable
   @TypeConverter
   public static String toString(Calendar date){
      return date == null ? null : DATE_FORMAT_DB.format(date.getTime());
   }

   /**
    * Этот метод преобразует String из БД в Calendar сущности.
    *
    * @param date  the data from the database column to be converted
    * @return converted data
    */
   @TypeConverter
   public static Calendar fromString(String date){
      return stringToCal(date, DATE_FORMAT_DB);
   }

   /**
    * Этот метод преобразует String из расписания второго корпуса
    * на Первомайском пр. в Calendar сущности.
    * @param date дата из расписания второго корпуса
    * @return преобразованная дата в формате Calendar
    */
   public Calendar convertSecondCorpusDate(String date){
      return stringToCal(date, DATE_FORMAT_SECOND_CORPUS);
   }

   /**
    * Этот метод преобразует String из расписания первого корпуса на
    * Мурманской ул. в Calendar сущности.
    * @param date дата из расписания первого корпуса
    * @return преобразованная дата в формате Calendar
    */
   public Calendar convertFirstCorpusDate(String date){
      return stringToCal(date, DATE_FORMAT_FIRST_CORPUS);
   }

   /**
    * Этот метод преобразует Calendar в дату для уведомления
    * @param date дата
    * @return дата в формате XX.XX
    */
   @NonNull
   public static String convertForNotification(@Nullable Calendar date){
      return date == null ? "" : DATE_FORMAT_NOTIFICATION.format(date.getTime());
   }

   /**
    * Этот метод используется для преобразования строки в дату согласно заданному формату.
    * @param date строка даты
    * @param format формат даты
    * @return дата
    */
   @Nullable
   private static synchronized Calendar stringToCal(String date, SimpleDateFormat format){
      if(date == null){
         return null;
      }
      else{
         try{
            Calendar cal = Calendar.getInstance();
            cal.setTime(Objects.requireNonNull(format.parse(date)));
            return cal;
         }
         catch (ParseException e){
            return null;
         }
      }
   }
}