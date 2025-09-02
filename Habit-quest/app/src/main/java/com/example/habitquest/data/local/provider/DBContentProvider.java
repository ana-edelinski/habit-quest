package com.example.habitquest.data.local.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.habitquest.data.local.db.AppContract;
import com.example.habitquest.data.local.db.SQLiteHelper;

// Implementira ContentProvider za pristup bazi podataka.
// Omogucava aplikaciji (i potencijalno drugim aplikacijama) da koriste
// standardizovane URI-je za CRUD operacije nad tabelom USERS.
public class DBContentProvider extends ContentProvider {
    private SQLiteHelper database;
    private static final int USERS = 5;
    private static final int USER_ID = 20;
    private static final String AUTHORITY = "com.example.habitquest";
    private static final String USERS_PATH = "users";
    public static final Uri CONTENT_URI_USERS = Uri.parse("content://" + AUTHORITY + "/" + USERS_PATH);
    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * content://com.example.habitquest/users
     * content://com.example.habitquest/users/1
     */
    static {
        sURIMatcher.addURI(AUTHORITY, USERS_PATH, USERS);
        sURIMatcher.addURI(AUTHORITY, USERS_PATH + "/#", USER_ID);
    }

    @Override
    public boolean onCreate() {
        Log.i("REZ_DB", "ON CREATE CONTENT PROVIDER");
        database = new SQLiteHelper(getContext());
        return true;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        if (database != null) {
            database.close();
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.i("REZ_DB", "QUERY");
        // Using SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a column which does not exist
        //checkColumns(projection);
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case USER_ID:
                // Adding the ID to the original query
                queryBuilder.appendWhere(AppContract.UserEntry._ID + "=" + uri.getLastPathSegment());
            case USERS:
                // Set the table
                queryBuilder.setTables(AppContract.UserEntry.TABLE_NAME);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getReadableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        // make sure that potential listeners are getting notified
        if (cursor != null && getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        } else {
            Log.e("REZ_DB", "Cursor or ContentResolver is null.");
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    //    ContentValues - parovi kljuc vrednost.
    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.i("REZ_DB", "INSERT");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case USERS:
                id = sqlDB.insert(AppContract.UserEntry.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        // resolver salje upis provideru
        // provider vraca informacije resolveru
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return Uri.parse(USERS_PATH + "/" + id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.i("REZ_DB", "DELETE");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case USERS:
                rowsDeleted = sqlDB.delete(AppContract.UserEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case USER_ID:
                String idUSER = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(AppContract.UserEntry.TABLE_NAME,
                            AppContract.UserEntry._ID + "=" + idUSER,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(AppContract.UserEntry.TABLE_NAME,
                            AppContract.UserEntry._ID + "=" + idUSER +
                                    (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.i("REZ_DB", "UPDATE");
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case USERS:
                rowsUpdated = sqlDB.update(AppContract.UserEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case USER_ID:
                String idUSER = uri.getLastPathSegment();
                rowsUpdated = sqlDB.update(AppContract.UserEntry.TABLE_NAME, values,
                        AppContract.UserEntry._ID + "=" + idUSER +
                                (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : ""),
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (getContext() != null) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}

