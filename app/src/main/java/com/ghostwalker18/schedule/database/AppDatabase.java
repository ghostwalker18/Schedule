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

package com.ghostwalker18.schedule.database;

import android.content.Context;
import com.ghostwalker18.schedule.models.Lesson;
import com.ghostwalker18.schedule.models.Note;
import java.io.File;
import java.util.List;
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
@Database(entities = {Lesson.class, Note.class}, version = 5, exportSchema = false)
public abstract class AppDatabase
        extends RoomDatabase {
    public abstract LessonDao lessonDao();
    public abstract NoteDao noteDao();
    private final static String APP_DATABASE_NAME = "database";
    private final static String EXPORT_DATABASE_NAME = "export_database.db";
    private final static String IMPORT_DATABASE_NAME = "import_database.db";
    private static volatile AppDatabase instance;

    /**
     * Этот метод позволяет получить сконфигурированную базу данных приложения по умолчанию.
     * @param context контекст приложения
     * @return база данных Room
     */
    @NonNull
    public static AppDatabase getInstance(final Context context){
        if(instance == null){
            synchronized (AppDatabase.class){
                if(instance == null){
                    instance = createAppDatabase(context, APP_DATABASE_NAME, null);
                }
            }
        }
        return instance;
    }

    /**
     * Этот метод позволяет получить архивированные файлы БД приложения для ее экспорта.
     * @param context контекст приложения
     * @return файл БД приложения
     */
    public File exportDBFile(@NonNull Context context, @NonNull String dataType){
        AppDatabase exportDB = createAppDatabase(context, EXPORT_DATABASE_NAME, null);
        exportDB.lessonDao().deleteAllLessonsSync();
        exportDB.noteDao().deleteAllNotesSync();
        if(dataType.equals("schedule") || dataType.equals("schedule_and_notes")){
            List<Lesson> lessons = instance.lessonDao().getAllLessonsSync();
            exportDB.lessonDao().insertManySync(lessons);
        }
        if(dataType.equals("notes") || dataType.equals("schedule_and_notes")){
            List<Note> notes = instance.noteDao().getAllNotesSync();
            exportDB.noteDao().insertManySync(notes);
        }
        exportDB.close();
        return context.getDatabasePath(EXPORT_DATABASE_NAME);
    }

    /**
     * Этот метод заменяет файлы БД приложения импортированными из стороннего источника.
     * @param dbFile архив с файлами БД
     */
    public void importDBFile(@NonNull Context context, File dbFile,
                             @NonNull String dataType, String importPolicy){
        AppDatabase importDB = createAppDatabase(context, IMPORT_DATABASE_NAME, dbFile);
        if(dataType.equals("schedule") || dataType.equals("schedule_and_notes")){
            if(importPolicy.equals("replace"))
                instance.lessonDao().deleteAllLessonsSync();
            List<Lesson> lessons = importDB.lessonDao().getAllLessonsSync();
            instance.lessonDao().insertManySync(lessons);
        }
        if(dataType.equals("notes") || dataType.equals("schedule_and_notes")){
            if(importPolicy.equals("replace"))
                instance.noteDao().deleteAllNotesSync();
            List<Note> notes = importDB.noteDao().getAllNotesSync();
            instance.noteDao().insertManySync(notes);
        }
        importDB.close();
        context.getDatabasePath(IMPORT_DATABASE_NAME).delete();
    }

    /**
     * Этот метод используется чтобы удалить файл экспортной БД после завершения эскспорта.
     * @param context контекст приложения
     */
    public static void deleteExportDB(@NonNull Context context){
        File exportDBFile = context.getDatabasePath(EXPORT_DATABASE_NAME);
        if(exportDBFile != null && exportDBFile.exists())
            exportDBFile.delete();
    }

    /**
     * Этот метод позволяет получить сконфигурированную базу данных приложения.
     * @param context контекст приложения
     * @param databaseName имя БД
     * @return база данных Room
     */
    @NonNull
    private static AppDatabase createAppDatabase(@NonNull Context context,
                                                 @NonNull String databaseName,
                                                 File file){
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
                .databaseBuilder(context, AppDatabase.class, databaseName)
                .setJournalMode(JournalMode.TRUNCATE)
                .addCallback(callback);
        for(Migration migration : DataBaseMigrations.getMigrations())
            builder.addMigrations(migration);
        if(file != null && file.exists())
            builder.createFromFile(file);
        return builder.build();
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