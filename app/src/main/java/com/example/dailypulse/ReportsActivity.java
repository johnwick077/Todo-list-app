package com.example.dailypulse;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReportsActivity extends AppCompatActivity {

    private DataBaseHelper myDb;
    private ListView completedTasksListView;
    private ListView pendingTasksListView;
    private CalendarView calendarView;
    private TextView dateSelected;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_reports);

        completedTasksListView = findViewById(R.id.completed_tasks_listview);
        pendingTasksListView = findViewById(R.id.pending_tasks_listview);
        calendarView = findViewById(R.id.calendar_view);
        dateSelected = findViewById(R.id.date_selected);

        myDb = new DataBaseHelper(this);

        // Set calendar to today's date and display tasks for today
        long currentDateMillis = System.currentTimeMillis();
        calendarView.setDate(currentDateMillis, false, true);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/M/yyyy", Locale.getDefault());
        String todayDate = sdf.format(currentDateMillis);
        dateSelected.setText(todayDate);
        loadTasksForDate(todayDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                dateSelected.setText(selectedDate);
                loadTasksForDate(selectedDate);
            }
        });
    }

    private void loadTasksForDate(String date) {
        new LoadTasksTask().execute(date);
    }

    @SuppressLint("StaticFieldLeak")
    private class LoadTasksTask extends AsyncTask<String, Void, TaskResult> {
        @Override
        protected TaskResult doInBackground(String... dates) {
            String date = dates[0];
            List<ToDoModel> completedTasks = myDb.getTasksByStatusAndDate(1, date);
            List<ToDoModel> pendingTasks = myDb.getTasksByStatusAndDate(0, date);
            return new TaskResult(completedTasks, pendingTasks);
        }

        @Override
        protected void onPostExecute(TaskResult result) {
            ArrayAdapter<ToDoModel> completedAdapter = new ArrayAdapter<>(ReportsActivity.this, android.R.layout.simple_list_item_1, result.getCompletedTasks());
            ArrayAdapter<ToDoModel> pendingAdapter = new ArrayAdapter<>(ReportsActivity.this, android.R.layout.simple_list_item_1, result.getPendingTasks());

            completedTasksListView.setAdapter(completedAdapter);
            pendingTasksListView.setAdapter(pendingAdapter);
        }
    }

    private static class TaskResult {
        private final List<ToDoModel> completedTasks;
        private final List<ToDoModel> pendingTasks;

        TaskResult(List<ToDoModel> completedTasks, List<ToDoModel> pendingTasks) {
            this.completedTasks = completedTasks;
            this.pendingTasks = pendingTasks;
        }

        List<ToDoModel> getCompletedTasks() {
            return completedTasks;
        }

        List<ToDoModel> getPendingTasks() {
            return pendingTasks;
        }
    }
}
