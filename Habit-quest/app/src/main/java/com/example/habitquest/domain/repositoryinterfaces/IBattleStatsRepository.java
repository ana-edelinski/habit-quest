package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.utils.RepositoryCallback;

public interface IBattleStatsRepository {

    void getActiveBattle(String userId, RepositoryCallback<BattleStats> callback);

    void saveBattle(BattleStats stats, RepositoryCallback<Void> callback);

    void updateBattle(BattleStats stats, RepositoryCallback<Void> callback);

    void deleteBattle(String userId, RepositoryCallback<Void> callback);
}

