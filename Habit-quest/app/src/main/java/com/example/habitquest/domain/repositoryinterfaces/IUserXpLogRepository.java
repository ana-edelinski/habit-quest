package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public interface IUserXpLogRepository {
    void insert(UserXpLog log, RepositoryCallback<UserXpLog> cb);
    void fetchAll(long userId, RepositoryCallback<List<UserXpLog>> cb);
    void deleteAllForUser(long userId, RepositoryCallback<Void> cb);
    public void getTotalXp(long userId, RepositoryCallback<Integer> cb);
}

