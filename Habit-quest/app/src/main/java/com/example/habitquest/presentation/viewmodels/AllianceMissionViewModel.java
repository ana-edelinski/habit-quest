package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MissionAction;
import com.example.habitquest.domain.repositoryinterfaces.IAllianceMissionRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public class AllianceMissionViewModel extends ViewModel {

    private final IAllianceMissionRepository missionRepo;
    private final AllianceRepository allianceRepo;
    private final String remoteUid;

    private AppPreferences prefs;

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





    public AllianceMissionViewModel(AppPreferences prefs,
                                    IAllianceMissionRepository missionRepo,
                                    AllianceRepository allianceRepo) {
        this.missionRepo = missionRepo;
        this.allianceRepo = allianceRepo;
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
        Alliance alliance = _currentAlliance.getValue();
        AllianceMission mission = _currentMission.getValue();

        if (mission == null || alliance == null) {
            _error.setValue("Mission or alliance not loaded.");
            return;
        }

        missionRepo.finishMission(mission.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                alliance.setMissionActive(false);
                allianceRepo.updateAlliance(alliance, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        _currentMission.postValue(null);
                        _currentAlliance.postValue(alliance);
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
        });
    }

    public void resetMissionJustStarted() {
        _missionJustStarted.setValue(false);
    }

    public String getRemoteUid() {
        return prefs.getFirebaseUid();
    }
}
