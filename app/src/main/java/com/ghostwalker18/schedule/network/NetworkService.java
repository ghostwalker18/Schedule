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

package com.ghostwalker18.schedule.network;

import android.content.Context;
import android.content.SharedPreferences;
import java.io.File;
import java.util.concurrent.Executors;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

/**
 * Этот класс используется для предоставления приложению услуг доступа к сети.
 *
 * @author Ipatov Nikita
 * @since 3.1
 * @see CacheInterceptor
 */
public class NetworkService {
   private static final long SIZE_OF_CACHE = 10 * 1024 * 1024; // 10 MiB
   private final String baseUri;
   private final Context context;
   private final SharedPreferences preferences;

   public NetworkService(Context context, String baseUri, SharedPreferences preferences) {
      this.context = context;
      this.baseUri = baseUri;
      this.preferences = preferences;
   }

   /**
    * Этот метод позволяет получить API сайта ПТГХ.
    * @return API сайта для доступа к скачиванию файлов расписания
    */
   public ScheduleNetworkAPI getScheduleAPI(){
      Retrofit.Builder apiBuilder = new Retrofit.Builder()
              .baseUrl(baseUri)
              .callbackExecutor(Executors.newFixedThreadPool(4))
              .addConverterFactory(new JsoupConverterFactory());

      boolean isCachingEnabled = preferences.getBoolean("isCachingEnabled", true);
      if(isCachingEnabled){
         Cache cache = new Cache(new File(context.getCacheDir(), "http"), SIZE_OF_CACHE);
         OkHttpClient client = new OkHttpClient().newBuilder()
                 .cache(cache)
                 .addInterceptor(new CacheInterceptor())
                 .build();
         apiBuilder.client(client);
      }

      return apiBuilder
              .build()
              .create(ScheduleNetworkAPI.class);
   }
}