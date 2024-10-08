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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

/**
 * Этот класс реализует функциональность виджета приложения по показу расписания на текущий день.
 *
 * @author Ипатов Никита
 * @since 2.2
 */
public class ScheduleWidget
        extends AppWidgetProvider {
    static final ScheduleRepository repository = ScheduleApp.getInstance().getRepository();
    static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        repository.update();

        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget_wrapper);

        SharedPreferences prefs = context.getSharedPreferences("WIDGET_" + appWidgetId,
                Context.MODE_PRIVATE);

        String group = prefs.getString("group", "last");
        if(group.equals("last")) {
            group = repository.getSavedGroup();
            if(group == null)
                group = context.getString(R.string.not_mentioned);
        };

        Calendar date = Calendar.getInstance();
        switch (prefs.getString("day", "")){
            case "tomorrow":
                date.add(Calendar.DAY_OF_YEAR,  1);
                break;
        }

        LiveData<Lesson[]> lessons = repository.getLessons(group, null, date);
        lessons.observeForever(new ScheduleObserver(appWidgetId));

        boolean isEdited = prefs.getBoolean("isEdited", false);
        if(isEdited){
            views.removeAllViews(R.id.widget_wrapper);

            boolean isDynamicColorsEnabled = prefs.getBoolean("dynamic_colors", false);
            String theme = prefs.getString("theme", "system");
            int widgetLayoutId = getRequiredLayout(theme, isDynamicColorsEnabled);
            RemoteViews scheduleView = new RemoteViews(context.getPackageName(), widgetLayoutId);

            String day = prefs.getString("day", "today");
            switch (day){
                case "today":
                    scheduleView.setTextViewText(R.id.day, context.getString(R.string.today));
                    break;
                case "tomorrow":
                    scheduleView.setTextViewText(R.id.day, context.getString(R.string.tomorrow));
                    break;
            }

            views.addView(R.id.widget_wrapper, scheduleView);
        }

        views.setTextViewText(R.id.group, context.getString(R.string.for_group) + " " + group);
        views.setTextViewText(R.id.updated,context.getString(R.string.updated) + " " +
                timeFormat.format(date.getTime()));

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
    public void onDeleted(Context context, int[] appWidgetIds) {
        for(int id : appWidgetIds){
            context.deleteSharedPreferences("WIDGET_" + id);
        }
    }

    private static int getRequiredLayout(String theme, boolean isDynamicColorsEnabled){
        if(isDynamicColorsEnabled){
            switch (theme){
                case "system":
                    return R.layout.schedule_widget_dynamic_daynight;
                case "night":
                    return R.layout.schedule_widget_dynamic_night;
                case "day":
                    return R.layout.schedule_widget_dynamic_day;
            }
        }
        else {
            switch (theme){
                case "system":
                    return R.layout.schedule_widget_daynight;
                case "night":
                    return R.layout.schedule_widget_night;
                case "day":
                    return R.layout.schedule_widget_day;
            }
        }
        return R.layout.schedule_widget_daynight;
    }

    /**
     * Этот класс служит для обновления View виджета новыми занятиями.
     *
     * @author Ипатов Никита
     * @since 2.3
     */
    private static class ScheduleObserver implements Observer<Lesson[]> {
        private int id;

        public ScheduleObserver(int widgetId){
            id = widgetId;
        }

        @Override
        public void onChanged(Lesson[] lessons) {
            Context context = ScheduleApp.getInstance().getApplicationContext();
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.schedule_widget_wrapper);
            views.removeAllViews(R.id.schedule);
            if(lessons.length == 0) {
                RemoteViews placeholder = new RemoteViews(context.getPackageName(),
                        R.layout.schedule_widget_row_placeholder);
                views.addView(R.id.schedule, placeholder);
            }
            int counter = 0;
            for(Lesson lesson : lessons){
                counter++;
                RemoteViews lessonItem = new RemoteViews(context.getPackageName(),
                        R.layout.schedule_widget_row_item);
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
            appWidgetManager.partiallyUpdateAppWidget(id, views);
        }
    }
}