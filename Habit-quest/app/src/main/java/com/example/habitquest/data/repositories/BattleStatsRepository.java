package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.BattleStatsRemoteDataSource;
import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.domain.repositoryinterfaces.IBattleStatsRepository;
import com.example.habitquest.utils.RepositoryCallback;

public class BattleStatsRepository implements IBattleStatsRepository {

    private final BattleStatsRemoteDataSource remote;

    public BattleStatsRepository(Context ctx) {
        this.remote = new BattleStatsRemoteDataSource();
    }

    @Override
    public void getActiveBattle(String userId, RepositoryCallback<BattleStats> callback) {
        remote.getActiveBattle(userId, callback);
    }

    @Override
    public void saveBattle(BattleStats stats, RepositoryCallback<Void> callback) {
        remote.saveBattle(stats, callback);
    }

    @Override
    public void updateBattle(BattleStats stats, RepositoryCallback<Void> callback) {
        remote.updateBattle(stats, callback);
    }

    @Override
    public void deleteBattle(String userId, RepositoryCallback<Void> callback) {
        remote.deleteBattle(userId, callback);
    }
}
