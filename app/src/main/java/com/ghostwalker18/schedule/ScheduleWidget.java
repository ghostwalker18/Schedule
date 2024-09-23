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

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * Этот класс реализует функциональность виджета приложения по показу расписания на текущий день.
 *
 * @author Ипатов Никита
 */
public class ScheduleWidget extends AppWidgetProvider {
    static final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");
    static LiveData<Lesson[]> lessons;
    static Observer<Lesson[]> lessonsObserver = ScheduleWidget::updateLessons;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);

        repository.update();
        String group = repository.getSavedGroup();
        Calendar date = Calendar.getInstance();

        //repository.update();
        lessons = repository.getLessons(group, null, date);
        lessons.observeForever(lessonsObserver);

        views.setTextViewText(R.id.group, context.getString(R.string.for_group) + " " + group);
        views.setTextViewText(R.id.updated,context.getString(R.string.updated) + " " + timeFormat.format(date.getTime()));
        //setting action for refresh button: refresh schedule
        Intent intentRefresh = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE,
                null, context.getApplicationContext(), ScheduleWidget.class);
        intentRefresh.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS , new int[]{appWidgetId});
        PendingIntent pendingRefresh = PendingIntent.getBroadcast(context, appWidgetId,
                intentRefresh, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.updateButton, pendingRefresh);
        //setting action for schedule tap: open app
        Intent intentOpenApp = new Intent(context, MainActivity.class);
        PendingIntent pendingOpenApp = PendingIntent.getActivity(context, appWidgetId,
                intentOpenApp, PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.schedule, pendingOpenApp);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        lessons.removeObserver(lessonsObserver);
    }

    /**
     * Этот метод служит для обновления View виджета новыми занятиями.
     * @param lessons занятия
     */
    private static void updateLessons(Lesson[] lessons){
        Context context = ScheduleApp.getInstance().getApplicationContext();
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget);
        views.removeAllViews(R.id.schedule);
        int counter = 0;
        for(Lesson lesson : lessons){
            counter++;
            RemoteViews lessonItem = new RemoteViews(context.getPackageName(), R.layout.schedule_widget_row_item);
            if(counter % 2 == 1)
                lessonItem.setInt(R.id.row, "setBackgroundColor",
                        context.getResources().getColor(R.color.gray_500));
            lessonItem.setTextViewText(R.id.lessonNumber, lesson.lessonNumber);
            lessonItem.setTextViewText(R.id.subjectName, lesson.subject);
            lessonItem.setTextViewText(R.id.teacherName, lesson.teacher);
            lessonItem.setTextViewText(R.id.roomNumber, lesson.roomNumber);
            views.addView(R.id.schedule, lessonItem);
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context, ScheduleWidget.class));
        for(int i = 0; i<ids.length; i++)
            //important! partially, or it breaks when theme changes
            appWidgetManager.partiallyUpdateAppWidget(ids[i], views);
    }
}