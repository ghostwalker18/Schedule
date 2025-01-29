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
import com.ghostwalker18.schedule.models.Lesson;
import com.google.common.util.concurrent.ListenableFuture;
import java.util.Calendar;
import java.util.List;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.TypeConverters;
import androidx.room.Update;

/**
 * Интерфейс DAO для работы с таблицой БД, содержащей сведения о занятиях.
 * Используется Room для генерации.
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see Lesson
 */
@Dao
@TypeConverters({DateConverters.class})
public interface LessonDao {

    /**
     * Этот метод позволяет получить список учителей из БД.
     * @return списко учителей
     */
    @Query("SELECT DISTINCT teacherName FROM tblSchedule ORDER BY teacherName ASC")
    LiveData<String[]> getTeachers();

    /**
     * Этот метод позволяет получить список групп из БД.
     * @return список групп
     */
    @Query("SELECT DISTINCT groupName FROM tblSchedule ORDER BY groupName ASC")
    LiveData<String[]> getGroups();

    /**
     * Этот метод позволяет получить список занятий на заданную дату у заданной группы,
     * которые проводит заданный преподаватель.
     * @param date дата
     * @param group группа
     * @param teacher преподаватель
     * @return список занятий
     */
    @Query("SELECT * FROM tblSchedule " +
            "WHERE lessonDate = :date AND groupName= :group AND teacherName LIKE '%' || :teacher || '%' " +
            "ORDER BY lessonTimes")
    LiveData<Lesson[]> getLessonsForGroupWithTeacher(Calendar date, String group, String teacher);

    /**
     * Этот метод позволяет получить список занятий на заданный день у заданной группы.
     * @param date дата
     * @param group группа
     * @return список занятий
     */
    @Query("SELECT * FROM tblSchedule WHERE lessonDate = :date AND groupName= :group " +
            "ORDER BY lessonTimes")
    LiveData<Lesson[]> getLessonsForGroup(Calendar date, String group);

    /**
     * Этот метод позволяет получить список занятий на заданный день у заданного преподавателя.
     * @param date дата
     * @param teacher преподаватель
     * @return список занятий
     */
    @Query("SELECT * FROM tblSchedule " +
            "WHERE lessonDate = :date AND teacherName LIKE '%' || :teacher || '%' " +
            "ORDER BY lessonTimes")
    LiveData<Lesson[]> getLessonsForTeacher(Calendar date, String teacher);

    /**
     * Этот метод позволяет получить список всех предметов у группы.
     * @param group название группы
     * @return список предметов
     */
    @Query("SELECT DISTINCT subjectName FROM tblSchedule WHERE groupName = :group " +
            "ORDER BY subjectName ASC")
    LiveData<String[]> getSubjectsForGroup(String group);

    /**
     * Этот метод позволяет получить последнюю дату,
     * для которой для заданной группы указано расписание.
     * @param group группа
     * @return последняя дата, для которой существует расписание
     */
    @Query("SELECT MAX(lessonDate) FROM tblSchedule WHERE groupName =:group")
    Calendar getLastKnownLessonDate(String group);

    /**
     * Этот метод позволяет вставить элементы Lesson в БД.
     * @param lessons занятия
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertMany(List<Lesson> lessons);

    /**
     * Этот метод позволяет обновить элемент Lesson В БД.
     * @param lesson занятие
     */
    @Update
    ListenableFuture<Integer> update(Lesson lesson);
}