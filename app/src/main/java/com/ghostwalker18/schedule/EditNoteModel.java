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

import android.content.Intent;
import android.net.Uri;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Этот класс используется для отслеживания изменений состояния редактируемой заметки.
 *
 * @author Ипатов Никита
 * @since 3.0
 */
public class EditNoteModel
        extends ViewModel {
   private final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
   private final MediatorLiveData<Note> note = new MediatorLiveData<>(new Note());
   private final MediatorLiveData<String[]> noteThemesMediator = new MediatorLiveData<>();
   private LiveData<String[]> themes = new MutableLiveData<>();
   private final MutableLiveData<String> theme = new MutableLiveData<>("");
   private final MutableLiveData<String> text = new MutableLiveData<>("");
   private final MutableLiveData<Uri> photoID = new MutableLiveData<>();
   private final MutableLiveData<Calendar> date = new MutableLiveData<>();
   private final MutableLiveData<String> group = new MutableLiveData<>();
   private boolean isEdited = false;

   /**
    * Этот метод позволяет задать ID заметки для редактирования.
    * @param ID идентификатор
    */
   public void setNoteID(Integer ID){
      isEdited = true;
      note.addSource(repository.getNote(ID), note::setValue);
      note.observeForever(note1 -> {
         if(note1 != null){
            group.setValue(note1.group);
            date.setValue(note1.date);
            text.setValue(note1.text);
            theme.setValue(note1.theme);
            if(note1.photoID != null)
               photoID.setValue(Uri.parse(note1.photoID));
         }
      });
   }

   /**
    * Этот метод позволяет задать группу для заметки.
    * @param group группа
    */
   public void setGroup(String group){
      this.group.setValue(group);
      noteThemesMediator.removeSource(themes);
      themes = repository.getSubjects(group);
      noteThemesMediator.addSource(themes, noteThemesMediator::setValue);
   }

   /**
    * Этот метод позволяет получить группу заметки.
    * @return
    */
   public LiveData<String> getGroup(){
      return group;
   }

   /**
    * Этот метод позволяет получить возможные группы для заметки.
    * @return список допустимых групп
    */
   public LiveData<String[]> getGroups(){
      return repository.getGroups();
   }

   /**
    * Этот метод позволяет задать ID фотографии, прикрепляемой к заметке.
    * @param id uri фотографии
    */
   public void setPhotoID(Uri id){
      photoID.setValue(id);
   }

   /**
    * Этот метод позволяет получить ID фотографии, прикрепленной к заметке.
    * @return
    */
   public LiveData<Uri> getPhotoID(){
      return photoID;
   }

   /**
    * Этот метод позволяет задать текст заметки.
    *
    * @param text текст
    */
   public void setText(String text){
      this.text.setValue(text);
   }

   /**
    * Этот метод позволяет получить текст заметки.
    *
    * @return текст
    */
   public LiveData<String> getText(){
      return text;
   }

   /**
    * Этот метод позволяет задать тему заметки.
    *
    * @param theme тема
    */
   public void setTheme(String theme){
      this.theme.setValue(theme);
   }

   /**
    * Этот метод позволяет получить тему заметки.
    * @return
    */
   public LiveData<String> getTheme(){
      return theme;
   }

   public LiveData<String[]> getThemes(){
      return noteThemesMediator;
   }

   /**
    * Этот метод позволяет получить текущую дату редактируемой заметки.
    *
    * @return дата
    */
   public LiveData<Calendar> getDate(){
      return date;
   }

   /**
    * Этот метод позволяет установить дату редактируемой заметки.
    *
    * @param date дата
    */
   public void setDate(Calendar date) {
      this.date.setValue(date);
   }

   public Integer getNoteID(){
      return note.getValue().id;
   }

   /**
    * Этот метод позволяет сохранить заметку.
    */
   public void saveNote(){
      Note noteToSave = note.getValue();
      if(noteToSave != null){
         noteToSave.date = date.getValue();
         noteToSave.group = group.getValue();
         noteToSave.theme = theme.getValue();
         noteToSave.text = text.getValue();
         if(photoID.getValue() != null){
            noteToSave.photoID = photoID.getValue().toString();
            try {
               ScheduleApp.getInstance().getContentResolver().takePersistableUriPermission(photoID.getValue(),
                       Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (Exception ignored){};
         }
         if(isEdited)
            repository.updateNote(noteToSave);
         else
            repository.saveNote(noteToSave);
      }
   }
}