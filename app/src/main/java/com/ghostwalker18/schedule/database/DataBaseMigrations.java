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

import java.util.Arrays;
import java.util.Collection;
import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

/**
 * Этот класс используется для проведения миграций между версиями БД приложения.
 *
 * @author Ипатов Никита
 * @since 4.0
 */
public class DataBaseMigrations {
   public static final Migration MIGRATION_1_2 = new Migration(1, 2){
      @Override
      public void migrate(@NonNull SupportSQLiteDatabase db){
         db.execSQL("CREATE TABLE IF NOT EXISTS tblNote ( 'noteGroup' TEXT NOT NULL, " +
                 "'noteTheme' TEXT, 'noteText' TEXT NOT NULL, 'notePhotoID' TEXT, " +
                 "'id' INTEGER NOT NULL, 'noteDate' TEXT NOT NULL, PRIMARY KEY(`id`))");
      }
   };

   public static final Migration MIGRATION_2_3 = new Migration(2, 3){
      @Override
      public void migrate(@NonNull SupportSQLiteDatabase db){
         db.execSQL("DROP TRIGGER IF EXISTS update_day_stage1");
         db.execSQL(AppDatabase.UPDATE_DAY_TRIGGER_1);
      }
   };

   public static final Migration MIGRATION_3_4 = new Migration(3, 4) {
      @Override
      public void migrate(@NonNull SupportSQLiteDatabase db) {
         db.execSQL("UPDATE tblNote " +
                 "SET notePhotoID = '[\"' || notePhotoID || '\"]'");
         db.execSQL("ALTER TABLE tblNote " +
                 "RENAME COLUMN notePhotoID TO notePhotoIDs");
      }
   };

   /**
    * Этот метод возвращает список всех миграций БД приложения
    * @return миграции приложения между версиями БД
    */
   public static Collection<Migration> getMigrations(){
      return  Arrays.asList(
              MIGRATION_1_2,
              MIGRATION_2_3,
              MIGRATION_3_4);
   }
}