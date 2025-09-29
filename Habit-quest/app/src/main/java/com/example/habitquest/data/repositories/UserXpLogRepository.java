package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.UserXpLogRemoteDataSource;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.domain.repositoryinterfaces.IUserXpLogRepository;
import com.example.habitquest.utils.RepositoryCallback;
import com.example.habitquest.data.local.datasource.UserXpLogLocalDataSource;

import java.util.List;

public class UserXpLogRepository implements IUserXpLogRepository {
    private final UserXpLogLocalDataSource local;
    private final UserXpLogRemoteDataSource remote;

    public UserXpLogRepository(Context ctx) {
        local = new UserXpLogLocalDataSource(ctx);
        remote = new UserXpLogRemoteDataSource();
    }

    @Override
    public void insert(UserXpLog log, RepositoryCallback<UserXpLog> cb) {
        // upiši remote
        remote.insert(log, new RepositoryCallback<UserXpLog>() {
            @Override
            public void onSuccess(UserXpLog result) {
                local.insert(result);
                cb.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void fetchAll(long userId, RepositoryCallback<List<UserXpLog>> cb) {
        remote.fetchAll(userId, new RepositoryCallback<List<UserXpLog>>() {
            @Override
            public void onSuccess(List<UserXpLog> result) {
                // prvo očisti lokalne logove
                local.deleteAllForUser(userId);

                // pa upiši sveže sa remote
                for (UserXpLog log : result) {
                    local.upsert(log);
                }
                cb.onSuccess(result);
            }
            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }


    @Override
    public void deleteAllForUser(long userId, RepositoryCallback<Void> cb) {
        remote.deleteAllForUser(userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                local.deleteAllForUser(userId);
                cb.onSuccess(null);
            }
            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }


    @Override
    public void getTotalXp(long userId, RepositoryCallback<Integer> cb) {
        // Probaj remote
        remote.fetchAll(userId, new RepositoryCallback<List<UserXpLog>>() {
            @Override
            public void onSuccess(List<UserXpLog> result) {
                // upiši sve logove lokalno
                for (UserXpLog log : result) {
                    local.upsert(log);
                }

                // izračunaj XP sumu
                int totalXp = 0;
                for (UserXpLog log : result) {
                    totalXp += log.getXpGained();
                }
                cb.onSuccess(totalXp);
            }

            @Override
            public void onFailure(Exception e) {
                // fallback → izračunaj XP iz lokalne baze
                List<UserXpLog> localLogs = local.getAllForUser(userId);
                int totalXp = 0;
                for (UserXpLog log : localLogs) {
                    totalXp += log.getXpGained();
                }
                cb.onSuccess(totalXp); // i dalje vrati XP, iako remote nije uspeo
            }
        });
    }

}

