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

import android.content.Context;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Этот класс используется Room для генерации класса для ORM операций с БД приложения.
 *
 * @author  Ипатов Никита
 * @since 1.0
 * @see LessonDao
 * @see NoteDao
 */
@Database(entities = {Lesson.class, Note.class}, version = 4, exportSchema = false)
public abstract class AppDatabase
        extends RoomDatabase {
    private final static String DATABASE_NAME = "database";
    public abstract LessonDao lessonDao();
    public abstract NoteDao noteDao();

    /**
     * Этот метод позволяет получить сконфигурированную базу данных приложения.
     * @param context контекс приложения
     * @return база данных Room
     */
    public static AppDatabase getInstance(Context context){
        Callback callback =  new RoomDatabase.Callback(){
            /**
             * Этот метод создает триггер при создании бд.
             * @param db создаваемая бд
             */
            @Override
            public void onCreate(@NonNull SupportSQLiteDatabase db) {
                super.onCreate(db);
                db.execSQL(UPDATE_DAY_TRIGGER_1);
                db.execSQL(UPDATE_DAY_TRIGGER_2);
            }
        };
        Builder<AppDatabase> builder = Room
                .databaseBuilder(context, AppDatabase.class, DATABASE_NAME)
                .addCallback(callback);
        for(Migration migration : DataBaseMigrations.getMigrations())
            builder.addMigrations(migration);
        return builder.build();
    }

    public static File exportDBFile(Context context){
        return context.getDatabasePath(DATABASE_NAME);
    }

    public static void importDBFile(File dbFile){
        
    }

    public static final String UPDATE_DAY_TRIGGER_1 =
            "CREATE TRIGGER IF NOT EXISTS update_day_stage1 " +
            "BEFORE INSERT ON tblSchedule " +
            "BEGIN " +
            "DELETE FROM tblSchedule WHERE groupName = NEW.groupName AND " +
            "                lessonDate = NEW.lessonDate AND " +
            "                lessonNumber = NEW.lessonNumber;" +
            "END;";

    public static final String UPDATE_DAY_TRIGGER_2 =
            "CREATE TRIGGER IF NOT EXISTS update_day_stage2 " +
                    "AFTER INSERT ON tblSchedule " +
                    "BEGIN " +
                    "DELETE FROM tblSchedule WHERE subjectName = '';"+
                    "END;";
}