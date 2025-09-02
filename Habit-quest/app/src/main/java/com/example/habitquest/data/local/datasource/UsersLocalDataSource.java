package com.example.habitquest.data.local.datasource;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitquest.data.local.db.AppContract;
import com.example.habitquest.data.local.db.SQLiteHelper;

// Koristi se za direktan pristup lokalnoj bazi podataka (INSERT, SELECT).
public class UsersLocalDataSource {

    private final SQLiteHelper dbHelper;

    public UsersLocalDataSource(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public long insertUser(String email, String username, String password, int avatar) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_EMAIL, email);
        values.put(AppContract.UserEntry.COLUMN_USERNAME, username);
        values.put(AppContract.UserEntry.COLUMN_PASSWORD, password);
        values.put(AppContract.UserEntry.COLUMN_AVATAR, avatar);
        return db.insert(AppContract.UserEntry.TABLE_NAME, null, values);
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(AppContract.UserEntry.TABLE_NAME, null, null, null, null, null, null);
    }
}
