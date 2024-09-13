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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

import androidx.room.TypeConverter;

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования Calendar в String для БД и наоборот
 *
 * @author  Ипатов Никита
 */
public class DateConverters {
   private static final SimpleDateFormat dateFormatDB = new SimpleDateFormat("dd.MM.yyyy");
   private static final SimpleDateFormat dateFormatFirstCorpus = new SimpleDateFormat("dd.MM.yyyy");
   private static final SimpleDateFormat dateFormatSecondCorpus = new SimpleDateFormat("d MMMM yyyy");

   /**
    * Этот метод преобразует Calendar сущнисти в String для БД.
    *
    * @param date  the entity attribute value to be converted
    * @return
    */
   @TypeConverter
   static public String toString(Calendar date){
      return date == null ? null : dateFormatDB.format(date.getTime());
   }

   /**
    * Этот метод преобразует String из БД в Calendar сущности.
    *
    * @param date  the data from the database column to be
    *                converted
    * @return
    */
   @TypeConverter
   static public Calendar fromString(String date){
      return stringToCal(date, dateFormatDB);
   }

   /**
    * Этот метод преобразует String из расписания первого корпуса на Первомайском пр. в Calendar сущности.
    * @param date дата из расписания первого корпуса
    * @return преобразованная дата в формате Calendar
    */
   public Calendar convertFirstCorpusDate(String date){
      return stringToCal(date, dateFormatFirstCorpus);
   }

   /**
    * Этот метод преобразует String из расписания второго корпуса на Мурманской ул. в Calendar сущности.
    * @param date дата из расписания второго корпуса
    * @return преобразованная дата в формате Calendar
    */
   public Calendar convertSecondCorpusDate(String date){
      return stringToCal(date, dateFormatSecondCorpus);
   }

   /**
    * Этот метод используется для преобразования строки в дату согласно заданному формату.
    * @param date строка даты
    * @param format формат даты
    * @return дата
    */
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