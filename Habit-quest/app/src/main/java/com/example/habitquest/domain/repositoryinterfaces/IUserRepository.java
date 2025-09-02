package com.example.habitquest.domain.repositoryinterfaces;

import android.database.Cursor;

public interface IUserRepository {
    void open();
    void close();
    long insertUser(String email, String username, String password, int avatar);
    Cursor getUser(Long id, String[] projection, String selection, String[] selectionArgs, String sortOrder);
    int updateUser(long id, String email, String username, String password, int avatar);
    int deleteUser(long id);
}
