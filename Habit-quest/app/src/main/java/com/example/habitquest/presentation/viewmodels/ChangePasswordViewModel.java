package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.utils.RepositoryCallback;

public class ChangePasswordViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final AppPreferences appPreferences;

    private final MutableLiveData<Boolean> _changeSuccess = new MutableLiveData<>();
    public LiveData<Boolean> changeSuccess = _changeSuccess;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    public ChangePasswordViewModel(UserRepository userRepository, AppPreferences appPreferences) {
        this.userRepository = userRepository;
        this.appPreferences = appPreferences;
    }

    public void changePassword(String oldPass, String newPass, String confirmPass) {
        if (oldPass.isEmpty() || newPass.isEmpty() || confirmPass.isEmpty()) {
            _errorMessage.postValue("All fields are required");
            return;
        }
        if (!newPass.equals(confirmPass)) {
            _errorMessage.postValue("New passwords do not match");
            return;
        }
        if (newPass.length() < 6) {
            _errorMessage.postValue("Password must be at least 6 characters");
            return;
        }

        userRepository.changePassword(oldPass, newPass, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _changeSuccess.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }
}

