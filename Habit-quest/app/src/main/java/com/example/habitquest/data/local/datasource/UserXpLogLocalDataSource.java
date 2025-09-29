package com.example.habitquest.data.local.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitquest.data.local.db.AppContract;
import com.example.habitquest.data.local.db.SQLiteHelper;
import com.example.habitquest.domain.model.UserXpLog;

import java.util.ArrayList;
import java.util.List;

public class UserXpLogLocalDataSource {
    private final SQLiteHelper dbHelper;

    public UserXpLogLocalDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void insert(UserXpLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(log);
        db.insert(AppContract.UserXpLogEntry.TABLE_NAME, null, values);
    }

    public void upsert(UserXpLog log) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toContentValues(log);
        db.insertWithOnConflict(
                AppContract.UserXpLogEntry.TABLE_NAME,
                null,
                values,
                SQLiteDatabase.CONFLICT_REPLACE
        );
    }

    public List<UserXpLog> getAllForUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                AppContract.UserXpLogEntry.TABLE_NAME,
                null,
                AppContract.UserXpLogEntry.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null
        );

        List<UserXpLog> logs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                logs.add(fromCursor(cursor));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return logs;
    }

    public void deleteAllForUser(long userId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(
                AppContract.UserXpLogEntry.TABLE_NAME,
                AppContract.UserXpLogEntry.COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)}
        );
    }

    private ContentValues toContentValues(UserXpLog log) {
        ContentValues values = new ContentValues();
        if (log.getId() != null) {
            values.put(AppContract.UserXpLogEntry.COLUMN_FIRESTORE_ID, log.getId()); // Firestore docId
        }
        values.put(AppContract.UserXpLogEntry.COLUMN_USER_ID, log.getUserId());
        values.put(AppContract.UserXpLogEntry.COLUMN_TASK_ID, log.getTaskId());
        values.put(AppContract.UserXpLogEntry.COLUMN_OCCURRENCE_ID, log.getOccurrenceId());
        values.put(AppContract.UserXpLogEntry.COLUMN_XP_GAINED, log.getXpGained());
        values.put(AppContract.UserXpLogEntry.COLUMN_COMPLETED_AT, log.getCompletedAt());
        return values;
    }

    private UserXpLog fromCursor(Cursor cursor) {
        UserXpLog log = new UserXpLog();
        log.setId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_FIRESTORE_ID))); // Firestore ID
        log.setUserId(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_USER_ID)));
        log.setTaskId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_TASK_ID)));
        log.setOccurrenceId(cursor.getString(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_OCCURRENCE_ID)));
        log.setXpGained(cursor.getInt(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_XP_GAINED)));
        log.setCompletedAt(cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.UserXpLogEntry.COLUMN_COMPLETED_AT)));
        return log;
    }
}
