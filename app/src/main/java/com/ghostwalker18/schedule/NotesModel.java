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
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Этот класс служит для отслеживания изменений состояния списка заметок, отображаемого пользователю.
 *
 * @author Ипатов Никита
 * @since 3.0
 * @see NotesActivity
 */
public class NotesModel
        extends ViewModel {
   private final NotesRepository repository = ScheduleApp.getInstance().getNotesRepository();
   private final MediatorLiveData<Note[]> notes = new MediatorLiveData<>();
   private final MutableLiveData<Calendar> startDate = new MutableLiveData<>(Calendar.getInstance());
   private final MutableLiveData<Calendar> endDate = new MutableLiveData<>(Calendar.getInstance());
   private String group;
   private String keyword;
   private LiveData<Note[]> notesMediator = new MutableLiveData<>();

   NotesModel(){
      super();
      setGroup(ScheduleApp.getInstance().getScheduleRepository().getSavedGroup());
   }
   /**
    * Этот метод выдает заметки для заданнной группы и временного интервала.
    * @return список заметок
    */
   public LiveData<Note[]> getNotes(){
      return notes;
   }

   /**
    * Этот метод позволяет получить стартовую дату временного интервала для выдачи заметок.
    * @return стратовая дата
    */
   public LiveData<Calendar> getStartDate(){
      return startDate;
   }

   /**
    * Этот метод позволяет получить конечную дату временного интервала для выдачи заметок.
    * @return конечная дата
    */
   public LiveData<Calendar> getEndDate(){
      return endDate;
   }

   /**
    * Этот метод задает группу для выдачи заметок.
    * @param group группа
    */
   public void setGroup(String group){
      this.group = group;
      notes.removeSource(notesMediator);
      if(group != null){
         if(keyword != null)
            notesMediator = repository.getNotes(group, keyword);
         if(startDate.getValue() != null && endDate.getValue() != null)
            notesMediator = repository.getNotes(group,
                    generateDateSequence(startDate.getValue(), endDate.getValue()));
      }
      notes.addSource(notesMediator, notes::setValue);
   }

   /**
    * Этот метод позволяет получить группу для выдачи заметок
    * @return группа
    */
   public String getGroup(){
      return group;
   }

   /**
    * Этот метод задает ключевое слова для поиска заметок по нему и выдачи их.
    * @param keyword ключевое слово
    */
   public void setKeyword(String keyword){
      this.keyword = keyword;
      notes.removeSource(notesMediator);
      if(keyword != null)
         notesMediator = repository.getNotes(group, keyword);
      else {
         if(startDate.getValue() != null && endDate.getValue() != null && group != null)
            notesMediator = repository.getNotes(group,
                    generateDateSequence(startDate.getValue(), endDate.getValue()));
      }
      notes.addSource(notesMediator, notes::setValue);
   }

   /**
    * Этот метод устанавливает начальную дату временного интервала выдачи заметок.
    * @param date начальная дата
    */
   public void setStartDate(Calendar date){
      this.startDate.setValue(date);
      notes.removeSource(notesMediator);
      if(startDate.getValue() != null && endDate.getValue() != null && group != null)
         notesMediator = repository.getNotes(group,
                 generateDateSequence(startDate.getValue(), endDate.getValue()));
      notes.addSource(notesMediator, notes::setValue);
   }

   /**
    * Этот метод устанавливает конечную дату временного интервала выдачи заметок.
    * @param date конечная дата
    */
   public void setEndDate(Calendar date){
      this.endDate.setValue(date);
      notes.removeSource(notesMediator);
      if(startDate.getValue() != null && endDate.getValue() != null && group != null)
         notesMediator = repository.getNotes(group,
                 generateDateSequence(startDate.getValue(), endDate.getValue()));
      notes.addSource(notesMediator, notes::setValue);
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
      //remember of reference nature of Java
      Calendar counter = (Calendar) startDate.clone();
      while(counter.before(endDate)){
         //remember of reference nature of Java (also here)
         Calendar date = (Calendar) counter.clone();
         resultList.add(date);
         counter.add(Calendar.DATE, 1);
      }
      Calendar[] result = new Calendar[resultList.size()];
      resultList.toArray(result);
      return result;
   }
}