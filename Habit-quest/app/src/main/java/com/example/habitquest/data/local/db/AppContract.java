package com.example.habitquest.data.local.db;

import android.provider.BaseColumns;

// Sadrzi imena tabela, kolona i SQL upit za kreiranje tabela.
// Olaksava centralizovano menjanje sema baze.
public final class AppContract {
    private AppContract() {}

    public static class UserEntry implements BaseColumns {
        public static final String TABLE_NAME = "USERS";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_USERNAME = "username";
        public static final String COLUMN_PASSWORD = "password";
        public static final String COLUMN_AVATAR = "avatar";

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_EMAIL + " TEXT, " +
                        COLUMN_USERNAME + " TEXT, " +
                        COLUMN_PASSWORD + " TEXT, " +
                        COLUMN_AVATAR + " INTEGER DEFAULT 1, " +
                        "isVerified INTEGER DEFAULT 0)";
    }

    public static final class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "CATEGORIES";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_USER_ID   = "userId";     // LONG (ako ti je uid String, promeni u TEXT)
        public static final String COLUMN_NAME      = "name";       // TEXT NOT NULL
        public static final String COLUMN_COLOR_HEX = "colorHex";   // TEXT NOT NULL, format #RRGGBB
        public static final String COLUMN_CREATED_AT= "createdAt";  // INTEGER (epoch millis)
        public static final String COLUMN_UPDATED_AT= "updatedAt";  // INTEGER (epoch millis)

        public static final String CREATE_TABLE =
                "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                        CategoryEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        CategoryEntry.COLUMN_USER_ID    + " INTEGER NOT NULL, " +
                        CategoryEntry.COLUMN_NAME       + " TEXT NOT NULL, " +
                        CategoryEntry.COLUMN_COLOR_HEX  + " TEXT NOT NULL, " +
                        CategoryEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                        CategoryEntry.COLUMN_UPDATED_AT + " INTEGER NOT NULL, " +
                        // jedinstveno po korisniku:
                        "UNIQUE (" + CategoryEntry.COLUMN_USER_ID + ", " + CategoryEntry.COLUMN_NAME + "), " +
                        "UNIQUE (" + CategoryEntry.COLUMN_USER_ID + ", " + CategoryEntry.COLUMN_COLOR_HEX + ")" +
                        ");";
    }
}
