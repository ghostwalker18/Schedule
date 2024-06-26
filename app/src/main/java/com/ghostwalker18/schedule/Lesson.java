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

import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.TypeConverters;

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
    @NonNull
    public String roomNumber;
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

    public Lesson(Calendar date, String lessonNumber, String roomNumber, String times,
                  String group, String subject, String teacher) {
        this.date = date;
        this.lessonNumber = lessonNumber;
        this.roomNumber = roomNumber;
        this.times = times;
        this.group = group;
        this.subject = subject;
        this.teacher = teacher;
    }
}