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
                        COLUMN_AVATAR + " INTEGER DEFAULT 1)";

    }
}
