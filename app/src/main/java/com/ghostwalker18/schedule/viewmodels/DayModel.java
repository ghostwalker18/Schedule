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

package com.ghostwalker18.schedule.viewmodels;

import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.models.Lesson;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Этот класс представляет собой модель представления фрагмента расписания для определенного дня недели.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public class DayModel
        extends ViewModel {
   private final ScheduleRepository repository = ScheduleApp.getInstance().getScheduleRepository();
   private final MutableLiveData<Calendar> date = new MutableLiveData<>();
   private final MediatorLiveData<Lesson[]> lessons = new MediatorLiveData<>();
   private LiveData<Lesson[]> lessonsMediator;
   private String group;
   private String teacher;

   public void setDate(Calendar date){
      this.date.setValue(date);
      revalidateLessons();
   }

   public LiveData<Calendar> getDate(){
      return date;
   }

   public void setGroup(String group){
      this.group = group;
      revalidateLessons();
   }

   public void setTeacher(String teacher){
      this.teacher = teacher;
      revalidateLessons();
   }

   public LiveData<Lesson[]> getLessons(){
      return lessons;
   }

   private void revalidateLessons(){
      if(lessonsMediator != null)
         lessons.removeSource(lessonsMediator);
      lessonsMediator = repository.getLessons(group, teacher, date.getValue());
      lessons.addSource(lessonsMediator, lessons::setValue);
   }
}