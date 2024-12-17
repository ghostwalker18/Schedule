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
import android.net.Uri;

import java.util.ArrayList;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

/**
 * Этот класс используется для описания единичной сущности заметок.
 * Используется в ORM.
 * Содержит поля для даты, группы, темы, текста, идентификатора фото.
 *
 * @author  Ипатов Никита
 * @since 3.0
 */
@Entity(tableName = "tblNote")
public class Note {
    @PrimaryKey(autoGenerate = true)
    @NonNull
    public Integer id;
    @TypeConverters({DateConverters.class})
    @ColumnInfo(name="noteDate")
    @NonNull
    public Calendar date;
    @NonNull
    @ColumnInfo(name="noteGroup")
    public String group;
    @ColumnInfo(name="noteTheme")
    public String theme;
    @ColumnInfo(name="noteText")
    @NonNull
    public String text;
    @TypeConverters({PhotoURIArrayConverters.class})
    @ColumnInfo(name="notePhotoID")
    public ArrayList<Uri> photoIDs;

    @Override
    public String toString(){
        String res = "";
        Resources resources = ScheduleApp.getInstance().getResources();
        res = res + resources.getString(R.string.date) + ": " + DateConverters.toString(date) + "\n";
        res = res + resources.getString(R.string.group) + ": " + group + "\n";
        res = res + resources.getString(R.string.theme) + ": " + theme + "\n";
        res = res + resources.getString(R.string.text) + ": " + text + "\n";
        return res;
    }
}