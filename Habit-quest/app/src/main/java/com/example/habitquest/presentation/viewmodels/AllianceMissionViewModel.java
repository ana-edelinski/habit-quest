package com.example.habitquest.presentation.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.MissionAction;
import com.example.habitquest.domain.model.ShopData;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.repositoryinterfaces.IAllianceMissionRepository;
import com.example.habitquest.domain.repositoryinterfaces.IUserRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AllianceMissionViewModel extends ViewModel {

    private final IAllianceMissionRepository missionRepo;
    private final AllianceRepository allianceRepo;
    private final IUserRepository userRepository;
    private final String remoteUid;

    private AppPreferences prefs;
    private boolean missionCompletionHandled = false;


    private final MutableLiveData<AllianceMission> _currentMission = new MutableLiveData<>();
    public LiveData<AllianceMission> currentMission = _currentMission;

    private final MutableLiveData<Alliance> _currentAlliance = new MutableLiveData<>();
    public LiveData<Alliance> currentAlliance = _currentAlliance;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    public LiveData<Boolean> isLoading = _isLoading;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public LiveData<String> error = _error;

    private final MutableLiveData<Boolean> _missionJustStarted = new MutableLiveData<>(false);
    public LiveData<Boolean> missionJustStarted = _missionJustStarted;

    private final MutableLiveData<AllianceMission> _lastFinishedMission = new MutableLiveData<>();
    public LiveData<AllianceMission> lastFinishedMission = _lastFinishedMission;







    public AllianceMissionViewModel(AppPreferences prefs,
                                    IAllianceMissionRepository missionRepo,
                                    IUserRepository userRepository,
                                    AllianceRepository allianceRepo) {
        this.missionRepo = missionRepo;
        this.allianceRepo = allianceRepo;
        this.userRepository = userRepository;
        this.remoteUid = prefs.getFirebaseUid();
        this.prefs = prefs;

    }

    // 🔹 Dohvati savez korisnika
    public void loadAllianceForUser() {
        _isLoading.setValue(true);
        allianceRepo.getAllianceByMember(remoteUid, new RepositoryCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance alliance) {
                _isLoading.postValue(false);
                _currentAlliance.postValue(alliance);
                if (alliance != null && alliance.isMissionActive()) {
                    loadActiveMission(alliance.getId());
                }
            }

            @Override
            public void onFailure(Exception e) {
                _isLoading.postValue(false);
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Pokretanje nove misije (samo vođa saveza)
    public void startMission() {
        Alliance alliance = _currentAlliance.getValue();
        if (alliance == null) {
            _error.setValue("Alliance not loaded.");
            return;
        }

        _isLoading.setValue(true);
        missionRepo.createMission(alliance.getId(), alliance.getMembers(), new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                // postavi missionActive = true u savezu
                alliance.setMissionActive(true);
                allianceRepo.updateAlliance(alliance, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        _isLoading.postValue(false);
                        _currentMission.postValue(mission);
                        _currentAlliance.postValue(alliance);
                        _missionJustStarted.postValue(true);

                    }

                    @Override
                    public void onFailure(Exception e) {
                        _isLoading.postValue(false);
                        _error.postValue("Mission created, but failed to update alliance: " + e.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                _isLoading.postValue(false);
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Učitavanje aktivne misije
    public void loadActiveMission(String allianceId) {
        missionRepo.getActiveMission(allianceId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                _currentMission.postValue(mission);
                if (mission != null) listenToMission(mission.getId());
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Real-time listener
    private void listenToMission(String missionId) {
        missionRepo.listenMission(missionId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission updated) {
                _currentMission.postValue(updated);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Ažuriranje napretka člana
    public void updateProgress(String missionId, MissionAction action) {
        missionRepo.updateMemberProgress(missionId, remoteUid, action, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    // 🔹 Završetak misije (i reset saveza)
    public void finishMission() {
        if (missionCompletionHandled) return; // ⚡ spreči ponavljanje
        missionCompletionHandled = true;
        Alliance alliance = _currentAlliance.getValue();
        AllianceMission mission = _currentMission.getValue();

        if (mission == null || alliance == null) {
            _error.setValue("Mission or alliance not loaded.");
            return;
        }

        // 🔹 Odredi ishod: pobeda ako je HP <= 0, poraz ako je isteklo vreme
        boolean victory = mission.getRemainingHP() <= 0;



        // 🔹 Označi misiju kao završenu sa tačnim ishodom
        mission.finish(victory);

        missionRepo.finishMission(
                mission.getId(),
                victory,
                mission.getRemainingHP(),
                new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        alliance.setMissionActive(false);
                        allianceRepo.updateAlliance(alliance, new RepositoryCallback<Void>() {
                            @Override
                            public void onSuccess(Void unused) {
                                if(victory){
                                    distributeMissionRewards();
                                }
                                _currentMission.postValue(null);
                                _currentAlliance.postValue(alliance);
                                missionCompletionHandled = false;

                            }

                            @Override
                            public void onFailure(Exception e) {
                                _error.postValue("Mission finished, but failed to update alliance: " + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        _error.postValue(e.getMessage());
                    }
                }
        );
    }

    public void loadLastFinishedMission(String allianceId) {
        missionRepo.getLastFinishedMission(allianceId, new RepositoryCallback<AllianceMission>() {
            @Override
            public void onSuccess(AllianceMission mission) {
                _lastFinishedMission.postValue(mission);
            }

            @Override
            public void onFailure(Exception e) {
                _error.postValue(e.getMessage());
            }
        });
    }

    public void distributeMissionRewards() {
        Alliance alliance = _currentAlliance.getValue();
        if (alliance == null || alliance.getMembers() == null || alliance.getMembers().isEmpty()) return;

        for (String memberId : alliance.getMembers()) {
            userRepository.getUser(memberId, new RepositoryCallback<User>() {
                @Override
                public void onSuccess(User user) {
                    if (user == null) return;

                    // 🔹 Izračunaj osnovnu nagradu: pola od sledeće boss nagrade
                    int baseReward = user.getPreviousBossReward() > 0
                            ? (int) Math.round(user.getPreviousBossReward() * 1.2)
                            : 200;
                    int earnedCoins = baseReward / 2;

                    // 🔹 Učitaj sve iteme i filtriraj CLOTHING
                    List<ShopItem> allItems = ShopData.ITEMS;
                    List<ShopItem> clothingItems = new ArrayList<>();
                    List<ShopItem> potionItems = new ArrayList<>();
                    for (ShopItem item : allItems) {
                        if (item.getType() == EquipmentType.CLOTHING) clothingItems.add(item);
                        if (item.getType() == EquipmentType.POTION) potionItems.add(item);
                    }

                    // 🔹 Random odeća
                    ShopItem clothingReward = null;
                    ShopItem potionReward = null;
                    if (!clothingItems.isEmpty()) {
                        clothingReward = new ShopItem(
                                clothingItems.get(new Random().nextInt(clothingItems.size())),
                                user.getPreviousBossReward()
                        );
                        clothingReward.setActive(false);
                    }
                    if (!potionItems.isEmpty()) {
                        potionReward = new ShopItem(
                                potionItems.get(new Random().nextInt(clothingItems.size())),
                                user.getPreviousBossReward()
                        );
                        clothingReward.setActive(false);
                    }



                    // 🔹 Dodaj sve nagrade korisniku
                    List<ShopItem> updatedEquipment = user.getEquipment() != null
                            ? new ArrayList<>(user.getEquipment())
                            : new ArrayList<>();

                    if (clothingReward != null) updatedEquipment.add(clothingReward);
                    if (potionReward != null) updatedEquipment.add(potionReward);
                    user.setEquipment(updatedEquipment);

                    // 🔹 Ažuriraj polja korisnika
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("coins", user.getCoins() + earnedCoins);
                    updates.put("equipment", updatedEquipment);

                    // 🔹 Sačuvaj izmene
                    userRepository.updateUserFields(memberId, updates, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void data) {
                            Log.d("MissionReward", "Alliance reward → " + user.getUsername()
                                    + ": +" + earnedCoins + " coins, +1 potion, +1 clothing");
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Log.e("MissionReward", "Failed to update user " + memberId, e);
                        }
                    });
                }

                @Override
                public void onFailure(Exception e) {
                    Log.e("MissionReward", "Failed to load user " + memberId, e);
                }
            });
        }
    }






    public void resetMissionJustStarted() {
        _missionJustStarted.setValue(false);
    }


    public void getUsername(String userId, RepositoryCallback<String> callback) {
        userRepository.getUser(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null && user.getUsername() != null) {
                    callback.onSuccess(user.getUsername());
                } else {
                    callback.onSuccess("Unknown");
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }


    public String getRemoteUid() {
        return prefs.getFirebaseUid();
    }
}
