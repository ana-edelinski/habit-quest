package com.example.habitquest.data.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.habitquest.data.local.db.AppContract;
import com.example.habitquest.data.local.db.SQLiteHelper;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;

public class UserRepository implements IUserRepository {

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

    @Override
    public long insertUser(String email, String username, String password, int avatar) {
        Log.i("REZ_DB", "insertUser to database");
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_EMAIL, email);
        values.put(AppContract.UserEntry.COLUMN_USERNAME, username);
        values.put(AppContract.UserEntry.COLUMN_PASSWORD, password);
        values.put(AppContract.UserEntry.COLUMN_AVATAR, avatar);

        return database.insert(AppContract.UserEntry.TABLE_NAME, null, values);
    }

    @Override
    public Cursor getUser(Long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i("REZ_DB", "getUser from database - queryBuilder");
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        if (id != null) {
            queryBuilder.appendWhere(AppContract.UserEntry._ID + "=" + id);
        }
        queryBuilder.setTables(AppContract.UserEntry.TABLE_NAME);

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

    @Override
    public int updateUser(long id, String email, String username, String password, int avatar) {
        ContentValues values = new ContentValues();
        values.put(AppContract.UserEntry.COLUMN_EMAIL, email);
        values.put(AppContract.UserEntry.COLUMN_USERNAME, username);
        values.put(AppContract.UserEntry.COLUMN_PASSWORD, password);
        values.put(AppContract.UserEntry.COLUMN_AVATAR, avatar);

        String whereClause = AppContract.UserEntry._ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return database.update(AppContract.UserEntry.TABLE_NAME, values, whereClause, whereArgs);
    }

    @Override
    public int deleteUser(long id) {
        String whereClause = AppContract.UserEntry._ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};
        return database.delete(AppContract.UserEntry.TABLE_NAME, whereClause, whereArgs);
    }
}
