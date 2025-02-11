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

package com.ghostwalker18.schedule.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.notifications.NotificationManagerWrapper;
import java.io.File;
import java.time.Duration;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

/**
 * В этом классе содержаться различные вспомогательные методы, специфичные для Android.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public class AndroidUtils {

   /**
    * Этот метод используется для очистки кэша ApachePOI.
    */
   public static void clearPOICache(@NonNull Context context){
      new Thread(() -> {
         try{
            File cacheDir = new File(context.getCacheDir(), "poifiles");
            Calendar currentDate = Calendar.getInstance();
            for (File cachedFile : Objects.requireNonNull(cacheDir.listFiles())){
               Calendar creationDate = Calendar.getInstance();
               creationDate.setTime(new Date(cachedFile.lastModified()));
               //Deleting files created more than 1 day ago
               if(Duration.between(creationDate.toInstant(), currentDate.toInstant()).toDays() > 1)
                  cachedFile.delete();
            }
         } catch (Exception ignored){/*Not required*/}
      }).start();
   }

   /**
    * Этот метод проверяет разрешения приложения
    * и меняет настройки приложения в соответствии с результатом
    *
    * @return
    */
   public static boolean checkNotificationsPermissions(@NonNull Context context,
                                                       @NonNull SharedPreferences preferences){
      boolean res = true;
      if (ActivityCompat.checkSelfPermission(
              context,
              Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_GRANTED) {
         preferences.edit()
                 .putBoolean("update_notifications", false)
                 .putBoolean("schedule_notifications", false)
                 .apply();
          res = false;
      }
      if(!NotificationManagerWrapper.getInstance(context)
              .isNotificationChannelEnabled(
                      context.getString(R.string.notifications_notification_schedule_update_channel_id))
      ){
         preferences.edit()
                 .putBoolean("schedule_notifications", false)
                 .apply();
         res = false;
      }
      if(!NotificationManagerWrapper.getInstance(context)
              .isNotificationChannelEnabled(
                      context.getString(R.string.notifications_notification_app_update_channel_id))
      ){
         preferences.edit()
                 .putBoolean("update_notifications", false)
                 .apply();
      }
       return res;
   }
}