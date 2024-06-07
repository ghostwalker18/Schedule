package com.example.schedule3;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = Lesson.class, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LessonDao lessonDao();
}
