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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Этот класс служит для отслеживания изменений состояния списка заметок, отображаемого пользователю.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class NotesModel
        extends ViewModel {
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private String group;
   private Calendar startDate;
   private Calendar endDate;
   private LiveData<Note[]> notes = new MutableLiveData<>();

   /**
    * Этот метод выдает заметки для заданнной группы и временного интервала.
    * @return список заметок
    */
   public LiveData<Note[]> getNotes(){
      return notes;
   }

   /**
    * Этот метод задает группу для выдачи заметок.
    * @param group группа
    */
   public void setGroup(String group){
      this.group = group;
      if(startDate != null && endDate != null)
         notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   }

   /**
    * Этот метод устанавливает начальную дату временного интервала выдачи заметок.
    * @param date начальная дата
    */
   public void setStartDate(Calendar date){
      this.startDate = date;
      if(startDate != null && endDate != null)
         notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   }

   /**
    * Этот метод устанавливает конечную дату временного интервала выдачи заметок.
    * @param date конечная дата
    */
   public void setEndDate(Calendar date){
      this.endDate = date;
      if(startDate != null && endDate != null)
         notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   }

   /**
    * Этот метод позволяет получить последовательность дат, основываясь на начальной и конечной.
    * @param startDate начальная дата (включается в интервал)
    * @param endDate конечная дата (включается в интервал)
    * @return массив дат
    */
   private Calendar[] generateDateSequence(Calendar startDate, Calendar endDate){
      if(startDate.equals(endDate) || endDate.before(startDate))
         return new Calendar[]{startDate};
      List<Calendar> resultList = new ArrayList<>();
      while(startDate.before(endDate)){
         Calendar date = (Calendar) startDate.clone();
         resultList.add(date);
         startDate.add(Calendar.DATE, 1);
      }
      Calendar[] result = new Calendar[resultList.size()];
      resultList.toArray(result);
      return result;
   }
}