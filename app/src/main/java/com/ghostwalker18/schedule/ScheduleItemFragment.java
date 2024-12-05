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

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import java.util.Calendar;
import java.util.HashMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Этот класс предсавляет собой кастомный элемент GUI,
 * используемый для отображения расписания на день.
 *
 * @author Ипатов Никита
 */
public class ScheduleItemFragment
        extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
   private static final HashMap<Integer, Integer> weekdaysNumbers = new HashMap<>();
   static {
      weekdaysNumbers.put(R.string.monday, Calendar.MONDAY);
      weekdaysNumbers.put(R.string.tuesday, Calendar.TUESDAY);
      weekdaysNumbers.put(R.string.wednesday, Calendar.WEDNESDAY);
      weekdaysNumbers.put(R.string.thursday, Calendar.THURSDAY);
      weekdaysNumbers.put(R.string.friday, Calendar.FRIDAY);
   }
   private SharedPreferences preferences;
   private Button button;
   private TableLayout table;
   private ScheduleState state;
   private ScheduleRepository repository;
   private final MutableLiveData<Calendar> date = new MutableLiveData<>();
   private LiveData<Lesson[]> lessons = new MutableLiveData<>();
   private int dayOfWeekID;
   private boolean isOpened = false;
   private String mode;

   public static ScheduleItemFragment newInstance(int dayOfWeekId) {
      Bundle args = new Bundle();
      args.putInt("dayOfWeek", dayOfWeekId);
      ScheduleItemFragment fragment = new ScheduleItemFragment();
      fragment.setArguments(args);
      return fragment;
   }

   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      state = new ViewModelProvider(requireActivity()).get(ScheduleState.class);
      repository = ScheduleApp.getInstance().getScheduleRepository();
      dayOfWeekID = getArguments().getInt("dayOfWeek");
      preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
      preferences.registerOnSharedPreferenceChangeListener(this);
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container,
                            Bundle savedInstanceState) {
      return inflater.inflate(R.layout.fragment_schedule_item, container, false);
   }

   @Override
   public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
      super.onViewCreated(view, savedInstanceState);
      button = view.findViewById(R.id.button);
      button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary));
      table = view.findViewById(R.id.schedule);
      state.getCalendar().observe(getViewLifecycleOwner(),
              calendar -> date.setValue(new Calendar.Builder()
              .setWeekDate(state.getYear(), state.getWeek(), weekdaysNumbers.get(dayOfWeekID))
              .build()));
      state.getGroup().observe(getViewLifecycleOwner(), group -> {
         lessons = repository.getLessons(state.getGroup().getValue(),
                 state.getTeacher().getValue(),
                 date.getValue());
         lessons.observe(getViewLifecycleOwner(), lessons -> populateTable(table, lessons));
      });
      state.getTeacher().observe(getViewLifecycleOwner(), teacher -> {
         lessons = repository.getLessons(state.getGroup().getValue(),
                 state.getTeacher().getValue(),
                 date.getValue());
         lessons.observe(getViewLifecycleOwner(), lessons -> populateTable(table, lessons));
      });
      date.observe(getViewLifecycleOwner(), date -> {
         isOpened = isDateToday(date);
         button.setText(generateTitle(date, dayOfWeekID));
         lessons = repository.getLessons(state.getGroup().getValue(),
                 state.getTeacher().getValue(),
                 date);
         lessons.observe(getViewLifecycleOwner(), lessons -> populateTable(table, lessons));
         showTable();
      });
      setUpMode();
      view.findViewById(R.id.notes).setOnClickListener(view1 -> openNotesActivity());
   }

   /**
    * Этот метод позвоволяет получить расписание для этого элемента в виде
    * форматированной строки.
    *
    * @return расписание на этот день
    */
   public String getSchedule(){
      StringBuilder schedule = new StringBuilder(getString(R.string.date) + ": ");
      schedule.append(DateConverters.toString(date.getValue())).append("\n");

      schedule.append("\n");
      for(Lesson lesson : lessons.getValue()){
         schedule.append(lesson.toString());
         schedule.append("\n");
      }
      schedule.append("\n");

      return schedule.toString();
   }

   /**
    * Этот метод позволяет узнать, открыто ли расписание для просмотра.
    * @return открыто ли окно
    */
   public boolean isOpened(){
      return isOpened;
   }

   /**
    * Этот метод устанавливает режим отображения расписания:
    * в отдельном окне или в таблице в элементе.
    */
   private void setUpMode(){
      mode = preferences.getString("scheduleStyle", "in_fragment");
      switch (mode){
         case "in_fragment":
            button.setOnClickListener(this::toggleSchedule);
            button.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    ResourcesCompat.getDrawable(getResources(),
                            R.drawable.baseline_keyboard_arrow_down_24,
                            null), null);
            break;
         case "in_activity":
            button.setOnClickListener(this::openScheduleInActivity);
            button.setCompoundDrawablesWithIntrinsicBounds(null, null,
                    null, null);
            break;
      }
   }

   /**
    * Этот метод используется для переключения состояния таблицы между скрыта/видима.
    * @param view этот параметр требуется для соответствия сигнатуре Listener
    */
   private void toggleSchedule(View view){
      isOpened = !isOpened;
      showTable();
   }

   /**
    * Этот метод открывает отдельное окно для отображения расписания.
    * @param view этот параметр требуется для соответствия сигнатуре Listener
    */
   private void openScheduleInActivity(View view){
      Bundle bundle = new Bundle();
      bundle.putString("group", state.getGroup().getValue());
      bundle.putString("teacher", state.getTeacher().getValue());
      bundle.putString("date", DateConverters.toString(state.getCalendar().getValue()));
      bundle.putInt("dayOfWeek", weekdaysNumbers.get(dayOfWeekID));
      Intent intent = new Intent(this.getActivity(), ScheduleItemActivity.class);
      intent.putExtras(bundle);
      startActivity(intent);
   }

   /**
    * Этот метод используется для настройки визуального отображения таблицы расписания
    */
   private void showTable(){
      if(isOpened && mode.equals("in_fragment")){
         button.setCompoundDrawablesWithIntrinsicBounds(null,
                 null,
                 ResourcesCompat.getDrawable(getResources(),
                         R.drawable.baseline_keyboard_arrow_up_24, null),
                 null);
         AnimatorSet open = (AnimatorSet) AnimatorInflater
                 .loadAnimator(requireContext(), R.animator.drop_down);
         open.setTarget(table);
         open.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
               super.onAnimationStart(animation);
               table.setVisibility(View.VISIBLE);
               getView().findViewById(R.id.notes).setVisibility(View.VISIBLE);
            }
         });
         open.start();
      } else {
         button.setCompoundDrawablesWithIntrinsicBounds(null,
                 null,
                 ResourcesCompat.getDrawable(getResources(),
                         R.drawable.baseline_keyboard_arrow_down_24, null),
                 null);
         table.setVisibility(View.GONE);
         getView().findViewById(R.id.notes).setVisibility(View.GONE);
      }
   }

   /**
    * Этот метод генерирует заголовок для этого элемента
    * @param date дата расписания
    * @param dayOfWeekId id строкового ресурса соответствующего дня недели
    * @return заголовок
    */
   private String generateTitle(Calendar date,  int dayOfWeekId){
      String dayOfWeek = getResources().getString(dayOfWeekId);
      //Month is a number in 0 - 11
      int month = date.get(Calendar.MONTH) + 1;
      //Formatting month number with leading zero
      String monthString = String.valueOf(month);
      if(month < 10){
         monthString = "0" + monthString;
      }
      int day = date.get(Calendar.DAY_OF_MONTH);
      String dayString = String.valueOf(day);
      //Formatting day number with leading zero
      if(day < 10){
         dayString = "0" + dayString;
      }
      String label = dayOfWeek + " (" + dayString  + "/" + monthString + ")";
      if(isDateToday(date)){
         label = label + " - " + getResources().getString(R.string.today);
      }
      return label;
   }

   /**
    * Этот метод используется для проверки, является ли заданная дата сегодняшним днем.
    * @param date дата для проверки
    * @return сегодня ли дата
    */
   private boolean isDateToday(Calendar date){
      Calendar rightNow = Calendar.getInstance();
      return rightNow.get(Calendar.YEAR) == date.get(Calendar.YEAR)
              && rightNow.get(Calendar.MONTH) == date.get(Calendar.MONTH)
              && rightNow.get(Calendar.DAY_OF_MONTH) == date.get(Calendar.DAY_OF_MONTH);
   }

   /**
    * Этот метод используется для заполнения таблицы занятиями и задания ее стиля.
    * @param table таблица для заполнения
    * @param lessons занятия
    */
   private void populateTable(TableLayout table, Lesson[] lessons){
      table.removeViews(1, table.getChildCount() - 1);
      int counter = 0;
      for(Lesson lesson : lessons){
         counter++;
         TableRow tr = addLesson(table, lesson);
         if(counter % 2 == 1)
            tr.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.gray_500));
      }
   }

   /**
    * Этот метод используется для добавления занятия в таблицу
    * @param table таблица для добавления
    * @param lesson занятие
    * @return ряд таблицы, куда было добавлено занятие
    */
   private TableRow addLesson(TableLayout table, Lesson lesson){
      LayoutInflater inflater = (LayoutInflater) getContext()
              .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      TableRow tr = (TableRow) inflater.inflate(R.layout.schedule_row, null);
      ((TextView)tr.findViewById(R.id.number)).setText(lesson.lessonNumber);
      ((TextView)tr.findViewById(R.id.subject)).setText(lesson.subject);
      ((TextView)tr.findViewById(R.id.teacher)).setText(lesson.teacher);
      ((TextView)tr.findViewById(R.id.room)).setText(lesson.roomNumber);
      table.addView(tr, new TableLayout.LayoutParams(
              TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
      return tr;
   }

   /**
    * Этот метод окрывает экран с заметками для этого дня.
    */
   private void openNotesActivity() {
      Bundle bundle = new Bundle();
      Intent intent = new Intent(this.getActivity(), NotesActivity.class);
      bundle.putString("group", state.getGroup().getValue());
      bundle.putString("date", DateConverters.toString(date.getValue()));
      intent.putExtras(bundle);
      startActivity(intent);
   }

   @Override
   public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, @Nullable String s) {
      switch (s){
         case "scheduleStyle":
            if(this.isOpened)
               toggleSchedule(getView());
            setUpMode();
            break;
      }
   }
}