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

import android.content.Intent;
import android.net.Uri;
import com.ghostwalker18.schedule.views.EditNoteActivity;
import com.ghostwalker18.schedule.models.Note;
import com.ghostwalker18.schedule.models.NotesRepository;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import io.appmetrica.analytics.AppMetrica;

/**
 * Этот класс используется для отслеживания изменений состояния редактируемой заметки.
 *
 * @author Ипатов Никита
 * @since 3.0
 * @see EditNoteActivity
 * @see NotesRepository
 * @see ScheduleRepository
 */
public class EditNoteModel
        extends ViewModel {
   private final ScheduleRepository scheduleRepository = ScheduleApp.getInstance().getScheduleRepository();
   private final NotesRepository notesRepository = ScheduleApp.getInstance().getNotesRepository();
   private final MediatorLiveData<Note> note = new MediatorLiveData<>(new Note());
   private final MediatorLiveData<String[]> noteThemesMediator = new MediatorLiveData<>();
   private LiveData<String[]> themes = new MutableLiveData<>();
   private final MutableLiveData<String> theme = new MutableLiveData<>("");
   private final MutableLiveData<String> text = new MutableLiveData<>("");
   private final MutableLiveData<List<Uri>> photoIDs = new MutableLiveData<>(new ArrayList<>());
   private final MutableLiveData<Calendar> date = new MutableLiveData<>(Calendar.getInstance());
   private final MutableLiveData<String> group = new MutableLiveData<>(scheduleRepository.getSavedGroup());
   private boolean isEdited = false;

   EditNoteModel(){
      super();
      noteThemesMediator.addSource(scheduleRepository.getSubjects(
              scheduleRepository.getSavedGroup()), noteThemesMediator::setValue);
   }

   /**
    * Этот метод позволяет задать id заметки для редактирования.
    * @param id идентификатор
    */
   public void setNoteID(Integer id){
      isEdited = true;
      note.addSource(notesRepository.getNote(id), note::setValue);
      note.observeForever(note1 -> {
         if(note1 != null){
            group.setValue(note1.group);
            date.setValue(note1.date);
            text.setValue(note1.text);
            theme.setValue(note1.theme);
            if(note1.photoIDs != null)
               photoIDs.setValue(note1.photoIDs);
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
      themes = scheduleRepository.getSubjects(group);
      noteThemesMediator.addSource(themes, noteThemesMediator::setValue);
   }

   /**
    * Этот метод позволяет получить группу заметки.
    * @return название группы
    */
   public LiveData<String> getGroup(){
      return group;
   }

   /**
    * Этот метод позволяет получить возможные группы для заметки.
    * @return список допустимых групп
    */
   public LiveData<String[]> getGroups(){
      return scheduleRepository.getGroups();
   }

   /**
    * Этот метод добавляет фотографию к заметке.
    * @param id идентификатор фотографии
    */
   public void addPhotoID(Uri id){
      List<Uri> currentUris = photoIDs.getValue();
      currentUris.add(id);
      photoIDs.setValue(currentUris);
   }

   /**
    * Этот метод убирает фотографию из заметки.
    * @param id идентификатор фотографии
    */
   public void removePhotoID(Uri id){
      List<Uri> currentUris = photoIDs.getValue();
      currentUris.remove(id);
      photoIDs.setValue(currentUris);
   }

   /**
    * Этот метод позволяет получить ID фотографий, прикрепленных к заметке.
    * @return идентификатор фотографии
    */
   public LiveData<List<Uri>> getPhotoIDs(){
      return photoIDs;
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
    * @return тема
    */
   public LiveData<String> getTheme(){
      return theme;
   }

   /**
    * Этот метод позволяет получить список предметов у данной группы в качестве тем.
    * @return список предлаагаемых тем
    */
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
   public void setDate(@NonNull Calendar date) {
      this.date.setValue(date);
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
         if(photoIDs.getValue() != null){
            noteToSave.photoIDs = photoIDs.getValue();
            try {
               for(Uri photoID : photoIDs.getValue()){
                  ScheduleApp.getInstance()
                          .getContentResolver()
                          .takePersistableUriPermission(photoID,
                          Intent.FLAG_GRANT_READ_URI_PERMISSION);
               }
            } catch (Exception ignored){/*Not required*/}
         }
         else
            noteToSave.photoIDs = null;
         if(isEdited)
            notesRepository.updateNote(noteToSave);
         else{
            if(ScheduleApp.getInstance().isAppMetricaActivated())
               AppMetrica.reportEvent("Добавлена заметка");

            notesRepository.saveNote(noteToSave);
         }
      }
   }
}