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

public class UserRepository implements IUserRepository {

    private final UsersLocalDataSource localDataSource;
    private final UserRemoteDataSource remoteDataSource;

    public UserRepository(Context context) {
        this.localDataSource = new UsersLocalDataSource(context);
        this.remoteDataSource = new UserRemoteDataSource();
    }

    @Override
    public long insertUser(String email, String username, String password, int avatar) {
        long localId = localDataSource.insertUser(email, username, password, avatar);
        User user = new User(localId, email, username, password, avatar);
        remoteDataSource.insertUser(user);

        return localId;
    }

    @Override
    public Cursor getUser(Long id, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return localDataSource.getAllUsers();
    }
}
