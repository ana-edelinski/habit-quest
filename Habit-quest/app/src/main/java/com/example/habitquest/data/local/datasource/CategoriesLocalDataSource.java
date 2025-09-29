package com.example.habitquest.data.local.datasource;

import static com.example.habitquest.data.local.db.AppContract.CategoryEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.habitquest.data.local.db.SQLiteHelper;
import com.example.habitquest.domain.model.Category;

import java.util.ArrayList;
import java.util.List;

public class CategoriesLocalDataSource {

    private final SQLiteHelper dbHelper;

    public CategoriesLocalDataSource(Context context) {
        this.dbHelper = new SQLiteHelper(context);
    }

    /** Zameni sve kategorije tog korisnika novom listom (korisno kada stigne snapshot sa remote-a). */
    public void replaceAll(long userId, List<Category> list) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(CategoryEntry.TABLE_NAME,
                    CategoryEntry.COLUMN_USER_ID + "=?",
                    new String[]{ String.valueOf(userId) });

            if (list != null) {
                for (Category c : list) {
                    c.setUserId(userId);
                    if (c.getColorHex() != null) {
                        c.setColorHex(c.getColorHex().toUpperCase());
                    }
                    insertOrReplace(db, c);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Upsert po firestoreId + userId. */
    public long upsert(long userId, Category c) {
        c.setUserId(userId);
        if (c.getColorHex() != null) c.setColorHex(c.getColorHex().toUpperCase());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = toValues(c);

        int affected = db.update(
                CategoryEntry.TABLE_NAME,
                values,
                CategoryEntry.COLUMN_FIRESTORE_ID + "=? AND " + CategoryEntry.COLUMN_USER_ID + "=?",
                new String[]{ c.getId(), String.valueOf(userId) }
        );
        if (affected == 0) {
            return insertOrReplace(db, c);
        }
        return affected;
    }

    /** Brisanje po firestoreId i userId; vraća broj pogođenih redova. */
    public int delete(long userId, String firestoreId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                CategoryEntry.TABLE_NAME,
                CategoryEntry.COLUMN_FIRESTORE_ID + "=? AND " + CategoryEntry.COLUMN_USER_ID + "=?",
                new String[]{ firestoreId, String.valueOf(userId) }
        );
    }

    /** Sve kategorije korisnika (iz lokalnog keša). */
    public List<Category> getAllByUser(long userId) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cur = db.query(
                CategoryEntry.TABLE_NAME,
                null,
                CategoryEntry.COLUMN_USER_ID + "=?",
                new String[]{ String.valueOf(userId) },
                null, null,
                CategoryEntry.COLUMN_NAME + " COLLATE NOCASE ASC"
        );
        try {
            List<Category> out = new ArrayList<>();
            while (cur.moveToNext()) out.add(fromCursor(cur));
            return out;
        } finally {
            cur.close();
        }
    }

    /** Da li je boja zauzeta za datog korisnika; excludeFirestoreId je opcioni (za EDIT formu). */
    public boolean isColorTaken(long userId, String colorHex, String excludeFirestoreId) {
        String normalized = colorHex != null ? colorHex.toUpperCase() : null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sel = CategoryEntry.COLUMN_USER_ID + "=? AND " + CategoryEntry.COLUMN_COLOR_HEX + "=?";
        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        args.add(normalized);

        if (excludeFirestoreId != null) {
            sel += " AND " + CategoryEntry.COLUMN_FIRESTORE_ID + "!=?";
            args.add(excludeFirestoreId);
        }

        Cursor c = db.query(CategoryEntry.TABLE_NAME,
                new String[]{CategoryEntry._ID},
                sel, args.toArray(new String[0]),
                null, null, null, "1");
        try {
            return c.moveToFirst();
        } finally {
            c.close();
        }
    }

    // ----------------- helpers -----------------

    private long insertOrReplace(SQLiteDatabase db, Category c) {
        ContentValues v = toValues(c);
        return db.insert(CategoryEntry.TABLE_NAME, null, v);
    }

    private static ContentValues toValues(Category c) {
        ContentValues v = new ContentValues();
        if (c.getId() != null) v.put(CategoryEntry.COLUMN_FIRESTORE_ID, c.getId()); // Firestore ID
        v.put(CategoryEntry.COLUMN_USER_ID,   c.getUserId());
        v.put(CategoryEntry.COLUMN_NAME,      c.getName());
        v.put(CategoryEntry.COLUMN_COLOR_HEX, c.getColorHex());
        v.put(CategoryEntry.COLUMN_CREATED_AT,c.getCreatedAt());
        v.put(CategoryEntry.COLUMN_UPDATED_AT,c.getUpdatedAt());
        return v;
    }

    private static Category fromCursor(Cursor cur) {
        Category c = new Category();
        c.setId(cur.getString(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_FIRESTORE_ID))); // Firestore ID
        c.setUserId(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_USER_ID)));
        c.setName(cur.getString(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME)));
        c.setColorHex(cur.getString(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_COLOR_HEX)));
        c.setCreatedAt(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_CREATED_AT)));
        c.setUpdatedAt(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_UPDATED_AT)));
        return c;
    }
}
