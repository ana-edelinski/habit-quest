package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.DifficultyLevel;
import com.example.habitquest.domain.model.ImportanceLevel;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public interface IUserXpLogRepository {
    void insert(UserXpLog log, RepositoryCallback<UserXpLog> cb);
    void fetchAll(long userId, String firebaseUid, RepositoryCallback<List<UserXpLog>> cb);
    void deleteAllForUser(long userId, String firebaseUid, RepositoryCallback<Void> cb);
    public void getTotalXp(long userId, String firebaseUid, RepositoryCallback<Integer> cb);
    public void checkQuotaForTask(Task task, String firebaseUid, RepositoryCallback<Boolean> cb);


}

