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

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Интерфейс для создания Retrofit2 API,
 * используемого при скачивании файлов расписания и звонков.
 *
 * @author  Ипатов Никита
 */
public interface ScheduleNetworkAPI {
   /**
    * Получение файла расписания звонков на понедельник
    *
    * @return асинхронный ответ сервера
    */
   @GET(ScheduleApp.MONDAY_TIMES_URL)
   Call<ResponseBody> getMondayTimes();

   /**
    * Получение файла расписания звонков со вторника по пятницу
    *
    * @return асинхронный ответ сервера
    */
   @GET(ScheduleApp.OTHER_TIMES_URL)
   Call<ResponseBody> getOtherTimes();

   /**
    * Получение файла расписания по заданному URL
    *
    * @return асинхронный ответ сервера
    */
   @GET
   Call<ResponseBody> getScheduleFile(@Url String url);
}