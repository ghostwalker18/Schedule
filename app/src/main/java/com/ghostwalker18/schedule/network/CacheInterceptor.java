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

import java.io.IOException;
import androidx.annotation.NonNull;
import okhttp3.Interceptor;
import okhttp3.Response;

/**
 * Этот класс служит для реализации кэширования запросов к серверу расписания.
 *
 * @author Ипатов Никита
 * @since 3.1
 */
public class CacheInterceptor
        implements Interceptor {
   @NonNull
   @Override
   public Response intercept(@NonNull Chain chain) throws IOException {
      Response response = chain.proceed(chain.request());
      if(!response.isSuccessful() || response.cacheControl().noCache()
              || response.cacheControl().mustRevalidate() || response.cacheControl().noStore())
         return response;
      return response.newBuilder()
              .header("Cache-Control", "max-age=3600")
              .build();
   }
}