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
import java.util.Date;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class ScheduleState extends ViewModel{
   private MutableLiveData<String> group = new MutableLiveData<>();
   private MutableLiveData<String> teacher = new MutableLiveData<>();
   private MutableLiveData<Calendar> calendar = new MutableLiveData<>(
           new Calendar.Builder()
                   .setInstant(new Date())
                   .build());

   public void goNextWeek(){
      Calendar date = calendar.getValue();
      date.add(Calendar.WEEK_OF_YEAR, 1);
      calendar.setValue(date);
   }

   public void goPreviousWeek(){
      Calendar date = calendar.getValue();
      date.add(Calendar.WEEK_OF_YEAR, -1);
      calendar.setValue(date);
   }

   public LiveData<Calendar> getCalendar(){
      return calendar;
   }

   public int getYear(){
      return calendar.getValue().get(Calendar.YEAR);
   }

   public int getWeek(){
      return calendar.getValue().get(Calendar.WEEK_OF_YEAR);
   }

   public void setGroup(String group){
      this.group.setValue(group);
   };

   public LiveData<String> getGroup(){
      return group;
   }

   public void setTeacher(String teacher){
      this.teacher.setValue(teacher);
   }

   public LiveData<String> getTeacher(){
      return teacher;
   }

   @Override
   protected void onCleared() {
      super.onCleared();
   }
}