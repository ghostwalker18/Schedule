package com.example.schedule3;

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

    @Query("SELECT * FROM tblSchedule WHERE lessonDate = :date AND teacherName= :teacher")
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