package com.example.habitquest.domain.repositoryinterfaces;

import android.database.Cursor;

import com.example.habitquest.utils.RepositoryCallback;

public interface IUserRepository {
    public void insertUser(String email, String username, String password, int avatar, RepositoryCallback<Void> callback);
    public void loginUser(String email, String password, RepositoryCallback<Void> callback);
}
