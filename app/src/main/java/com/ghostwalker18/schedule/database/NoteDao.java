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

package com.ghostwalker18.schedule.database;

import com.ghostwalker18.schedule.converters.DateConverters;
import com.ghostwalker18.schedule.models.Note;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Calendar;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

/**
 * Интерфейс DAO для работы с таблицой БД, содержащей сведения о заметках к занятиям.
 * Используется Room для генерации.
 *
 * @author  Ипатов Никита
 * @since 3.0
 * @see Note
 */
@Dao
@TypeConverters({DateConverters.class})
public interface NoteDao {

    /**
     * Этот метод позволяет получить заметку из базы данных по ее ID.
     * @param id идентификатор заметки
     * @return заметка
     */
    @Query("SELECT * FROM tblNote WHERE id = :id")
    LiveData<Note> getNote(Integer id);

    /**
     * Этот метод позволяет получить заметки для заданной группы и дня.
     * @param date день
     * @param group группа
     * @return список заметок
     */
    @Query("SELECT * FROM tblNote WHERE noteDate = :date AND noteGroup = :group")
    LiveData<Note[]> getNotes(Calendar date, String group);

    /**
     * Этот метод позволяет получить заметки для заданных группы и дней.
     * @param dates дни
     * @param group группа
     * @return список заметок
     */
    @Query("SELECT * FROM tblNote WHERE noteDate IN (:dates) AND noteGroup = :group " +
            "ORDER BY noteDate")
    LiveData<Note[]> getNotesForDays(Calendar[] dates, String group);

    /**
     * Этот метод позволяет получить заметки, содержащие в теме или тексте заданное слова.
     * @param keyword ключевое слово
     * @return список заметок
     */
    @Query("SELECT * FROM tblNote WHERE (noteText LIKE '%' || :keyword || '%' OR " +
            "noteTheme LIKE '%' || :keyword || '%') AND noteGroup = :group " +
            "ORDER BY noteDate DESC")
    LiveData<Note[]> getNotesByKeyword(String keyword, String group);

    /**
     * Этот метод позволяет синхронно получить все содержимое заметок (например, для экспорта).
     * @return все содержимое tblNote
     */
    @Query("SELECT * FROM tblNote")
    List<Note> getAllNotesSync();

    /**
     * Этот метод позволяет синхронно удалить все содержимое tblNote
     */
    @Query("DELETE FROM tblNote")
    int deleteAllNotesSync();

    /**
     * Этот метод позволяет внести заметку в БД.
     * @param note заметка
     * @return количество внесенных изменений
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insert(Note note);

    /**
     * Этот метод позволяет синхронно вставить элементы Note в БД.
     * @param notes заметки
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertManySync(List<Note> notes);

    /**
     * Этот метод позволяет обновить заметку из БД.
     * @param note заметка
     * @return количество внесенных изменений
     */
    @Update
    ListenableFuture<Integer> update(Note note);

    /**
     * Этот метод позволяет удалить заметку из БД.
     * @param note заметка
     * @return количество внесенных изменений
     */
    @Delete
    ListenableFuture<Integer> delete(Note note);
}