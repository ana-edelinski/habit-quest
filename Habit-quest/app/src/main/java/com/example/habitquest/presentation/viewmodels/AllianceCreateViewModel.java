package com.example.habitquest.presentation.viewmodels;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.utils.NotificationHelper;
import com.example.habitquest.utils.RepositoryCallback;

public class AllianceCreateViewModel extends AndroidViewModel {

    private final AllianceRepository repository;
    private final Context context;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _creationSuccess = new MutableLiveData<>(false);

    public AllianceCreateViewModel(@NonNull Application application, AllianceRepository repository) {
        super(application);
        this.repository = repository;
        this.context = application.getApplicationContext();
    }

    public LiveData<Boolean> isLoading() {
        return _isLoading;
    }

    public LiveData<String> getStatusMessage() {
        return _statusMessage;
    }

    public LiveData<Boolean> creationSuccess() {
        return _creationSuccess;
    }

    public void createAlliance(Alliance alliance) {
        _isLoading.setValue(true);
        _statusMessage.setValue(null);
        _creationSuccess.setValue(false);

        repository.createAlliance(alliance, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _isLoading.postValue(false);
                _creationSuccess.postValue(true);
                _statusMessage.postValue("Alliance created successfully!");

                NotificationHelper.createChannel(context);

                if (alliance.getRequests() != null && !alliance.getRequests().isEmpty()) {
                    for (String requestUid : alliance.getRequests()) {
                        NotificationHelper.showAllianceInvite(
                                context,
                                alliance.getLeaderName(),
                                alliance.getName(),
                                alliance.getId() != null ? alliance.getId() : requestUid
                        );
                    }
                }
            }

            @Override
            public void onFailure(Exception e) {
                _isLoading.postValue(false);
                _creationSuccess.postValue(false);
                _statusMessage.postValue("Failed to create alliance: " + e.getMessage());
            }
        });
    }

    public void notifyLeaderMemberJoined(String leaderName, String memberName, String allianceName) {
        NotificationHelper.createChannel(context);
        NotificationHelper.showAllianceAccepted(context, memberName, allianceName);
        _statusMessage.postValue(memberName + " has joined alliance " + allianceName);
    }
}
