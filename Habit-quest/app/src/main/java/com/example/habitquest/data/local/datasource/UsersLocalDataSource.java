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

        Cursor cursor = db.query(AppContract.UserEntry.TABLE_NAME,
                new String[]{AppContract.UserEntry.COLUMN_EMAIL},
                AppContract.UserEntry.COLUMN_EMAIL + " = ?",
                new String[]{email},
                null, null, null);

        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            return -1;
        }

        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_EMAIL, email);
        values.put(AppContract.UserEntry.COLUMN_USERNAME, username);
        values.put(AppContract.UserEntry.COLUMN_PASSWORD, password);
        values.put(AppContract.UserEntry.COLUMN_AVATAR, avatar);
        values.put(AppContract.UserEntry.COLUMN_TOTAL_XP, 0);
        values.put(AppContract.UserEntry.COLUMN_LEVEL, 0);
        values.put(AppContract.UserEntry.COLUMN_TITLE, "Beginner");
        values.put(AppContract.UserEntry.COLUMN_PP, 0);


        return db.insert(AppContract.UserEntry.TABLE_NAME, null, values);
    }

    public Cursor getAllUsers() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        return db.query(AppContract.UserEntry.TABLE_NAME, null, null, null, null, null, null);
    }

    public Long getUserIdByEmail(String email) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(
                AppContract.UserEntry.TABLE_NAME,
                new String[]{AppContract.UserEntry._ID},
                AppContract.UserEntry.COLUMN_EMAIL + "=?",
                new String[]{email},
                null, null, null
        );
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(AppContract.UserEntry._ID));
            cursor.close();
            return id;
        }
        if (cursor != null) cursor.close();
        return null;
    }

    public void updateUserXpAndLevel(long userId, int totalXp, int level, String title, int pp) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_TOTAL_XP, totalXp);
        values.put(AppContract.UserEntry.COLUMN_LEVEL, level);
        values.put(AppContract.UserEntry.COLUMN_TITLE, title);
        values.put(AppContract.UserEntry.COLUMN_PP, pp);

        db.update(AppContract.UserEntry.TABLE_NAME,
                values,
                AppContract.UserEntry._ID + "=?",
                new String[]{String.valueOf(userId)});
    }


}
