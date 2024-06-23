package com.example.schedule3;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import java.util.Calendar;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;;

public class ScheduleItemActivity extends AppCompatActivity {
    private ScheduleRepository repository;
    private String teacher;
    private String group;
    private Calendar date;
    private TableLayout table;

    private LiveData<Lesson[]> lessons = new MutableLiveData<>();
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_item);
        Bundle bundle = getIntent().getExtras();
        if(bundle == null){
            bundle = savedInstanceState;
        }
        teacher = bundle.getString("teacher");
        group = bundle.getString("group");
        date = DateConverters
                .fromString(bundle.getString("date"));
        date.set(Calendar.DAY_OF_WEEK, bundle.getInt("dayOfWeek"));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        toolbar.setTitle(generateTitle(date));
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        };
        table = findViewById(R.id.schedule);
        repository = ScheduleApp.getInstance().getRepository();
        lessons = repository.getLessons(group, teacher, date);
        lessons.observe(this, lessons -> {
            populateTable(table, lessons);
        });
    }

    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_schedule_item_activity, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.action_share){
            return shareSchedule();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("teacher", teacher);
        outState.putString("group", group);
        outState.putString("date", DateConverters.toString(date));
        super.onSaveInstanceState(outState);
    }

    private String generateTitle(Calendar date){
        String title = getString(R.string.day_table);
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
        return title + " " + dayString + "/" + monthString;
    }

    private void populateTable(TableLayout table, Lesson[] lessons){
        int tableRowLayout = R.layout.schedule_row;
        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            tableRowLayout = R.layout.schedule_row_with_times;
        int counter = 0;
        for(Lesson lesson : lessons){
            counter++;
            TableRow tr = addLesson(table, tableRowLayout, lesson);
            if(counter % 2 == 1)
                tr.setBackgroundColor(getResources().getColor(R.color.gray_500));
        }
    }

    private TableRow addLesson(TableLayout table, int tableRowLayout, Lesson lesson){
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        TableRow tr = (TableRow) inflater.inflate(tableRowLayout, null);
        ((TextView)tr.findViewById(R.id.number)).setText(lesson.lessonNumber.toString());
        ((TextView)tr.findViewById(R.id.subject)).setText(lesson.subject);
        ((TextView)tr.findViewById(R.id.teacher)).setText(lesson.teacher);
        ((TextView)tr.findViewById(R.id.room)).setText(lesson.roomNumber);
        TextView timesView = tr.findViewById(R.id.times);
        if(timesView != null)
            timesView.setText(lesson.times);
        table.addView(tr,new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        return tr;
    }

    private boolean shareSchedule(){
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");

        String schedule = getString(R.string.date) + ": ";
        schedule = schedule + DateConverters.toString(date) + "\n";
        schedule += "\n";

        for(Lesson lesson : lessons.getValue()){
            schedule = schedule + getString(R.string.number) + ": ";
            schedule = schedule + lesson.lessonNumber + "\n";

            schedule = schedule + getString(R.string.subject) + ": ";
            schedule = schedule + lesson.subject + "\n";

            if(!lesson.teacher.equals(""))
            {
                schedule = schedule + getString(R.string.teacher) + ": ";
                schedule = schedule + lesson.teacher + "\n";
            }

            if(!lesson.roomNumber.equals(""))
            {
                schedule = schedule + getString(R.string.room) + ": ";
                schedule = schedule + lesson.roomNumber + "\n";
            }

            schedule += "\n";
        }

        schedule += "\n";

        intent.putExtra(Intent.EXTRA_TEXT, schedule);
        Intent shareIntent = Intent.createChooser(intent, null);
        startActivity(shareIntent);
        return true;
    }
}