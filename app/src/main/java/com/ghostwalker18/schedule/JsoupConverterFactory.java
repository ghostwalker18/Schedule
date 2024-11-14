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

import androidx.annotation.NonNull;
import okhttp3.ResponseBody;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import retrofit2.Converter;
import retrofit2.Retrofit;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * Этот класс используется для преобразования тела ответа Retrofit в Document библиотеки Jsoup.
 *
 * @author Ипатов Никита
 * @since 3.1
 */
public class JsoupConverterFactory
        extends Converter.Factory {
   @Override
   public Converter<ResponseBody, ?> responseBodyConverter(@NonNull Type type,
                                                           @NonNull Annotation[] annotations,
                                                           @NonNull Retrofit retrofit) {
      if(type == Document.class){
         return new JsoupConverter(retrofit.baseUrl().toString());
      }
      return null;
   }

   private class JsoupConverter implements Converter<ResponseBody, Document> {
      private final String baseUri;

      JsoupConverter(String baseUri){
         this.baseUri = baseUri;
      }

      @Override
      public Document convert(ResponseBody value) throws IOException {
         Parser parser = Parser.htmlParser();
         return Jsoup.parse(value.byteStream(), "UTF-8", baseUri, parser);
      }
   }
}