package com.example.habitquest.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

public class UserRepository {

    private SQLiteDatabase database;
    private SQLiteHelper dbHelper;

    public UserRepository(Context context) {
        dbHelper = new SQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public long insertUser(String email, String username, String password, int avatar) {
        Log.i("REZ_DB", "insertUser to database");
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_EMAIL, email);
        values.put(SQLiteHelper.COLUMN_USERNAME, username);
        values.put(SQLiteHelper.COLUMN_PASSWORD, password);
        values.put("avatar", avatar);

        return database.insert(SQLiteHelper.TABLE_USERS, null, values);
    }

    public Cursor getUser(Long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i("REZ_DB", "getUser from database - queryBuilder");
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        if (id != null) {
            queryBuilder.appendWhere(SQLiteHelper.COLUMN_ID + "=" + id);
        }
        queryBuilder.setTables(SQLiteHelper.TABLE_USERS);

        return queryBuilder.query(
                database,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    public int updateUser(long id, String email, String username, String password, int avatar) {
        ContentValues values = new ContentValues();
        values.put(SQLiteHelper.COLUMN_EMAIL, email);
        values.put(SQLiteHelper.COLUMN_USERNAME, username);
        values.put(SQLiteHelper.COLUMN_PASSWORD, password);
        values.put("avatar", avatar);

        String whereClause = SQLiteHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return database.update(SQLiteHelper.TABLE_USERS, values, whereClause, whereArgs);
    }

    public int deleteUser(long id) {
        String whereClause = SQLiteHelper.COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return database.delete(SQLiteHelper.TABLE_USERS, whereClause, whereArgs);
    }
}
