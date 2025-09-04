package com.example.habitquest.data.repositories;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.example.habitquest.data.local.datasource.UsersLocalDataSource;
import com.example.habitquest.data.local.db.AppContract;
import com.example.habitquest.data.local.db.SQLiteHelper;
import com.example.habitquest.data.remote.UserRemoteDataSource;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;
import com.example.habitquest.utils.RepositoryCallback;

public class UserRepository implements IUserRepository {

    private final UsersLocalDataSource localDataSource;
    private final UserRemoteDataSource remoteDataSource;

    public UserRepository(Context context) {
        this.localDataSource = new UsersLocalDataSource(context);
        this.remoteDataSource = new UserRemoteDataSource();
    }

    @Override
    public void insertUser(String email, String username, String password, int avatar,
                                 RepositoryCallback<Void> callback) {
        remoteDataSource.registerUser(email, password, username, avatar, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Ako uspešno registrovan remote → upiši i lokalno (cache)
                localDataSource.insertUser(email, username, password, avatar);
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    @Override
    public void loginUser(String email, String password, RepositoryCallback<Void> callback) {
        remoteDataSource.loginUser(email, password, callback);
    }

}
