package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.UserXpLogRemoteDataSource;
import com.example.habitquest.domain.model.DifficultyLevel;
import com.example.habitquest.domain.model.ImportanceLevel;
import com.example.habitquest.domain.model.Task;
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
    public void fetchAll(long userId,String firebaseUid, RepositoryCallback<List<UserXpLog>> cb) {
        remote.fetchAll(firebaseUid, new RepositoryCallback<List<UserXpLog>>() {
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
    public void deleteAllForUser(long userId, String firebaseUid, RepositoryCallback<Void> cb) {
        remote.deleteAllForUser(firebaseUid, new RepositoryCallback<Void>() {
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
    public void getTotalXp(long userId, String firebaseUid, RepositoryCallback<Integer> cb) {
        // Probaj remote
        remote.fetchAll(firebaseUid, new RepositoryCallback<List<UserXpLog>>() {
            @Override
            public void onSuccess(List<UserXpLog> result) {
                local.deleteAllForUser(userId);
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



    public void checkQuotaForTask(Task task, String firebaseUid, RepositoryCallback<Boolean> cb) {
        DifficultyLevel diff = DifficultyLevel.fromXp(task.getDifficultyXp());
        ImportanceLevel imp = ImportanceLevel.fromXp(task.getImportanceXp());

        // Extreme → max 1 nedeljno
        if (diff == DifficultyLevel.EXTREME) {
            remote.countLogsForThisWeek(firebaseUid, diff, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    cb.onSuccess(count < 1);
                }
                @Override public void onFailure(Exception e) { cb.onFailure(e); }
            });
            return;
        }

        // Hard i Very important → max 2 dnevno
        if (diff == DifficultyLevel.HARD || imp == ImportanceLevel.EXTREME_IMPORTANT) {
            remote.countLogsForToday(firebaseUid, diff, null, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    cb.onSuccess(count < 2);
                }
                @Override public void onFailure(Exception e) { cb.onFailure(e); }
            });
            return;
        }

        // Easy i Important → max 5 dnevno
        if (diff == DifficultyLevel.EASY || imp == ImportanceLevel.IMPORTANT) {
            remote.countLogsForToday(firebaseUid, diff, null, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    cb.onSuccess(count < 5);
                }
                @Override public void onFailure(Exception e) { cb.onFailure(e); }
            });
            return;
        }

        // Very easy i Normal → max 5 dnevno
        if (diff == DifficultyLevel.VERY_EASY || imp == ImportanceLevel.NORMAL) {
            remote.countLogsForToday(firebaseUid, diff, null, new RepositoryCallback<Integer>() {
                @Override
                public void onSuccess(Integer count) {
                    cb.onSuccess(count < 5);
                }
                @Override public void onFailure(Exception e) { cb.onFailure(e); }
            });
            return;
        }







//        // Special → max 1 mesečno
//        if (imp == ImportanceLevel.SPECIAL) {
//            remote.countLogsForThisMonth(firebaseUid, imp, new RepositoryCallback<Integer>() {
//                @Override
//                public void onSuccess(Integer count) {
//                    cb.onSuccess(count < 1);
//                }
//                @Override public void onFailure(Exception e) { cb.onFailure(e); }
//            });
//            return;
//        }

        // default → nema limita
        cb.onSuccess(true);
    }



}

