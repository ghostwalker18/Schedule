package com.example.schedule3;

import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"date", "number", "room"})
public class Lesson {
    @ColumnInfo(name="date")
    public String date;
    @ColumnInfo(name="number")
    public int number;
    @ColumnInfo(name="room")
    public int room;
    @ColumnInfo(name="times")
    public String times;
    @ColumnInfo(name="group")
    public String group;
    @ColumnInfo(name="subject")
    public String subject;
    @ColumnInfo(name="teacher")
    public String teacher;
}
