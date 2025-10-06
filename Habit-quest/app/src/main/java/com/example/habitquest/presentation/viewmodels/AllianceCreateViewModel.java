package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.utils.RepositoryCallback;

public class AllianceCreateViewModel extends ViewModel {

    private final AllianceRepository repository;

    private final MutableLiveData<Boolean> _isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> _statusMessage = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> _creationSuccess = new MutableLiveData<>(false);

    public AllianceCreateViewModel(AllianceRepository repository) {
        this.repository = repository;
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
            }

            @Override
            public void onFailure(Exception e) {
                _isLoading.postValue(false);
                _creationSuccess.postValue(false);
                _statusMessage.postValue("Failed to create alliance: " + e.getMessage());
            }
        });
    }
}
