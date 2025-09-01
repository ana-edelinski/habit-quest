package com.example.habitquest.tools;

import android.app.Activity;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.habitquest.database.DBContentProvider;
import com.example.habitquest.database.SQLiteHelper;

public class Util {
    public static void initDB(Activity activity) {
        SQLiteHelper dbHelper = new SQLiteHelper(activity);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Log.i("REZ_DB", "ENTRY INSERT TO DATABASE");
        {
            ContentValues entry = new ContentValues();
            entry.put(SQLiteHelper.COLUMN_EMAIL, "pera@example.com");
            entry.put(SQLiteHelper.COLUMN_USERNAME, "pera");
            entry.put(SQLiteHelper.COLUMN_PASSWORD, "123456789");
            entry.put(SQLiteHelper.COLUMN_AVATAR, 1);
            // content resolver salje neki zahtev content provideru sa nekim informacijama
            // i vracama nam se odgovor od content provider-a - u ovom slucaju uri
            activity.getContentResolver().insert(DBContentProvider.CONTENT_URI_USERS, entry);

            entry = new ContentValues();
            entry.put(SQLiteHelper.COLUMN_EMAIL, "zika@example.conm");
            entry.put(SQLiteHelper.COLUMN_USERNAME, "zika");
            entry.put(SQLiteHelper.COLUMN_PASSWORD, "123456789");
            entry.put(SQLiteHelper.COLUMN_AVATAR, 2);

            activity.getContentResolver().insert(DBContentProvider.CONTENT_URI_USERS, entry);
        }

        db.close();
    }
}