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

import java.util.Calendar;
import androidx.annotation.Nullable;

/**
 * В этом классе содержаться различные вспомогательные методы, использующиеся по всему приложению.
 *
 * @author Ипатов Никита
 * @since 3.2
 */
public class Utils {
   enum LessonAvailability{
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
   static LessonAvailability isLessonAvailable(Calendar lessonDate, String lessonTimes){
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

         String e = end.getTime().toString();
         String s = start.getTime().toString();
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
   static boolean isDateToday(Calendar date){
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
   static String generateDateForTitle(Calendar date){
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
}