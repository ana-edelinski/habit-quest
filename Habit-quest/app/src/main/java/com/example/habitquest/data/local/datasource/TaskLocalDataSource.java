package com.example.habitquest.data.local.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitquest.data.local.db.SQLiteHelper;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class TaskLocalDataSource {
    private final SQLiteHelper dbHelper;

    public TaskLocalDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public long insert(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(task, true);
        long id = db.insert("TASKS", null, values);
        task.setId(id);
        return id;
    }

    public long upsert(Task task) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(task, true);
        int rows = db.update("TASKS", values, "_id=? AND userId=?",
                new String[]{String.valueOf(task.getId()), String.valueOf(task.getUserId())});
        if (rows == 0) {
            return insert(task);
        }
        return task.getId();
    }

    public int delete(long taskId, long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete("TASKS", "_id=? AND userId=?",
                new String[]{String.valueOf(taskId), String.valueOf(userId)});
    }

    public List<Task> getAllByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query("TASKS", null, "userId=?",
                new String[]{String.valueOf(userId)}, null, null, "date ASC");
        List<Task> tasks = new ArrayList<>();
        while (c.moveToNext()) {
            tasks.add(fromCursor(c));
        }
        c.close();
        return tasks;
    }

    private static ContentValues toValues(Task t, boolean includeId) {
        ContentValues v = new ContentValues();
        if (includeId && t.getId() != null) v.put("_id", t.getId());
        v.put("userId", t.getUserId());
        v.put("categoryId", t.getCategoryId());
        v.put("name", t.getName());
        v.put("description", t.getDescription());
        v.put("date", t.getDate());
        v.put("startDate", t.getStartDate());
        v.put("endDate", t.getEndDate());
        v.put("interval", t.getInterval());
        v.put("unit", t.getUnit());
        v.put("difficultyXp", t.getDifficultyXp());
        v.put("importanceXp", t.getImportanceXp());
        v.put("totalXp", t.getTotalXp());
        if (t.getStatus() != null) {
            v.put("status", t.getStatus().name()); // npr. "ACTIVE", "PAUSED", ...
        } else {
            v.put("status", TaskStatus.ACTIVE.name()); // default
        }
        return v;
    }

    private static Task fromCursor(Cursor c) {
        Task t = new Task();
        t.setId(c.getLong(c.getColumnIndexOrThrow("_id")));
        t.setUserId(c.getLong(c.getColumnIndexOrThrow("userId")));
        t.setCategoryId(c.getLong(c.getColumnIndexOrThrow("categoryId")));
        t.setName(c.getString(c.getColumnIndexOrThrow("name")));
        t.setDescription(c.getString(c.getColumnIndexOrThrow("description")));
        t.setDate(c.getLong(c.getColumnIndexOrThrow("date")));
        t.setStartDate(c.getLong(c.getColumnIndexOrThrow("startDate")));
        t.setEndDate(c.getLong(c.getColumnIndexOrThrow("endDate")));
        t.setInterval(c.getInt(c.getColumnIndexOrThrow("interval")));
        t.setUnit(c.getString(c.getColumnIndexOrThrow("unit")));
        t.setDifficultyXp(c.getInt(c.getColumnIndexOrThrow("difficultyXp")));
        t.setImportanceXp(c.getInt(c.getColumnIndexOrThrow("importanceXp")));
        t.setTotalXp(c.getInt(c.getColumnIndexOrThrow("totalXp")));
        String statusStr = c.getString(c.getColumnIndexOrThrow("status"));
        TaskStatus status = TaskStatus.ACTIVE; // default
        try {
            status = TaskStatus.valueOf(statusStr);
        } catch (Exception e) {
            // ostaje default
        }
        t.setStatus(status);
        return t;
    }
}
