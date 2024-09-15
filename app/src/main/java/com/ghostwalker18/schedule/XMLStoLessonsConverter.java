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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.TreeMap;

public class XMLStoLessonsConverter
        implements IConverter{

   private static final int FIRST_ROW_GAP_1 = 5;
   private static final int FIST_ROW_GAP_2 = 105;
   private static final int SCHEDULE_CELL_HEIGHT_1 = 2;
   private static final int SCHEDULE_CELL_HEIGHT_2 = 4;

   public List<Lesson> convertFirstCorpus(Workbook excelFile){
      List<Lesson> lessons = new ArrayList<>();
      DateConverters dateConverters = new DateConverters();


      for(int i = 0; i < excelFile.getNumberOfSheets(); i++){
         Sheet sheet = excelFile.getSheetAt(i);
         RowCache cache = RowCache.builder()
                 .setSheet(sheet)
                 .setSize(5)
                 .build();
         String date = sheet.getSheetName() + "." + Calendar.getInstance().get(Calendar.YEAR);
         NavigableMap<Integer, String> groups = new TreeMap<>();
         //Row groupsRow = sheet.getRow(3);
         Row groupsRow = cache.getRow(3);
         //checking if there is a schedule at the list
         if(groupsRow == null)
            break;
         //getting groups` names
         for(int j = groupsRow.getFirstCellNum() + 2; j < groupsRow.getLastCellNum(); j++){
            Cell groupRowCell = groupsRow.getCell(j);
            //if cells are united, only first cell in union is not null
            if(groupRowCell == null )
               continue;
            if(!groupRowCell.getStringCellValue().trim().equals("")){
               String group = groupRowCell.getStringCellValue().trim();
               //mistake protection
               groups.put(j, group.replaceAll("\\s+", ""));
            }
         }

         //start filling schedule from top to bottom and from left to right
         scheduleFilling : {
            NavigableSet<Integer> groupBounds = groups.navigableKeySet();
            for(int j = sheet.getFirstRowNum() + FIRST_ROW_GAP_1;
                j < sheet.getLastRowNum();
                j += SCHEDULE_CELL_HEIGHT_1){
               for(int k : groupBounds){
                  //bottom of schedule are group names, breaking here
                  if(cache.getRow(j).getCell(k).getStringCellValue().equals(groups.get(k)))
                     break scheduleFilling;
                  Lesson lesson = new Lesson();
                  lesson.date = dateConverters.convertFirstCorpusDate(date);
                  lesson.group = (Objects.requireNonNull(groups.get(k)));
                  lesson.lessonNumber = getCellContentsAsString(cache, j, 1).trim();
                  lesson.times = getCellContentsAsString(cache, j + 1, 1).trim();
                  lesson.subject = getCellContentsAsString(cache, j, k).trim();
                  lesson.teacher = getCellContentsAsString(cache, j + 1, k).trim();
                  Integer nextGroupBound = groupBounds.higher(k);
                  String roomNumber;
                  if(nextGroupBound != null){
                     roomNumber = getCellContentsAsString(cache, j, nextGroupBound - 1).trim();
                  }
                  else{
                     roomNumber = getCellContentsAsString(cache, j, k + 2).trim();
                     if(roomNumber.equals(""))
                        roomNumber = getCellContentsAsString(cache, j, k + 3).trim();
                  }
                  lesson.roomNumber = roomNumber;
                  //Required for primary key
                  if(!lesson.subject.equals(""))
                     lessons.add(lesson);
               }
            }
         }
      }

      return lessons;
   }

   public List<Lesson> convertSecondCorpus(Workbook excelFile){
      List<Lesson> lessons = new ArrayList<>();
      DateConverters dateConverters = new DateConverters();

      for(int i = 0; i < excelFile.getNumberOfSheets(); i++) {
         Sheet sheet = excelFile.getSheetAt(i);
         RowCache cache = RowCache.builder()
                 .setSheet(sheet)
                 .setSize(10)
                 .build();
         String date = sheet.getSheetName().trim();
         NavigableMap<Integer, String> groups = new TreeMap<>();
         Row groupsRow = cache.getRow(3);
         //checking if there is a schedule at the list
         if (groupsRow == null)
            break;
         //getting groups` names
         for (int j = groupsRow.getFirstCellNum() + 2; j < groupsRow.getLastCellNum(); j++) {
            Cell groupRowCell = groupsRow.getCell(j);
            //if cells are united, only first cell in union is not null
            if (groupRowCell == null)
               continue;
            if (!groupRowCell.getStringCellValue().trim().equals("") &&
                    !groupRowCell.getStringCellValue().trim().equals("Группа") &&
                    !groupRowCell.getStringCellValue().trim().equals("День недели")
            ){
               String group = groupRowCell.getStringCellValue().trim();
               //mistake protection
               groups.put(j, group.replaceAll("\\s+", "").trim());
            }
         }

         //start filling schedule from top to bottom and from left to right
         scheduleFilling : {
            NavigableSet<Integer> groupBounds = groups.navigableKeySet();
            for(int j = sheet.getFirstRowNum() + FIST_ROW_GAP_2;
                j < sheet.getLastRowNum();
                j += SCHEDULE_CELL_HEIGHT_2){
               for(int k : groupBounds){
                  //bottom of schedule are group names, breaking here
                  if(cache.getRow(j).getCell(k).getStringCellValue().equals(groups.get(k)))
                     break scheduleFilling;
                  Lesson lesson = new Lesson();
                  lesson.date = dateConverters.convertSecondCorpusDate(date);
                  lesson.group = Objects.requireNonNull(groups.get(k));
                  lesson.lessonNumber = getCellContentsAsString(cache, j, 1).trim();
                  lesson.times = getCellContentsAsString(cache, j + 1, 1).trim();
                  String lessonSubject = getCellContentsAsString(cache, j, k) + " " +
                          getCellContentsAsString(cache, j + 1, k);
                  lesson.subject = lessonSubject.trim();
                  lesson.teacher = getCellContentsAsString(cache, j + 2, k).trim();
                  Integer nextGroupBound = groupBounds.higher(k);
                  String roomNumber;
                  if(nextGroupBound != null){
                     roomNumber = getCellContentsAsString(cache, j, nextGroupBound - 1) + " "
                             + getCellContentsAsString(cache, j + 1, nextGroupBound - 1) + " "
                             + getCellContentsAsString(cache, j + 2, nextGroupBound - 1);
                  }
                  else{
                     roomNumber = getCellContentsAsString(cache, j, k + 3) + " " +
                             getCellContentsAsString(cache, j + 1, k + 3) + " " +
                             getCellContentsAsString(cache, j + 2, k + 3);
                  }
                  lesson.roomNumber = roomNumber.trim();
                  //Required for primary key
                  if(!lesson.subject.equals(""))
                     lessons.add(lesson);
               }
            }
         }
      }

      return lessons;
   }

   /**
    * Этот метод используется для получения содержимого ячейки в виде строки.
    *
    * @param cache лист эксель
    * @param row номер ряда ячейки
    * @param column номер столбца ячейки
    * @return содержимое ячейки в виде строки
    */
   private static String getCellContentsAsString(RowCache cache, int row, int column){
      Cell cell = cache.getRow(row)
              .getCell(column);
      if(cell == null)
         return "";
      switch (cell.getCellType()){
         case STRING:
            return cache.getRow(row)
                    .getCell(column)
                    .getStringCellValue();
         case NUMERIC:
            return String.valueOf((int)cache.getRow(row)
                    .getCell(column)
                    .getNumericCellValue());
         default:
            return "";
      }
   }
}