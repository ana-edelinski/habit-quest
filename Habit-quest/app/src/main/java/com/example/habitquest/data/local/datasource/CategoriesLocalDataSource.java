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
    public void replaceAll(String userIdStr, List<Category> list) {
        long userId = parseUserIdOrThrow(userIdStr);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            db.delete(CategoryEntry.TABLE_NAME,
                    CategoryEntry.COLUMN_USER_ID + "=?",
                    new String[]{ String.valueOf(userId) });

            if (list != null) {
                for (Category c : list) {
                    // osiguraj userId i normalizuj boju radi UNIQUE(userId, colorHex)
                    c.setUserId(userId);
                    if (c.getColorHex() != null) {
                        c.setColorHex(c.getColorHex().toUpperCase());
                    }
                    insertWithId(db, c);
                }
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    /** Upsert: ako nema _id → insert; ako ima → update (po _id i userId). Vraća rowId. */
    public long upsert(Long userId, Category c) {
        c.setUserId(userId);
        if (c.getColorHex() != null) c.setColorHex(c.getColorHex().toUpperCase());

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        if (c.getId() == null || c.getId() == 0L) {
            return insertInternal(db, c);
        } else {
            ContentValues values = toValues(c, /*includeId*/ false);
            int affected = db.update(
                    CategoryEntry.TABLE_NAME,
                    values,
                    CategoryEntry._ID + "=? AND " + CategoryEntry.COLUMN_USER_ID + "=?",
                    new String[]{ String.valueOf(c.getId()), String.valueOf(userId) }
            );
            if (affected == 0) {
                // Ako iz nekog razloga nema reda (npr. sync race), uradi insert
                return insertWithId(db, c);
            }
            return c.getId();
        }
    }

    /** Brisanje po _id i userId (bezbednije); vraća broj pogođenih redova. */
    public int delete(String userIdStr, Object categoryId) {
        long userId = parseUserIdOrThrow(userIdStr);
        long id = parseId(categoryId);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(
                CategoryEntry.TABLE_NAME,
                CategoryEntry._ID + "=? AND " + CategoryEntry.COLUMN_USER_ID + "=?",
                new String[]{ String.valueOf(id), String.valueOf(userId) }
        );
    }

    /** Sve kategorije korisnika (korisno za prikaz iz lokalnog keša). */
    public List<Category> getAllByUser(String userIdStr) {
        long userId = parseUserIdOrThrow(userIdStr);
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

    /** Da li je boja zauzeta za datog korisnika; excludeId je opcioni (za EDIT formu). */
    public boolean isColorTaken(String userIdStr, String colorHex, Long excludeIdOrNull) {
        long userId = parseUserIdOrThrow(userIdStr);
        String normalized = colorHex != null ? colorHex.toUpperCase() : null;

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String sel = CategoryEntry.COLUMN_USER_ID + "=? AND " + CategoryEntry.COLUMN_COLOR_HEX + "=?";
        ArrayList<String> args = new ArrayList<>();
        args.add(String.valueOf(userId));
        args.add(normalized);

        if (excludeIdOrNull != null) {
            sel += " AND " + CategoryEntry._ID + "!=?";
            args.add(String.valueOf(excludeIdOrNull));
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

    private long insertInternal(SQLiteDatabase db, Category c) {
        ContentValues v = toValues(c, /*includeId*/ false); // _id je AUTOINCREMENT
        long rowId = db.insert(CategoryEntry.TABLE_NAME, null, v);
        if (c.getId() == null || c.getId() == 0L) c.setId(rowId);
        return rowId;
    }

    private long insertWithId(SQLiteDatabase db, Category c) {
        ContentValues v = toValues(c, /*includeId*/ true); // upiši tačno ovaj ID
        long rowId = db.insert(CategoryEntry.TABLE_NAME, null, v);
        // ako si upisala ID, ostaje isti; možeš i da vratiš c.getId()
        return (rowId > 0) ? (c.getId() != null ? c.getId() : rowId) : rowId;
    }

    private long insertAuto(SQLiteDatabase db, Category c) {
        ContentValues v = toValues(c, /*includeId*/ false);
        long rowId = db.insert(CategoryEntry.TABLE_NAME, null, v);
        if (c.getId() == null || c.getId() == 0L) c.setId(rowId);
        return rowId;
    }

    private static ContentValues toValues(Category c, boolean includeId) {
        ContentValues v = new ContentValues();
        if (includeId && c.getId() != null) v.put(CategoryEntry._ID, c.getId());
        v.put(CategoryEntry.COLUMN_USER_ID,   c.getUserId());
        v.put(CategoryEntry.COLUMN_NAME,      c.getName());
        v.put(CategoryEntry.COLUMN_COLOR_HEX, c.getColorHex());
        v.put(CategoryEntry.COLUMN_CREATED_AT,c.getCreatedAt());
        v.put(CategoryEntry.COLUMN_UPDATED_AT,c.getUpdatedAt());
        return v;
    }

    private static Category fromCursor(Cursor cur) {
        Category c = new Category();
        c.setId(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry._ID)));
        c.setUserId(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_USER_ID)));
        c.setName(cur.getString(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_NAME)));
        c.setColorHex(cur.getString(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_COLOR_HEX)));
        c.setCreatedAt(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_CREATED_AT)));
        c.setUpdatedAt(cur.getLong(cur.getColumnIndexOrThrow(CategoryEntry.COLUMN_UPDATED_AT)));
        return c;
    }

    private static long parseUserIdOrThrow(String userIdStr) {
        try {
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            throw new IllegalArgumentException("Local DB expects numeric userId, got: " + userIdStr, e);
        }
    }

    private static long parseId(Object idObj) {
        if (idObj instanceof Long) return (Long) idObj;
        if (idObj instanceof Integer) return ((Integer) idObj).longValue();
        if (idObj instanceof String) {
            try { return Long.parseLong((String) idObj); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Unsupported id type: " + idObj);
    }
}
