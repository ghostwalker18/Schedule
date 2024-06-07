package com.example.schedule3;

import java.util.List;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

@Dao
public interface LessonDao {
    @Query("SELECT DISTINCT teacher FROM lesson ORDER BY teacher ASC")
    List<String> getGroups();

    @Query("SELECT * FROM lesson WHERE date = :date AND group = :group AND teacher = :teacher")
    List<Lesson> getLessons(String date, String group, String teacher);

    @Insert
    void insert(Lesson lesson);

    @Insert
    void insertMany(Lesson... lessons);

    @Insert
    void insertMany(List<Lesson> lessons);

    @Update
    void update(Lesson lesson);
}
