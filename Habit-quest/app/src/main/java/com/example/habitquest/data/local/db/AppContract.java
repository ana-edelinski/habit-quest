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

    public static final class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "CATEGORIES";
        public static final String COLUMN_FIRESTORE_ID    = "firestoreId";     // TEXT (ID dokumenta u Firestore-u)
        public static final String COLUMN_USER_ID   = "userId";     // LONG (ako ti je uid String, promeni u TEXT)
        public static final String COLUMN_NAME      = "name";       // TEXT NOT NULL
        public static final String COLUMN_COLOR_HEX = "colorHex";   // TEXT NOT NULL, format #RRGGBB
        public static final String COLUMN_CREATED_AT= "createdAt";  // INTEGER (epoch millis)
        public static final String COLUMN_UPDATED_AT= "updatedAt";  // INTEGER (epoch millis)

        public static final String CREATE_TABLE =
                "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                        CategoryEntry._ID               + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        CategoryEntry.COLUMN_USER_ID    + " INTEGER NOT NULL, " +
                        CategoryEntry.COLUMN_FIRESTORE_ID    + " TEXT UNIQUE, " +
                        CategoryEntry.COLUMN_NAME       + " TEXT NOT NULL, " +
                        CategoryEntry.COLUMN_COLOR_HEX  + " TEXT NOT NULL, " +
                        CategoryEntry.COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                        CategoryEntry.COLUMN_UPDATED_AT + " INTEGER NOT NULL, " +
                        // jedinstveno po korisniku:
                        "UNIQUE (" + CategoryEntry.COLUMN_USER_ID + ", " + CategoryEntry.COLUMN_NAME + "), " +
                        "UNIQUE (" + CategoryEntry.COLUMN_USER_ID + ", " + CategoryEntry.COLUMN_COLOR_HEX + ")" +
                        ");";
    }

    public static final class TaskEntry implements BaseColumns {
        public static final String TABLE_NAME = "TASKS";

        public static final String COLUMN_USER_ID       = "userId";        // LONG (lokalni ID korisnika)
        public static final String COLUMN_FIRESTORE_ID    = "firestoreId";     // TEXT (ID dokumenta u Firestore-u)
        public static final String COLUMN_CATEGORY_ID   = "categoryId";    // TEXT (FK na kategoriju)
        public static final String COLUMN_NAME          = "name";          // TEXT NOT NULL
        public static final String COLUMN_DESCRIPTION   = "description";   // TEXT (opciono)

        // Jednokratni zadaci
        public static final String COLUMN_DATE_TIME     = "date";      // INTEGER (epoch millis)

        // Ponavljajući zadaci
        public static final String COLUMN_START_DATE_TIME = "startDate"; // INTEGER (epoch millis)
        public static final String COLUMN_END_DATE_TIME   = "endDate";   // INTEGER (epoch millis)
        public static final String COLUMN_INTERVAL        = "interval";      // INTEGER (npr. 2 dana / 3 nedelje)
        public static final String COLUMN_UNIT            = "unit";          // TEXT (day / week)

        // XP vrednosti
        public static final String COLUMN_DIFFICULTY_XP = "difficultyXp";  // INTEGER (XP za težinu)
        public static final String COLUMN_IMPORTANCE_XP = "importanceXp";  // INTEGER (XP za bitnost)
        public static final String COLUMN_TOTAL_XP      = "totalXp";       // INTEGER (ukupno XP)

        // Status (ACTIVE, PAUSED, COMPLETED, CANCELED)
        public static final String COLUMN_STATUS        = "status";        // TEXT NOT NULL

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID        + " INTEGER NOT NULL, " +
                        COLUMN_FIRESTORE_ID    + " TEXT UNIQUE, " +
                        COLUMN_CATEGORY_ID    + " TEXT NOT NULL, " +
                        COLUMN_NAME           + " TEXT NOT NULL, " +
                        COLUMN_DESCRIPTION    + " TEXT, " +
                        COLUMN_DATE_TIME      + " INTEGER, " +
                        COLUMN_START_DATE_TIME+ " INTEGER, " +
                        COLUMN_END_DATE_TIME  + " INTEGER, " +
                        COLUMN_INTERVAL       + " INTEGER, " +
                        COLUMN_UNIT           + " TEXT, " +
                        COLUMN_DIFFICULTY_XP  + " INTEGER NOT NULL, " +
                        COLUMN_IMPORTANCE_XP  + " INTEGER NOT NULL, " +
                        COLUMN_TOTAL_XP       + " INTEGER NOT NULL, " +
                        COLUMN_STATUS         + " TEXT NOT NULL DEFAULT 'ACTIVE'" +
                        ");";
    }


    public static final class UserXpLogEntry implements BaseColumns {
        public static final String TABLE_NAME = "USER_XP_LOG";
        public static final String COLUMN_FIRESTORE_ID    = "firestoreId";     // TEXT (ID dokumenta u Firestore-u)

        public static final String COLUMN_USER_ID      = "userId";       // LONG (FK User)
        public static final String COLUMN_TASK_ID      = "taskId";       // String (FK Task)

        public static final String COLUMN_OCCURRENCE_ID= "occurrenceId"; // String (FK TaskOccurrence) može biti NULL
        public static final String COLUMN_XP_GAINED    = "xpGained";     // INTEGER
        public static final String COLUMN_COMPLETED_AT = "completedAt";  // INTEGER (epoch millis)

        public static final String CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_USER_ID       + " INTEGER NOT NULL, " +
                        COLUMN_FIRESTORE_ID    + " TEXT UNIQUE, " +
                        COLUMN_TASK_ID       + " TEXT NOT NULL, " +
                        COLUMN_OCCURRENCE_ID + " TEXT, " +
                        COLUMN_XP_GAINED     + " INTEGER NOT NULL, " +
                        COLUMN_COMPLETED_AT  + " INTEGER NOT NULL" +
                        ");";
    }



}
