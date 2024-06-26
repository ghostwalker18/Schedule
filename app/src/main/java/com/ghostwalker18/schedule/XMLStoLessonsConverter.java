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

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.TreeMap;

public class XMLStoLessonsConverter {
   public static List<Lesson> convert(XSSFWorkbook excelFile){
      List<Lesson> lessons = new ArrayList<>();

      for(int i = 0; i < excelFile.getNumberOfSheets(); i++){
         XSSFSheet sheet = excelFile.getSheetAt(i);
         String date = sheet.getSheetName() + "." + Calendar.getInstance().get(Calendar.YEAR);
         NavigableMap<Integer, String> groups = new TreeMap<>();
         XSSFRow groupsRow = sheet.getRow(3);
         if(groupsRow == null)
            break;
         for(int j = groupsRow.getFirstCellNum() + 2; j < groupsRow.getLastCellNum(); j++){
            XSSFCell groupRowCell = groupsRow.getCell(j);
            if(groupRowCell == null )
               continue;
            if(!groupRowCell.getStringCellValue().equals(""))
               groups.put(j, groupRowCell.getStringCellValue());
         }

         scheduleFilling : {
            NavigableSet<Integer> groupBounds = groups.navigableKeySet();
            for(int j = sheet.getFirstRowNum() + 5; j < sheet.getLastRowNum(); j +=2){
               for(int k : groupBounds){
                  if(sheet.getRow(j).getCell(k).getStringCellValue().equals(groups.get(k)))
                     break scheduleFilling;
                  Lesson lesson = new Lesson();
                  lesson.date = DateConverters.fromString(date);
                  lesson.group = groups.get(k);
                  lesson.lessonNumber = sheet.getRow(j)
                          .getCell(1)
                          .getStringCellValue();
                  lesson.times = sheet.getRow(j+1)
                          .getCell(1)
                          .getStringCellValue();
                  lesson.subject = sheet.getRow(j)
                          .getCell(k)
                          .getStringCellValue();
                  lesson.teacher = sheet.getRow(j+1)
                          .getCell(k)
                          .getStringCellValue();
                  Integer nextGroupBound = groupBounds.higher(k);
                  if(nextGroupBound != null){
                     lesson.roomNumber = getCellContentsAsString(sheet, j, nextGroupBound - 1);
                  }
                  else{
                     String roomNumber = getCellContentsAsString(sheet, j, k + 2);
                     if(!roomNumber.equals(""))
                        lesson.roomNumber = roomNumber;
                     else
                        lesson.roomNumber = getCellContentsAsString(sheet, j, k + 3);
                  }
                  //Required for primary key
                  if(!lesson.subject.equals(""))
                     lessons.add(lesson);
               }
            }
         }
      }
      return lessons;
   }

   private static String getCellContentsAsString(XSSFSheet sheet, int row, int column){
      CellType cellType = sheet.getRow(row)
              .getCell(column)
              .getCellTypeEnum();
      switch (cellType){
         case STRING:
            return sheet.getRow(row)
                    .getCell(column)
                    .getStringCellValue();
         case NUMERIC:
            return String.valueOf((int)sheet.getRow(row)
                    .getCell(column)
                    .getNumericCellValue());
         default:
            return "";
      }
   }
}