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

import com.github.pjfanning.xlsx.StreamingReader;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import java.io.File;

/**
 * Модульные тесты для класса RowCache
 *
 * @author Ипатов Никита
 * @since 3.1
 */
@RunWith(Theories.class)
public class RowCacheUnitTest {
    private final File file = new File(getClass()
            .getResource("/testScheduleFile.xlsx").getPath());
    private RowCache cache;
    private static final int ROW_CACHE_SIZE = 7;

    @DataPoints("rowNumberSet")
    public static int[] getRowNumber() {
        return new int[] {0, 1, 10, 21, 37, 101};
    }

    @Before
    public void reloadCache(){
        Workbook excelFile = StreamingReader.builder()
                .rowCacheSize(10)
                .bufferSize(4096)
                .open(file);
        cache = RowCache.builder()
                .setSheet(excelFile.getSheetAt(0))
                .setSize(ROW_CACHE_SIZE)
                .build();
    }

    /**
     * Проверка выдачи нужного ряда из кэша.
     * @param rowNum номер требуемого ряда
     */
    @Theory
    public void gettingCorrectRowOneShot(
            @FromDataPoints("rowNumberSet") int rowNum){
        Row row = cache.getRow(rowNum);
        Assert.assertEquals(row.getRowNum(), rowNum);
    }

    /**
     * Проверка последовательной выдачи нужных рядов из кэша.
     * @param rowLimit предел выдачи рядов
     */
    @Theory
    public void gettingCorrectRowSerial(
            @FromDataPoints("rowNumberSet") int rowLimit) {
        for(int i = 0; i < rowLimit; i++){
            Row row = cache.getRow(i);
            Assert.assertEquals(row.getRowNum(), i);
        }
    }

    /**
     * Проверка некорректного доступа к кэшу.
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void incorrectOrderRowAccess(){
        Row row = cache.getRow(32);
        Row row2 = cache.getRow(0);
    }

    /**
     * Проверка некорректных номеров рядов
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void incorrectRowNumberAccess(){
        Row row = cache.getRow(-1);
    }

    /**
     * Проверка слишком большого номера рядя.
     */
    @Test(expected = StackOverflowError.class)
    public void tooHugeRowNumber(){
        Row row = cache.getRow(1000000000);
    }

    /**
     * Проверка выдачи немного устаревшнго ряда.
     */
    @Test
    public void getSomeRotten(){
        cache.getRow(8);
        Row row = cache.getRow(5);
        Assert.assertEquals(5, row.getRowNum());
    }
}