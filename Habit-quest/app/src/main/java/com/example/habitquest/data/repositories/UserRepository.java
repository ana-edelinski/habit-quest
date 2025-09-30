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
import com.example.habitquest.utils.LevelUtils;
import com.example.habitquest.utils.ProgressPointsUtils;
import com.example.habitquest.utils.RepositoryCallback;
import com.example.habitquest.utils.TitleUtils;

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
    public void loginUser(String email, String password, RepositoryCallback<User> callback) {
        remoteDataSource.loginUser(email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User remoteUser) {
                // Uzmi localId iz SQLite baze
                Long localId = localDataSource.getUserIdByEmail(email);
                if (localId == null) {
                    // User ne postoji lokalno → kreiraj ga
                    localId = localDataSource.insertUser(
                            email,
                            remoteUser.getUsername(),
                            password,
                            remoteUser.getAvatar()
                    );
                }
                if (localId != null) {
                    remoteUser.setId(localId);
                }
                callback.onSuccess(remoteUser);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    public void changePassword(String oldPassword, String newPassword, RepositoryCallback<Void> callback) {
        remoteDataSource.changePassword(oldPassword, newPassword, callback);
    }

    public void updateUserXp(long localUserId, String remoteUid, int newXp, RepositoryCallback<Void> cb) {
        int newLevel = LevelUtils.calculateLevelFromXp(newXp);
        String newTitle = TitleUtils.getTitleForLevel(newLevel);
        int newPp = ProgressPointsUtils.getTotalPPForLevel(newLevel);

        remoteDataSource.updateUserXpAndLevel(remoteUid, newXp, newLevel, newTitle, newPp, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                localDataSource.updateUserXpAndLevel(localUserId, newXp, newLevel, newTitle, newPp);
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                localDataSource.updateUserXpAndLevel(localUserId, newXp, newLevel, newTitle, newPp);
                cb.onFailure(e);
            }
        });
    }



    public void getUser(String remoteUid, RepositoryCallback<User> cb) {
        remoteDataSource.getUser(remoteUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User remoteUser) {
                // Optionally update local cache
                Long localId = localDataSource.getUserIdByEmail(remoteUser.getEmail());
                if (localId != null) {
                    remoteUser.setId(localId);
                }
                cb.onSuccess(remoteUser);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }



}
