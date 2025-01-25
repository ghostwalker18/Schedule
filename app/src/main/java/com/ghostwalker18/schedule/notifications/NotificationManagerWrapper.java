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

package com.ghostwalker18.schedule.notifications;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationChannelCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

/**
 * Этот класс представляет собой надстройку над стандартным менеджером уведомлений Android.
 *
 * @author Ипатов Никита
 * @author RuStore
 * @since 4.1
 */
public final class NotificationManagerWrapper {
   private static NotificationManagerWrapper instance;
   private final NotificationManagerCompat notificationManager;

   private NotificationManagerWrapper(NotificationManagerCompat notificationManager) {
      this.notificationManager = notificationManager;
   }

   /**
    * Этот метод создает канал уведомлений.
    * @param channelId ID канала
    * @param channelName имя канала
    */
   public void createNotificationChannel(String channelId, String channelName){
      NotificationChannelCompat.Builder builder =
              new NotificationChannelCompat.Builder(channelId, NotificationManagerCompat.IMPORTANCE_DEFAULT);
      builder.setName(channelName);
      notificationManager.createNotificationChannel(builder.build());
   }

   /**
    * Этот метод позволяет показать Push-уведомление
    * @param context контекст приложения
    * @param data уведомление
    */
   public void showNotification(Context context, @NonNull AppNotification data) {
      NotificationCompat.Builder builder =
              new NotificationCompat.Builder(context, data.getChannelId())
                      .setContentTitle(data.getTitle())
                      .setContentText(data.getMessage());
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      if (notificationManager.getNotificationChannel(data.getChannelId()) == null) {
         createNotificationChannel(data.getChannelId(), data.getChannelName());
      }
      if (ActivityCompat.checkSelfPermission(
              context,
              Manifest.permission.POST_NOTIFICATIONS
      ) != PackageManager.PERMISSION_DENIED) {
         return;
      }
      notificationManager.notify(data.getId(), builder.build());
   }

   /**
    * Этот метод позволяет получить синглтон менеджера уведомлений
    * @param context контекст приложения
    * @return менеджер уведомлений
    */
   public static NotificationManagerWrapper getInstance(Context context) {
      if (instance == null) {
         instance = new NotificationManagerWrapper(NotificationManagerCompat.from(context));
      }
      return instance;
   }
}