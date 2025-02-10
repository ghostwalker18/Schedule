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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.ghostwalker18.schedule.R;

import java.util.Objects;

import androidx.annotation.NonNull;

/**
 * Этот класс служит для изменения названий и описаний каналов уведомлений
 * в соответствии с текущим языком при его изменении.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public final class NotificationsLocaleUpdater
        extends BroadcastReceiver {

   @Override
   public void onReceive(Context context, @NonNull Intent intent) {
      if(Objects.equals(intent.getAction(), Intent.ACTION_LOCALE_CHANGED)){
         NotificationManagerWrapper.getInstance(context).createNotificationChannel(
                 context.getString(R.string.notifications_notification_app_update_channel_id),
                 context.getString(R.string.notifications_notification_app_update_channel_name),
                 context.getString(R.string.notifications_notification_app_update_channel_descr)
         );
         NotificationManagerWrapper.getInstance(context).createNotificationChannel(
                 context.getString(R.string.notifications_notification_schedule_update_channel_id),
                 context.getString(R.string.notifications_notification_schedule_update_channel_name),
                 context.getString(R.string.notifications_notification_schedule_update_channel_descr)
         );
      }
   }
}