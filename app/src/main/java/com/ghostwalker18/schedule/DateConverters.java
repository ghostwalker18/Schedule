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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import androidx.room.TypeConverter;

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования Calendar в String для БД и наоборот
 */
public class DateConverters {
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");

   /**
    * Этот метод преобразует Calendar сущнисти в String для БД.
    * @param date  the entity attribute value to be converted
    * @return
    */
   @TypeConverter
   static public String toString(Calendar date){
      return date == null ? null : dateFormat.format(date.getTime());
   }

   /**
    * Этот метод преобразует String из БД в Calendar сущности.
    * @param date  the data from the database column to be
    *                converted
    * @return
    */
   @TypeConverter
   static public Calendar fromString(String date){
      if(date == null){
         return null;
      }
      else{
         try{
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateFormat.parse(date));
            return cal;
         }
         catch (java.text.ParseException e){
            return null;
         }
      }
   }
}