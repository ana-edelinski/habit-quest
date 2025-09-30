package com.example.habitquest.data.local.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

// Klasa koja upravlja kreiranjem i verzijama baze podataka.
// Nasledjuje SQLiteOpenHelper i koristi AppContract za definiciju sema.
// Brine se o onCreate i onUpgrade metodama baze.
public class SQLiteHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habitquest.db";
    private static final int DATABASE_VERSION = 17;

    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("REZ_DB", "ON CREATE SQLITE HELPER");
        db.execSQL(AppContract.UserEntry.CREATE_TABLE);
        db.execSQL(AppContract.CategoryEntry.CREATE_TABLE);
        db.execSQL(AppContract.TaskEntry.CREATE_TABLE);
        db.execSQL(AppContract.UserXpLogEntry.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("REZ_DB", "onUpgrade DB");
        db.execSQL("DROP TABLE IF EXISTS " + AppContract.UserEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AppContract.CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AppContract.TaskEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AppContract.UserXpLogEntry.TABLE_NAME);
        onCreate(db);
    }

}