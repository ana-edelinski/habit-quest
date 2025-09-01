package com.example.habitquest.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLiteHelper extends SQLiteOpenHelper {

    public static final String TABLE_USERS = "USERS";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_USERNAME = "username";
    public static final String COLUMN_PASSWORD = "password";
    public static final String COLUMN_AVATAR = "avatar";

    //Dajemo ime bazi
    private static final String DATABASE_NAME = "habitquest.db";
    //i pocetnu verziju baze. Obicno krece od 1
    private static final int DATABASE_VERSION = 4;

    private static final String DB_CREATE = "create table "
            + TABLE_USERS + "("
            + COLUMN_ID  + " integer primary key autoincrement , "
            + COLUMN_EMAIL + " text, "
            + COLUMN_USERNAME + " text, "
            + COLUMN_PASSWORD + " text, "
            + COLUMN_AVATAR + " integer default 1"
            + ")";


    //Potrebno je dodati konstruktor zbog pravilne inicijalizacije
    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //Prilikom kreiranja baze potrebno je da pozovemo odgovarajuce metode biblioteke
    //prilikom kreiranja moramo pozvati db.execSQL za svaku tabelu koju imamo
    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.i("REZ_DB", "ON CREATE SQLITE HELPER");
        db.execSQL(DB_CREATE);
    }

    //kada zelimo da izmenimo tabele, moramo pozvati drop table za sve tabele koje imamo
    //  moramo voditi računa o podacima, pa ćemo onda raditi ovde migracije po potrebi
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("REZ_DB", "ON UPGRADE SQLITE HELPER");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

}