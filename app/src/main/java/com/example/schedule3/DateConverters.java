package com.example.schedule3;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import androidx.room.TypeConverter;

public class DateConverters {
   private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
   @TypeConverter
   static public String toString(Calendar date){
      return date == null ? null : dateFormat.format(date.getTime());
   }

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
