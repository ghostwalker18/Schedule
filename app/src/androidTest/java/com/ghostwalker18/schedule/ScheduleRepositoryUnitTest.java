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

import com.ghostwalker18.schedule.models.ScheduleRepository;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.util.List;

/**
 * Модульные тесты для класса ScheduleRepository
 *
 * @author Ипатов Никита
 * @since 3.1
 */
@RunWith(AndroidJUnit4.class)
public class ScheduleRepositoryUnitTest {
    private static ScheduleRepository repository;

    /**
     * Инициализация репозитория.
     */
    @BeforeClass
    public static void initRepo(){
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        //AppDatabase db = Mockito.mock(AppDatabase.class, Answers.CALLS_REAL_METHODS);
        repository = new ScheduleRepository(appContext, null);
    }

    /**
     * Проверка получения ссылок на расписание для первого корпуса.
     */
    @Test
    public void retrieveScheduleLinksForFirstCorpus(){
        List<String> links = repository.getLinksForFirstCorpusSchedule();
        Assert.assertFalse(links.isEmpty());
        for(String link : links)
            Assert.assertTrue(link.endsWith(".xlsx"));
    }

    /**
     * Проверка получения ссылок на расписание для второго корпуса.
     */
    @Test
    public void retrieveScheduleLinksForSecondCorpus(){
        List<String> links = repository.getLinksForSecondCorpusSchedule();
        Assert.assertFalse(links.isEmpty());
        for(String link : links)
            Assert.assertTrue(link.endsWith(".xlsx"));
    }

    /**
     * Проверка сохранения группы в репозитории.
     */
    @Test
    public void savingGroup(){
        String group = "A-31";
        repository.saveGroup(group);
        Assert.assertEquals("A-31", repository.getSavedGroup());
    }
}