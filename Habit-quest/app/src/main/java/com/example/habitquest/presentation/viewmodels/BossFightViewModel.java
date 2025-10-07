package com.example.habitquest.presentation.viewmodels;



import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.BattleStatsRepository;
import com.example.habitquest.data.repositories.BossRepository;
import com.example.habitquest.data.repositories.EquipmentRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.managers.StagePerformanceCalculator;
import com.example.habitquest.domain.model.ActiveEffects;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.PotentialRewards;
import com.example.habitquest.domain.model.ShopData;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.repositoryinterfaces.IBattleStatsRepository;
import com.example.habitquest.domain.repositoryinterfaces.IBossRepository;
import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;



public class BossFightViewModel extends ViewModel {

    private final IBossRepository bossRepository;
    private final IBattleStatsRepository battleStatsRepository;
    private final UserRepository userRepository;
    private final EquipmentRepository equipmentRepository;
    private final StagePerformanceCalculator stagePerformanceCalculator;

    private final MutableLiveData<Boss> _currentBoss = new MutableLiveData<>();
    public LiveData<Boss> currentBoss = _currentBoss;

    private final MutableLiveData<BattleStats> _battleStats = new MutableLiveData<>();
    public LiveData<BattleStats> battleStats = _battleStats;

    private final MutableLiveData<BossFightResult> _battleResult = new MutableLiveData<>();
    public LiveData<BossFightResult> battleResult = _battleResult;

    private final MutableLiveData<List<ShopItem>> _activeEquipment = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<ShopItem>> activeEquipment = _activeEquipment;

    private final MutableLiveData<PotentialRewards> _potentialRewards = new MutableLiveData<>();
    public LiveData<PotentialRewards> potentialRewards = _potentialRewards;



    private final Random random = new Random();
    private final AppPreferences prefs;

    public BossFightViewModel(
            AppPreferences prefs,
            BossRepository bossRepository,
            UserRepository userRepository,
            BattleStatsRepository battleStatsRepository,
            EquipmentRepository equipmentRepository,
            StagePerformanceCalculator stagePerformanceCalculator
    ) {
        this.prefs = prefs;
        this.bossRepository = bossRepository;
        this.userRepository = userRepository;
        this.battleStatsRepository = battleStatsRepository;
        this.equipmentRepository = equipmentRepository;
        this.stagePerformanceCalculator = stagePerformanceCalculator;
    }

    // ------------------ Inicijalizacija borbe ------------------

    public void prepareBattleData() {
        String uid = prefs.getFirebaseUid();

        // 🔹 prvo proveri da li već postoji aktivna borba
        battleStatsRepository.getActiveBattle(uid, new RepositoryCallback<BattleStats>() {
            @Override
            public void onSuccess(BattleStats activeBattle) {
                if (activeBattle != null && !activeBattle.isBattleOver()) {
                    // ✅ postojeća borba — nastavi odavde
                    recalculateStatsForActiveBattle(activeBattle);
                    _battleStats.postValue(activeBattle);
                } else {
                    // 🔹 nema aktivne borbe — započni novu
                    loadAndStartNewBattle(uid);
                }
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                // ako dohvatanje ne uspe, fallback — pokreni novu
                loadAndStartNewBattle(uid);
            }
        });
    }

    private void loadAndStartNewBattle(String uid) {
        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                long localUserId = user.getId();
                _potentialRewards.postValue(new PotentialRewards(user.getPreviousBossReward()));

                // 🔹 Sačuvaj aktivnu opremu u LiveData
                if (user.getEquipment() != null) {
                    List<ShopItem> active = new ArrayList<>();
                    for (ShopItem item : user.getEquipment()) {
                        if (item.isActive()) {
                            active.add(item);
                        }
                    }
                    _activeEquipment.postValue(active);
                }

                // 🔹 prvo izračunaj osnovni success rate
                stagePerformanceCalculator.calculateStageSuccessRate(uid, localUserId, new RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double successRate) {
                        // 🔹 zatim primeni aktivne efekte (bonusi iz opreme, napitaka...)
                        applyActiveEffects(uid, user, successRate, new RepositoryCallback<ActiveEffectsResult>() {
                            @Override
                            public void onSuccess(ActiveEffectsResult result) {
                                // koristi pojačani PP i korigovan success rate
                                startBattle(result.adjustedSuccess, result.effectivePp);
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                                startBattle(successRate, user.getPp());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        startBattle(0.0, user.getPp());
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

        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                // 🔹 Postavi potencijalne nagrade
                _potentialRewards.postValue(new PotentialRewards(user.getPreviousBossReward()));

                // 🔹 Sačuvaj aktivnu opremu u LiveData
                if (user.getEquipment() != null) {
                    List<ShopItem> active = new ArrayList<>();
                    for (ShopItem item : user.getEquipment()) {
                        if (item.isActive()) active.add(item);
                    }
                    _activeEquipment.postValue(active);
                }

                // 🔹 Formiraj bossId na osnovu nivoa korisnika
                String bossId = "boss_" + user.getLevel();

                // 🔹 Prvo dohvati samog bossa
                bossRepository.getCurrentBoss(bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss boss) {
                        if (boss != null) {
                            // Ako postoji aktivna borba, koristi HP iz nje
                            BattleStats active = _battleStats.getValue();
                            if (active != null && active.getBossId().equals(bossId)) {
                                boss.setHp(active.getBossHP());
                            } else {
                                boss.setHp(boss.getMaxHp());
                            }

                            _currentBoss.postValue(boss);

                            //proveri da li već postoji rezultat borbe za ovog bossa
                            bossRepository.getBattleResultForBoss(bossId, uid, new RepositoryCallback<BossFightResult>() {
                                @Override
                                public void onSuccess(BossFightResult result) {
                                    if (result != null) {
                                        // Ako postoji rezultat, postavi ga
                                        _battleResult.postValue(result);
                                    }
                                }

                                @Override
                                public void onFailure(Exception e) {
                                    e.printStackTrace();
                                }
                            });

                        } else {
                            // Ako nema bossa, napravi prvog
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
                prefs.getFirebaseUid(),
                boss.getId(),
                System.currentTimeMillis(),
                false,
                5,              // broj napada
                successRate,    // uspešnost
                userPP,         // PP korisnika
                boss.getMaxHp(),
                boss.getMaxHp()
        );

        // 🔹 Sačuvaj borbu u repo
        battleStatsRepository.saveBattle(stats, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) {
                _battleStats.postValue(stats);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                _battleStats.postValue(stats);
            }
        });
    }

    // ------------------ Logika napada ------------------

    public void performAttack() {
        BattleStats stats = _battleStats.getValue();
        Boss boss = _currentBoss.getValue();
        if (stats == null || boss == null || stats.isBattleOver()) return;

        int roll = random.nextInt(100);
        boolean hitSuccess = roll < stats.getSuccessRate();

        if (hitSuccess) {
            int newHP = Math.max(boss.getHp() - stats.getUserPP(), 0);
            boss.setHp(newHP);
            stats.setBossHP(newHP);
            stats.setHitsLanded(stats.getHitsLanded() + 1);
        }

        stats.setRemainingAttempts(stats.getRemainingAttempts() - 1);

        // 🔹 Čuvaj svaku promenu
        battleStatsRepository.updateBattle(stats, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void data) { }
            @Override
            public void onFailure(Exception e) { e.printStackTrace(); }
        });

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
        stats.setBattleOver(true);
        stats.setRewardGranted(true);

        String uid = prefs.getFirebaseUid();

        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                int baseReward = user.getPreviousBossReward() > 0
                        ? (int) Math.round(user.getPreviousBossReward() * 1.2)
                        : 200; // prvi boss = 200

                int earnedCoins = 0;
                boolean equipmentWon = false;
                ShopItem rewardItem = null;

                // ------------------ Odredi nagradu ------------------
                if (victory) {
                    earnedCoins = baseReward;

                    // 20% šansa da dobije opremu
                    if (random.nextInt(100) < 20) {
                        equipmentWon = true;
                    }

                    boss.setDefeated(true);
                } else {
                    // ako nije pobedio, ali je skinuo ≥50% HP-a
                    if (stats.getBossHP() <= boss.getMaxHp() / 2) {
                        earnedCoins = baseReward / 2;

                        // polovi i šansu
                        if (random.nextInt(100) < 10) {
                            equipmentWon = true;
                        }
                    }
                }

                // ------------------ Ako je dobio opremu ------------------
                if (equipmentWon) {
                    // 95% odeća, 5% oružje
                    EquipmentType type = (random.nextInt(100) < 95)
                            ? EquipmentType.CLOTHING
                            : EquipmentType.WEAPON;

                    // Učitaj dostupne iteme
                    List<ShopItem> allItems = ShopData.ITEMS;
                    List<ShopItem> eligible = new ArrayList<>();
                    for (ShopItem item : allItems) {
                        if (item.getType() == type) eligible.add(item);
                    }

                    if (!eligible.isEmpty()) {
                        rewardItem = new ShopItem(
                                eligible.get(random.nextInt(eligible.size())),
                                user.getPreviousBossReward()
                        );
                        rewardItem.setActive(false);

                        List<ShopItem> equipment = user.getEquipment() != null
                                ? new ArrayList<>(user.getEquipment())
                                : new ArrayList<>();
                        equipment.add(rewardItem);
                        user.setEquipment(equipment);
                    }
                }

                // ------------------ Ažuriraj korisnika ------------------
                Map<String, Object> updates = new HashMap<>();
                updates.put("coins", user.getCoins() + earnedCoins);
                updates.put("previousBossReward", baseReward);
                if (victory) {
                    updates.put("bossesDefeated", user.getBossesDefeated() + 1);
                }
                if (user.getEquipment() != null) {
                    // 🔹 Resetuj opremu posle borbe
                    List<ShopItem> updatedEquipment = new ArrayList<>();

                    for (ShopItem item : user.getEquipment()) {
                        if (item.isPermanent()) {
                            updatedEquipment.add(item);

                        } else if (item.getType() == EquipmentType.CLOTHING) {
                            // 👕 odeća traje više borbi
                            int remaining = item.getRemainingBattles();

                            if (remaining > 1) {
                                item.setRemainingBattles(remaining - 1);
                                updatedEquipment.add(item); // još traje, zadrži
                            }
                            // ako remaining == 1 → ne dodajemo, ističe posle ove borbe

                        } else {
                            // ❌ sve ostalo (npr. napici) — jednokratno, briše se odmah
                            // ništa ne dodajemo u updated listu
                        }
                    }

                    user.setEquipment(updatedEquipment);

                    updates.put("equipment", user.getEquipment());
                }

                final int earnedCoinsFinal = earnedCoins;
                final String rewardItemName  = rewardItem != null ? rewardItem.getName() : null;


                userRepository.updateUserFields(uid, updates, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void data) {
                        // 🔹 Sačuvaj rezultat borbe
                        BossFightResult result = new BossFightResult(
                                boss.getId(),
                                uid,
                                victory,
                                earnedCoinsFinal,
                                rewardItemName
                        );

                        bossRepository.saveBattleResult(result, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                _battleResult.postValue(result);

                                // ažuriraj bossa
                                bossRepository.updateBoss(boss, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        // obriši aktivnu sesiju borbe
                                        battleStatsRepository.deleteBattle(uid, new RepositoryCallback<Void>() {
                                            @Override
                                            public void onSuccess(Void data) { }
                                            @Override
                                            public void onFailure(Exception e) { e.printStackTrace(); }
                                        });
                                    }

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

        equipmentRepository.resetAfterBattle(new RepositoryCallback<Void>() {
            @Override public void onSuccess(Void data) { }
            @Override public void onFailure(Exception e) { e.printStackTrace(); }
        });

    }


    // ------------------ Sledeći boss ------------------

    public void createNextBoss(RepositoryCallback<Boss> callback) {
        String uid = prefs.getFirebaseUid();

        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) {
                    callback.onFailure(new Exception("User not found"));
                    return;
                }

                String bossId = "boss_" + user.getLevel();

                bossRepository.getCurrentBoss(bossId, new RepositoryCallback<Boss>() {
                    @Override
                    public void onSuccess(Boss currentBoss) {
                        if (currentBoss != null) {
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
                            Boss firstBoss = new Boss(1, 100, 200);
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


    private void recalculateStatsForActiveBattle(BattleStats activeBattle) {
        String uid = prefs.getFirebaseUid();

        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;

                long localUserId = user.getId();

                // 🔹 ponovo izračunaj osnovni success rate iz performansi korisnika
                stagePerformanceCalculator.calculateStageSuccessRate(uid, localUserId, new RepositoryCallback<Double>() {
                    @Override
                    public void onSuccess(Double successRate) {
                        // 🔹 primeni aktivne efekte (bonusi iz ActiveEffects)
                        applyActiveEffects(uid, user, successRate, new RepositoryCallback<ActiveEffectsResult>() {
                            @Override
                            public void onSuccess(ActiveEffectsResult result) {
                                activeBattle.setUserPP(result.effectivePp);
                                activeBattle.setSuccessRate(result.adjustedSuccess);

                                // 🔹 sačuvaj osvežene vrednosti u repo
                                battleStatsRepository.updateBattle(activeBattle, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        _battleStats.postValue(activeBattle);
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        e.printStackTrace();
                                        _battleStats.postValue(activeBattle);
                                    }
                                });
                            }

                            @Override
                            public void onFailure(Exception e) {
                                e.printStackTrace();
                                // fallback ako ne uspe učitavanje efekata
                                activeBattle.setUserPP(user.getPp());
                                activeBattle.setSuccessRate(successRate);

                                battleStatsRepository.updateBattle(activeBattle, new RepositoryCallback<Void>() {
                                    @Override
                                    public void onSuccess(Void data) {
                                        _battleStats.postValue(activeBattle);
                                    }

                                    @Override
                                    public void onFailure(Exception ex) {
                                        ex.printStackTrace();
                                        _battleStats.postValue(activeBattle);
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                        // fallback ako ne uspe kalkulacija success rate-a
                        activeBattle.setUserPP(user.getPp());
                        battleStatsRepository.updateBattle(activeBattle, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void data) {
                                _battleStats.postValue(activeBattle);
                            }

                            @Override
                            public void onFailure(Exception ex) {
                                ex.printStackTrace();
                                _battleStats.postValue(activeBattle);
                            }
                        });
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }



    private void applyActiveEffects(String uid, User user, double baseSuccessRate, RepositoryCallback<ActiveEffectsResult> callback) {
        equipmentRepository.getActiveEffects(new RepositoryCallback<ActiveEffects>() {
            @Override
            public void onSuccess(ActiveEffects effects) {
                int effectivePp = effects.calculateEffectivePp(user.getPp());
                double adjustedSuccess = baseSuccessRate;

                // 🔹 ako ima bonus iz odeće, povećaj successRate (npr. +10%)
                if (effects.getEquipmentBonus() > 0) {
                    adjustedSuccess += adjustedSuccess * effects.getEquipmentBonus();
                }

                // 🔹 garantuj da ne prelazi 100%
                if (adjustedSuccess > 100) adjustedSuccess = 100;

                callback.onSuccess(new ActiveEffectsResult(effectivePp, adjustedSuccess, effects));
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
                callback.onFailure(e);
            }
        });
    }

    private static class ActiveEffectsResult {
        int effectivePp;
        double adjustedSuccess;
        ActiveEffects effects;

        ActiveEffectsResult(int effectivePp, double adjustedSuccess, ActiveEffects effects) {
            this.effectivePp = effectivePp;
            this.adjustedSuccess = adjustedSuccess;
            this.effects = effects;
        }
    }


}


