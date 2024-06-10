package com.example.schedule3;

import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

@Entity(tableName = "tblSchedule", primaryKeys = {"lessonDate", "roomNumber", "lessonNumber"})
public class Lesson {
    @TypeConverters({DateConverters.class})
    @ColumnInfo(name="lessonDate")
    @NonNull
    public Calendar date;
    @ColumnInfo(name="lessonNumber")
    @NonNull
    public String lessonNumber;
    @ColumnInfo(name="roomNumber")
    @NonNull
    public int roomNumber;
    @ColumnInfo(name="lessonTimes")
    @NonNull
    public String times;
    @ColumnInfo(name="groupName")
    @NonNull
    public String group;
    @ColumnInfo(name="subjectName")
    @NonNull
    public String subject;
    @ColumnInfo(name="teacherName")
    public String teacher;

    public Lesson() {
        date = Calendar.getInstance();
    }

    public Lesson(Calendar date, String lessonNumber, int roomNumber, String times,
                  String group, String subject, String teacher){
        this.date = date;
        this.lessonNumber = lessonNumber;
        this.roomNumber = roomNumber;
        this.times = times;
        this.group = group;
        this.subject = subject;
        this.teacher = teacher;
    }
}
