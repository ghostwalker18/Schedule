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
public class NotesModel extends ViewModel {
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private String group;
   private Calendar startDate = Calendar.getInstance();
   private Calendar endDate = Calendar.getInstance();
   private LiveData<Note[]> notes = new MutableLiveData<>();

   public LiveData<Note[]> getNotes(){
      return notes;
   };

   public void setGroup(String group){
      this.group = group;
      notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   }
   public void setStartDate(Calendar date){
      this.startDate = date;
      notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   };

   public void setEndDate(Calendar date){
      this.endDate = date;
      notes = repository.getNotes(group, generateDateSequence(startDate, endDate));
   };

   private Calendar[] generateDateSequence(Calendar startDate, Calendar endDate){
      if(startDate == endDate || endDate.before(startDate))
         return new Calendar[]{startDate};
      List<Calendar> resultList = new ArrayList<>();
      while(startDate.before(endDate)){
         resultList.add(startDate);
         startDate.add(Calendar.DATE, 1);
      }
      Calendar[] result = new Calendar[resultList.size()];
      resultList.toArray(result);
      return result;
   }
}