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

import android.net.Uri;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import androidx.room.TypeConverter;

/**
 * Этот класс используется для ORM.
 * Содержит методы для преобразования ArrayList of Uri в String для БД и наоборот
 *
 * @author  Ипатов Никита
 * @since 3.2
 */
public class PhotoURIArrayConverters {

   /**
    * Этот метод преобразует ArrayList of Uri сущности в String для БД.
    *
    * @param uris  the entity attribute value to be converted
    * @return
    */
   @TypeConverter
   public static String toString(ArrayList<Uri> uris){
      new GsonBuilder().registerTypeAdapter(Uri.class, new UriJsonAdapter());
      if(uris == null || uris.size() == 0)
         return null;
      return new Gson().toJson(uris);
   }

   /**
    * Этот метод преобразует String из БД в ArrayList of Uri сущности.
    *
    * @param uriString  the data from the database column to be converted
    * @return
    */
   @TypeConverter
   public static ArrayList<Uri> fromString(String uriString){
      new GsonBuilder().registerTypeAdapter(Uri.class, new UriJsonAdapter());
      if (uriString == null)
         return null;
      Type listType = new TypeToken<ArrayList<Uri>>() {}.getType();
      return new Gson().fromJson(uriString, listType);
   }

   /**
    * Этот класс используется для конвертации Uri в Json и наоборот.
    *
    * @author Ипатов Никита
    * @since 3.2
    */
   private static class UriJsonAdapter
           implements JsonSerializer<Uri>, JsonDeserializer<Uri>{

      @Override
      public Uri deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
              throws JsonParseException {
         try{
            String uri = json.getAsString();
            if(uri == null || uri.equals("")){
               return Uri.EMPTY;
            }
            else
               return Uri.parse(uri);
         }
         catch (UnsupportedOperationException e){
            return Uri.EMPTY;
         }
      }

      @Override
      public JsonElement serialize(Uri src, Type typeOfSrc, JsonSerializationContext context) {
         return new JsonPrimitive(src.toString());
      }
   }
}