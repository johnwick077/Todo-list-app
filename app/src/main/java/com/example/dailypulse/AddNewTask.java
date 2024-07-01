package com.example.dailypulse;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddNewTask extends BottomSheetDialogFragment {

    public static final String TAG = "AddNewTask";

    //widgets
    private EditText mEditText;
    private Button mSaveButton, mCancelButton;
    private TextView mDate, mTime ;

    private DataBaseHelper myDb;

    public static AddNewTask newInstance(){
        return new AddNewTask();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.add_new_task , container , false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mEditText = view.findViewById(R.id.task_edittext);
        mSaveButton = view.findViewById(R.id.save_btn);
        mCancelButton = view.findViewById(R.id.cancel_btn);
        mDate = view.findViewById(R.id.set_due_date);
        mTime = view.findViewById(R.id.set_due_time);

        myDb = new DataBaseHelper(getActivity());

        boolean isUpdate = false;

        final Bundle bundle = getArguments();
        if (bundle != null){
            isUpdate = true;
            String task = bundle.getString("task");
            String date = bundle.getString("date");
            String time = bundle.getString("time");
            mEditText.setText(task);
            mDate.setText(date);
            mTime.setText(time);

            if (task.length() > 0 ){
                mSaveButton.setEnabled(false);
            }
        } else {
            mDate.setText("Set Date");
            mTime.setText("Set Time");
        }

        mDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePickerDialog();
            }
        });

        mTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePickerDialog();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    mSaveButton.setEnabled(false);
                    mSaveButton.setBackgroundColor(Color.GRAY);
                } else {
                    mSaveButton.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        final boolean finalIsUpdate = isUpdate;
        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = mEditText.getText().toString();
                String date = mDate.getText().toString();
                String time = mTime.getText().toString();

                if (text.isEmpty() || date.equals("Set Date") || time.equals("Set Time")) {
                    // Display message if any field is empty
                    Toast.makeText(getContext(), "Please enter task name, date, and time", Toast.LENGTH_SHORT).show();
                } else {
                    if (finalIsUpdate) {
                        myDb.updateTask(bundle.getInt("id"), text, date, time);
                    } else {
                        ToDoModel item = new ToDoModel();
                        item.setTask(text);
                        item.setStatus(0);
                        item.setDate(date);
                        item.setTime(time);
                        myDb.insertTask(item);
                    }
                    scheduleNotification(text, date, time);
                    dismiss();
                }
            }
        });
    }

    private void showDatePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        String date =  dayOfMonth + "/" + (month + 1) + "/" + year;
                        mDate.setText(date);
                    }
                }, year, month, day);
        datePickerDialog.show();
    }

    private void showTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Format the time with AM/PM
                        String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                        int hourIn12Format = (hourOfDay == 0 || hourOfDay == 12) ? 12 : hourOfDay % 12;
                        String formattedTime = String.format("%d:%02d %s", hourIn12Format, minute, amPm);
                        mTime.setText(formattedTime);
                    }
                }, hour, minute, false);
        timePickerDialog.show();
    }

    private void scheduleNotification(String taskTitle, String taskDate, String taskTime) {
        // Parse date and time to get the milliseconds
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy h:mm a", Locale.getDefault());
        Date date = null;
        try {
            date = format.parse(taskDate + " " + taskTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (date == null) {
            Toast.makeText(getContext(), "Invalid date or time format.", Toast.LENGTH_SHORT).show();
            return;
        }

        long triggerAtMillis = date.getTime();

        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        intent.putExtra("taskTitle", taskTitle);
        intent.putExtra("taskDate", taskDate);
        intent.putExtra("taskTime", taskTime);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
    }


    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof OnDialogCloseListener){
            ((OnDialogCloseListener)activity).onDialogClose(dialog);
        }
    }
}
