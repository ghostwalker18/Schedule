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

@Dao
@TypeConverters({DateConverters.class})
public interface LessonDao {
    @Query("SELECT DISTINCT teacherName FROM tblSchedule ORDER BY teacherName ASC")
    LiveData<String[]> getTeachers();

    @Query("SELECT DISTINCT groupName FROM tblSchedule ORDER BY groupName ASC")
    LiveData<String[]> getGroups();

    @Query("SELECT * FROM tblSchedule WHERE lessonDate = :date AND groupName= :group AND teacherName = :teacher")
    LiveData<Lesson[]> getLessonsForGroupWithTeacher(Calendar date, String group, String teacher);

    @Query("SELECT * FROM tblSchedule WHERE lessonDate = :date AND groupName= :group")
    LiveData<Lesson[]> getLessonsForGroup(Calendar date, String group);

    @Query("SELECT * FROM tblSchedule WHERE lessonDate = :date AND teacherName LIKE :teacher")
    LiveData<Lesson[]> getLessonsForTeacher(Calendar date, String teacher);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<Long> insert(Lesson lesson);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertMany(Lesson... lessons);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    ListenableFuture<List<Long>> insertMany(List<Lesson> lessons);

    @Update
    ListenableFuture<Integer> update(Lesson lesson);
}