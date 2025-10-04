package com.example.habitquest.domain.repositoryinterfaces;



import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.utils.RepositoryCallback;

public interface IBossRepository {
    void getCurrentBoss(String bossId, RepositoryCallback<Boss> callback);
    void createNextBoss(Boss previousBoss, RepositoryCallback<Boss> callback);
    void updateBoss(Boss boss, RepositoryCallback<Void> callback);
    void saveBattleResult(BossFightResult result, RepositoryCallback<Void> callback);
    void saveBoss(Boss boss, RepositoryCallback<Void> callback);
}

