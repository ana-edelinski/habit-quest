package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.TaskRemoteDataSource;
import com.example.habitquest.domain.repositoryinterfaces.IBossRepository;
import com.example.habitquest.data.remote.BossRemoteDataSource;
import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.utils.RepositoryCallback;

public class BossRepository implements IBossRepository {

    private final BossRemoteDataSource remoteDataSource;

    public BossRepository(Context context) {
        remoteDataSource = new BossRemoteDataSource();
    }

    @Override
    public void getCurrentBoss(String bossId, RepositoryCallback<Boss> callback) {
        remoteDataSource.getCurrentBoss(bossId, callback);
    }

    @Override
    public void createNextBoss(Boss previousBoss, RepositoryCallback<Boss> callback) {
        // generiši novog bosa po formuli
        int newLevel = previousBoss.getLevel() + 1;
        int newMaxHp = (int) Math.round(previousBoss.getMaxHp() * 2.5);
        int newReward = (int) Math.round(previousBoss.getRewardCoins() * 1.2);

        Boss newBoss = new Boss(newLevel, newMaxHp, newReward);
        newBoss.setId("boss_" + newLevel);

        remoteDataSource.createNextBoss(newBoss, callback);
    }

    @Override
    public void updateBoss(Boss boss, RepositoryCallback<Void> callback) {
        remoteDataSource.updateBoss(boss, callback);
    }

    @Override
    public void saveBattleResult(BossFightResult result, RepositoryCallback<Void> callback) {
        remoteDataSource.saveBattleResult(result, callback);
    }

    public void saveBoss(Boss boss, RepositoryCallback<Void> callback) {
        remoteDataSource.saveBoss(boss, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }




}

