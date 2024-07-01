package com.example.dailypulse;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "TODO_DATABASE";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "TODO_TABLE";
    private static final String COL_1 = "ID";
    private static final String COL_2 = "TASK";
    private static final String COL_3 = "STATUS";
    private static final String COL_4 = "DATE";
    private static final String COL_5 = "TIME";

    public DataBaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                COL_1 + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_2 + " TEXT, " +
                COL_3 + " INTEGER, " +
                COL_4 + " TEXT, " +
                COL_5 + " TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Here you could add checks for version numbers and manage migrations accordingly
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void insertTask(ToDoModel model) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, model.getTask());
        values.put(COL_3, model.getStatus()); // Now taking status from model
        values.put(COL_4, model.getDate());
        values.put(COL_5, model.getTime());
        try {
            db.insert(TABLE_NAME, null, values);
        } finally {
            db.close();
        }
    }

    public void updateTask(int id, String task, String date, String time) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_2, task);
        values.put(COL_4, date);
        values.put(COL_5, time);
        try {
            db.update(TABLE_NAME, values, "ID=?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public void updateStatus(int id, int status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_3, status);
        try {
            db.update(TABLE_NAME, values, "ID=?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public void deleteTask(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            db.delete(TABLE_NAME, "ID=?", new String[]{String.valueOf(id)});
        } finally {
            db.close();
        }
    }

    public List<ToDoModel> getAllTasks() {
        List<ToDoModel> modelList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ToDoModel task = new ToDoModel();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_1)));
                    task.setTask(cursor.getString(cursor.getColumnIndexOrThrow(COL_2)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_3)));
                    task.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_4)));
                    task.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_5)));
                    modelList.add(task);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return modelList;
    }

    public List<ToDoModel> getTasksByStatusAndDate(int status, String date) {
        List<ToDoModel> tasks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_NAME, null, "STATUS=? AND DATE=?", new String[]{String.valueOf(status), date}, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    ToDoModel task = new ToDoModel();
                    task.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_1)));
                    task.setTask(cursor.getString(cursor.getColumnIndexOrThrow(COL_2)));
                    task.setStatus(cursor.getInt(cursor.getColumnIndexOrThrow(COL_3)));
                    task.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_4)));
                    task.setTime(cursor.getString(cursor.getColumnIndexOrThrow(COL_5)));
                    tasks.add(task);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }
        return tasks;
    }
}
