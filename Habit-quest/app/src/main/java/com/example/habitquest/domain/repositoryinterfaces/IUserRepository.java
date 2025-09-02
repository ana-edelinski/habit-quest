package com.example.habitquest.domain.repositoryinterfaces;

import android.database.Cursor;

public interface IUserRepository {
    long insertUser(String email, String username, String password, int avatar);
    Cursor getUser(Long id, String[] projection, String selection, String[] selectionArgs, String sortOrder);
}
