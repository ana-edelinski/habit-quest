package com.example.habitquest.presentation.viewmodels;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.BossRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.managers.StagePerformanceCalculator;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.repositoryinterfaces.IBossRepository;
import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.Random;

public class BossFightViewModel extends ViewModel {

    private final IBossRepository bossRepository;
    private final UserRepository userRepository;
    private final StagePerformanceCalculator stagePerformanceCalculator; // 🆕 novi menadžer

    private final MutableLiveData<Boss> _currentBoss = new MutableLiveData<>();
    public LiveData<Boss> currentBoss = _currentBoss;

    private final MutableLiveData<BattleStats> _battleStats = new MutableLiveData<>();
    public LiveData<BattleStats> battleStats = _battleStats;

    private final MutableLiveData<BossFightResult> _battleResult = new MutableLiveData<>();
    public LiveData<BossFightResult> battleResult = _battleResult;

    private final Random random = new Random();
    private final AppPreferences prefs;

    // 🔹 Konstruktor sada prima i StagePerformanceCalculator
    public BossFightViewModel(
            AppPreferences prefs,
            BossRepository bossRepository,
            UserRepository userRepository,
            StagePerformanceCalculator stagePerformanceCalculator
    ) {
        this.prefs = prefs;
        this.bossRepository = bossRepository;
        this.userRepository = userRepository;
        this.stagePerformanceCalculator = stagePerformanceCalculator;

    }

    // ------------------ Inicijalizacija borbe ------------------

    public void prepareBattleData() {
        String uid = prefs.getFirebaseUid();

        // 1️⃣ prvo dohvatimo usera da bismo dobili njegov PP i lokalni ID
        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                int userPP = user.getPp();
                long localUserId = user.getId(); // koristi se u XP log repo-u

                // 2️⃣ izračunamo success rate preko StagePerformanceCalculator
                stagePerformanceCalculator.calculateStageSuccessRate(uid, localUserId, new RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double successRate) {
                        // 3️⃣ kada dobijemo uspešnost, započinjemo borbu
                        startBattle(successRate, userPP);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        // fallback ako izračun ne uspe
                        startBattle(0.0, userPP);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    // ------------------ Učitavanje trenutnog bosa ------------------

    public void loadCurrentBoss() {
        String uid = prefs.getFirebaseUid();

        // 1️⃣ Dohvati usera da vidiš koji je njegov trenutni level
        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                // 2️⃣ Na osnovu levela formiraj bossId (npr. boss_1, boss_2, ...)
                String bossId = "boss_" + user.getLevel();

                // 3️⃣ Sada dohvati odgovarajućeg bossa
                bossRepository.getCurrentBoss(bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss boss) {
                        if (boss != null) {
                            boss.setHp(boss.getMaxHp());
                            _currentBoss.postValue(boss);
                        } else {
                            // Ako nema bossa u bazi, napravi ga
                            Boss firstBoss = new Boss(user.getLevel(), 100, 200);
                            firstBoss.setId(bossId);

                            bossRepository.saveBoss(firstBoss, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    _currentBoss.postValue(firstBoss);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }


    // ------------------ Početak borbe ------------------

    public void startBattle(double successRate, int userPP) {
        Boss boss = _currentBoss.getValue();
        if (boss == null) return;

        BattleStats stats = new BattleStats(
                5,              // broj napada
                successRate,    // šansa za uspeh napada (0–100)
                userPP,         // snaga korisnika
                boss.getMaxHp() // HP bosa
        );

        _battleStats.setValue(stats);
    }

    // ------------------ Logika napada ------------------

    public void performAttack() {
        BattleStats stats = _battleStats.getValue();
        Boss boss = _currentBoss.getValue();
        if (stats == null || boss == null || stats.isBattleOver()) return;

        int roll = random.nextInt(100); // 0–99
        boolean hitSuccess = roll < stats.getSuccessRate();

        if (hitSuccess) {
            int newHP = Math.max(boss.getHp() - stats.getUserPP(), 0);
            boss.setHp(newHP);
            stats.setBossHP(newHP);
            stats.setHitsLanded(stats.getHitsLanded() + 1);
        }

        stats.setRemainingAttempts(stats.getRemainingAttempts() - 1);

        if (stats.getRemainingAttempts() == 0 || boss.getHp() <= 0) {
            stats.setBattleOver(true);
            stats.setVictory(boss.getHp() <= 0);
            handleBattleEnd(stats, boss);
        }

        _battleStats.setValue(stats);
    }

    // ------------------ Kraj borbe ------------------

    private void handleBattleEnd(BattleStats stats, Boss boss) {
        boolean victory = stats.isVictory();
        int earnedCoins = 0;
        String equipmentId = null;

        if (victory) {
            earnedCoins = boss.getRewardCoins();

            // 20% šansa da dobije opremu (95% odeća, 5% oružje)
            if (random.nextInt(100) < 20) {
                equipmentId = (random.nextInt(100) < 95)
                        ? "equipment_clothing"
                        : "equipment_weapon";
            }

            boss.setDefeated(true);
        } else if (boss.getHp() <= boss.getMaxHp() / 2) {
            earnedCoins = boss.getRewardCoins() / 2;
        }

        BossFightResult result = new BossFightResult(
                boss.getId(),
                prefs.getFirebaseUid(),
                victory,
                earnedCoins,
                equipmentId
        );

        bossRepository.saveBattleResult(result, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _battleResult.postValue(result);
                bossRepository.updateBoss(boss, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) { /* OK */ }
                    @Override
                    public void onFailure(Exception e) { e.printStackTrace(); }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }


    /**
     * Kreira sledećeg bossa kada korisnik dostigne novi nivo.
     */
    public void createNextBoss(RepositoryCallback<Boss> callback) {
        String uid = prefs.getFirebaseUid();

        // 1️⃣ Prvo dohvati korisnika da saznaš njegov trenutni level
        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                // 2️⃣ Formiraj bossId na osnovu njegovog trenutnog levela
                String bossId = "boss_" + user.getLevel();

                // 3️⃣ Dohvati trenutnog bossa po tom ID-ju (ako postoji)
                bossRepository.getCurrentBoss(bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss currentBoss) {
                        if (currentBoss != null) {
                            // ✅ Kreiraj sledećeg bossa na osnovu prethodnog
                            bossRepository.createNextBoss(currentBoss, new RepositoryCallback<Boss>() {
                                @Override
                                public void onSuccess(Boss newBoss) {
                                    _currentBoss.postValue(newBoss);
                                    callback.onSuccess(newBoss);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    e.printStackTrace();
                                    callback.onFailure(e);
                                }
                            });
                        } else {
                            // ✅ Ako nema trenutnog, kreiraj prvog
                            Boss firstBoss = new Boss(
                                    1,      // level
                                    100,    // HP
                                    200     // rewardCoins
                            );
                            firstBoss.setId("boss_1");

                            bossRepository.saveBoss(firstBoss, new RepositoryCallback<Void>() {
                                @Override
                                public void onSuccess(Void data) {
                                    _currentBoss.postValue(firstBoss);
                                    callback.onSuccess(firstBoss);
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    e.printStackTrace();
                                    callback.onFailure(e);
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        callback.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }


}

