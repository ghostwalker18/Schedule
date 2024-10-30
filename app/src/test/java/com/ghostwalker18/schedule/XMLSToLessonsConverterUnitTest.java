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

import org.apache.commons.math3.util.Pair;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import java.lang.reflect.Method;

/**
 * Модульные классы для класса XMLSToLessonsConverter
 *
 * @author Ипатов Никита
 * @since 3.1
 */
@RunWith(Theories.class)
public class XMLSToLessonsConverterUnitTest {
    private final IConverter converter = new XMLStoLessonsConverter();
    private static Method prepareTeacher;
    private static Method prepareSubject;
    private static Method prepareTimes;
    private static Method prepareRoomNumber;

    @DataPoints("prepareTimesIncorrectSet")
    public static Pair[] prepareTimesIncorrectSet(){
        return new Pair[]{
            new Pair<>("9:30- 10:30 ", "09:30-10:30"),
            new Pair<>("8:30 -10:30", "08:30-10:30"),
            new Pair<>(" 7:30 - 10:30", "07:30-10:30"),
        };
    }

    @DataPoints("prepareTimesCorrectSet")
    public static Pair[] prepareTimesCorrectSet(){
        return new Pair[]{
                new Pair<>("19:15-20:30", "19:15-20:30"),
                new Pair<>("20:00-20:30", "20:00-20:30"),
                new Pair<>("07:30-10:30", "07:30-10:30"),
        };
    }

    /**
     * Получение доступа к проверяемым приватным методам.
     * @throws Exception
     */
    @BeforeClass
    public static void getMethods() throws Exception {
        prepareTeacher = XMLStoLessonsConverter.class
                .getDeclaredMethod("prepareTeacher", String.class);
        prepareTeacher.setAccessible(true);
        prepareSubject = XMLStoLessonsConverter.class
                .getDeclaredMethod("prepareSubject", String.class);
        prepareSubject.setAccessible(true);
        prepareTimes = XMLStoLessonsConverter.class
                .getDeclaredMethod("prepareTimes", String.class);
        prepareTimes.setAccessible(true);
        prepareRoomNumber = XMLStoLessonsConverter.class
                .getDeclaredMethod("prepareRoomNumber", String.class);
        prepareRoomNumber.setAccessible(true);
    }

    /**
     * Проверка обработки имени преподавателя, ввод не соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareTeacherTestIncorrectInput() throws Exception {
        String actualResult = (String) prepareTeacher.invoke(converter, " Иванов    И.И. ");
        Assert.assertEquals("Иванов И.И.", actualResult);
    }

    /**
     * Проверка обработки имени преподавателя, ввод уже соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareTeacherTestCorrectInput() throws Exception {
        String actualResult = (String) prepareTeacher.invoke(converter, "Иванов И.И.");
        Assert.assertEquals("Иванов И.И.", actualResult);
    }

    /**
     * Проверка обработки имени преподавателя, ввод некорректен.
     * @throws Exception
     */
    @Test
    public void prepareTeacherTestNullInput() throws Exception {
        String input = null;
        String actualResult = (String) prepareTeacher.invoke(converter, input);
        Assert.assertNull(actualResult);
    }

    /**
     * Проверка обработки названия предмета, ввод не соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareSubjectTestIncorrectInput() throws Exception {
        String actualResult = (String) prepareSubject.invoke(converter, " 3D  \n моделирование ");
        Assert.assertEquals("3D моделирование", actualResult);
    }

    /**
     * Проверка обработки названия предмета, ввод уже соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareSubjectTestCorrectInput() throws Exception {
        String actualResult = (String) prepareSubject.invoke(converter, "3D моделирование");
        Assert.assertEquals("3D моделирование", actualResult);
    }

    /**
     * Проверка обработки названия предмета, ввод некорректен.
     * @throws Exception
     */
    @Test
    public void prepareSubjectTestNullInput() throws Exception {
        String input = null;
        String actualResult = (String) prepareSubject.invoke(converter, input);
        Assert.assertNull(actualResult);
    }

    /**
     * Проверка обработки времени занятия, ввод не соответствует желаемому.
     * @throws Exception
     */
    @Theory
    public void prepareTimesTestIncorrectInput(
            @FromDataPoints("prepareTimesIncorrectSet") Pair<String, String> pair)
            throws Exception {
        String actualResult = (String) prepareTimes.invoke(converter, pair.getFirst());
        Assert.assertEquals(pair.getSecond(), actualResult);
    }

    /**
     * Проверка обработк времени занятия, ввод уже соответствует желаемому.
     * @throws Exception
     */
    @Theory
    public void prepareTimesTestCorrectInput(
            @FromDataPoints("prepareTimesCorrectSet") Pair<String, String> pair)
            throws Exception {
        String actualResult = (String) prepareTimes.invoke(converter, pair.getFirst());
        Assert.assertEquals(pair.getSecond(), actualResult);
    }

    /**
     * Проверка обработки времени занятия, ввод некорректен.
     * @throws Exception
     */
    @Test
    public void prepareTimesTestNullInput() throws Exception {
        String input = null;
        String actualResult = (String) prepareTimes.invoke(converter, input);
        Assert.assertNull(actualResult);
    }

    /**
     * Проверка обработки номера кабинета, ввод не соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareRoomNumberTestIncorrectInput() throws Exception {
        String actualResult = (String) prepareRoomNumber.invoke(converter, " 32/ 45");
        Assert.assertEquals("32/45", actualResult);
    }

    /**
     * Проверка обработки номера кабинета, ввод уже соответствует желаемому.
     * @throws Exception
     */
    @Test
    public void prepareRoomNumberTestCorrectInput() throws Exception {
        String actualResult = (String) prepareRoomNumber.invoke(converter, "32 45");
        Assert.assertEquals("32 45", actualResult);
    }

    /**
     * Проверка обработки номера кабинета, ввод некорректен.
     * @throws Exception
     */
    @Test
    public void prepareRoomNumberTestNullInput() throws Exception {
        String input = null;
        String actualResult = (String) prepareRoomNumber.invoke(converter, input);
        Assert.assertNull(actualResult);
    }
}