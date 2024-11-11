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
import java.util.Collection;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * Этот класс представляет репозиторий данных приложения о заметках.
 *
 * @author Ипатов Никита
 * @since 3.1
 */
public class NotesRepository {
   private final AppDatabase db;

   public NotesRepository(AppDatabase db){
      this.db = db;
   }

   /**
    * Этот метод позволяет сохранить заметку.
    */
   public void saveNote(@NonNull Note note){
      db.noteDao().insert(note);
   }

   /**
    * Этот метод позволяет обновить заметку.
    *
    * @param note заметка
    */
   public void updateNote(@NonNull Note note){
      db.noteDao().update(note);
   }

   /**
    * Этот метод позволяет получить заметку по ее ID.
    *
    * @param id первичный ключ
    * @return заметка
    */
   public LiveData<Note> getNote(@NonNull Integer id) {
      return db.noteDao().getNote(id);
   }

   /**
    * Этот метод позволяет получить заметки для заданных группы и временного промежутка.
    *
    * @param group группа
    * @param dates список дат для выдачи
    * @return заметки
    */
   public LiveData<Note[]> getNotes(@NonNull String group, @NonNull Calendar[] dates){
      if(dates.length == 1)
         return db.noteDao().getNotes(dates[0], group);
      return db.noteDao().getNotesForDays(dates, group);
   }

   /**
    * Этот метод позволяет получить заметки для заданного ключевого слова и группы.
    *
    * @param group группа
    * @param keyword ключевое слово
    * @return список заметок
    */
   public LiveData<Note[]> getNotes(@NonNull String group, @NonNull String keyword){
      return db.noteDao().getNotesByKeyword(keyword, group);
   }

   /**
    * Этот метод позволяет удалить выбранные заметки из БД.
    *
    * @param notes заметки для удаления
    */
   public void deleteNotes(@NonNull Collection<Note> notes){
      for(Note note : notes)
         db.noteDao().delete(note);
   }
}