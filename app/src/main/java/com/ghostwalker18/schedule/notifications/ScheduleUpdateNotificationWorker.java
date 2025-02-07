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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ghostwalker18.schedule.R;
import com.ghostwalker18.schedule.ScheduleApp;
import com.ghostwalker18.schedule.converters.DateConverters;
import com.ghostwalker18.schedule.models.ScheduleRepository;
import com.ghostwalker18.schedule.views.MainActivity;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Calendar;
import java.util.concurrent.Executors;
import androidx.annotation.NonNull;
import androidx.work.ListenableWorker;
import androidx.work.WorkerParameters;

/**
 * Этот класс представляет собой работу
 * по получению нового расписания в фоновом режиме
 * и уведомления пользователя о результате работы.
 *
 * @author Ипатов Никита
 * @since 4.1
 */
public final class ScheduleUpdateNotificationWorker
        extends ListenableWorker {

    public ScheduleUpdateNotificationWorker(@NonNull Context context,
                                            @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public ListenableFuture<Result> startWork() {
        ListeningExecutorService service = MoreExecutors
                .listeningDecorator(Executors.newSingleThreadExecutor());
        return service.submit(() -> {
            final ScheduleRepository repository = ScheduleApp.getInstance().getScheduleRepository();
            ScheduleRepository.UpdateResult lastUpdateResult = repository.getUpdateResult();
            Calendar lastAvailableDate = repository.getLastKnownLessonDate(repository.getSavedGroup());
            repository.update();
            repository.onUpdateCompleted().whenComplete((updateResult, e) -> {
                if(lastUpdateResult != ScheduleRepository.UpdateResult.FAIL
                        && updateResult == ScheduleRepository.UpdateResult.FAIL){
                    NotificationManagerWrapper.getInstance(getApplicationContext())
                            .showNotification(getApplicationContext(), new AppNotification(
                                            0,
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_name),
                                            getApplicationContext().getString(
                                                    R.string.notifications_schedule_unavailable),
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_id),
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_name)
                                    )
                            );
                }
                Calendar currentAvailableDate = repository.getLastKnownLessonDate(
                        repository.getSavedGroup());
                if(currentAvailableDate.after(lastAvailableDate)){
                    Intent openScheduleIntent = new Intent(getApplicationContext(), MainActivity.class);
                    openScheduleIntent.putExtra("date",
                            DateConverters.toString(currentAvailableDate));
                    NotificationManagerWrapper.getInstance(getApplicationContext())
                            .showNotification(getApplicationContext(), new AppNotification(
                                            0,
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_name),
                                            getApplicationContext().getString(
                                                    R.string.notifications_new_schedule_available),
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_id),
                                            getApplicationContext().getString(
                                                    R.string.notifications_notification_schedule_update_channel_name)
                                    ),
                                    PendingIntent.getActivity(getApplicationContext(), 0,
                                            openScheduleIntent, PendingIntent.FLAG_IMMUTABLE)
                            );
                }
            });
            return Result.success();
        });
    }
}