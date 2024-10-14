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
    @PrimaryKey()
    public long id;
    @TypeConverters({DateConverters.class})
    @ColumnInfo(name="noteDate")
    public Calendar date;
    @ColumnInfo(name="noteGroup")
    public String group;
    @ColumnInfo(name="noteTheme")
    public String theme;
    @ColumnInfo(name="noteText")
    public String text;
    @ColumnInfo(name="notePhotoID")
    public String photoID;
}