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

import android.content.res.Resources;

import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

/**
 * Этот класс используется для описания единичной сущности расписания - урока.
 * Используется в ORM.
 * Содержит поля для даты, порядкового номера, номера(названия) кабинета,
 * времени проведения, группы, преподавателя, предмета.
 *
 * @author  Ипатов Никита
 */
@Entity(tableName = "tblSchedule", primaryKeys = {"lessonDate", "lessonNumber", "groupName", "subjectName"})
public class Lesson {
    @TypeConverters({DateConverters.class})
    @ColumnInfo(name="lessonDate")
    @NonNull
    public Calendar date;
    @ColumnInfo(name="lessonNumber")
    @NonNull
    public String lessonNumber;
    @ColumnInfo(name="roomNumber")
    public String roomNumber;
    @ColumnInfo(name="lessonTimes")
    public String times;
    @ColumnInfo(name="groupName")
    @NonNull
    public String groupName;
    @ColumnInfo(name="subjectName")
    @NonNull
    public String subject;
    @ColumnInfo(name="teacherName")
    public String teacher;

    public Lesson() {
        date = Calendar.getInstance();
    }

    public Lesson(@NonNull Calendar date, @NonNull String lessonNumber, @NonNull String roomNumber, String times,
                  @NonNull String groupName, @NonNull String subject, String teacher) {
        this.date = date;
        this.lessonNumber = lessonNumber;
        this.roomNumber = roomNumber;
        this.times = times;
        this.groupName = groupName;
        this.subject = subject;
        this.teacher = teacher;
    }

    @NonNull
    public Calendar getDate() {
        return date;
    }

    @NonNull
    public String getLessonNumber() {
        return lessonNumber;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getTimes() {
        return times;
    }

    public String getGroup() {
        return groupName;
    }

    @NonNull
    public String getSubject() {
        return subject;
    }

    public String getTeacher() {
        return teacher;
    }

    public void setDate(@NonNull Calendar date) {
        this.date = date;
    }

    public void setLessonNumber(@NonNull String lessonNumber) {
        this.lessonNumber = lessonNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public void setTimes(String times) {
        this.times = times;
    }

    public void setGroup(String groupName) {
        this.groupName = groupName;
    }

    public void setSubject(@NonNull String subject) {
        this.subject = subject;
    }

    public void setTeacher(String teacher) {
        this.teacher = teacher;
    }


    @NonNull
    @Override
    public String toString() {
        Resources resources = ScheduleApp.getInstance().getResources();
        String res = "";
        res = res + resources.getString(R.string.number) + ": " + lessonNumber + "\n";
        res = res + resources.getString(R.string.subject) + ": " + subject + "\n";
        if(!teacher.equals(""))
            res = res + resources.getString(R.string.teacher) + ": " + teacher + "\n";
        if(!roomNumber.equals(""))
            res = res + resources.getString(R.string.room) + ": " + roomNumber + "\n";
        return res;
    }
}