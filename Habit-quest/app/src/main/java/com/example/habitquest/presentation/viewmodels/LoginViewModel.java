package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

public class LoginViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final AppPreferences appPreferences;

    private final MutableLiveData<Boolean> _loginSuccess = new MutableLiveData<>();
    public LiveData<Boolean> loginSuccess = _loginSuccess;

    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;

    private final MutableLiveData<Boolean> _logoutSuccess = new MutableLiveData<>();
    public LiveData<Boolean> logoutSuccess = _logoutSuccess;

    public LoginViewModel(UserRepository userRepository, AppPreferences appPreferences) {
        this.userRepository = userRepository;
        this.appPreferences = appPreferences;
    }

    public void login(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            _errorMessage.postValue("Please fill in all fields");
            return;
        }

        userRepository.loginUser(email, password, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User result) {
                appPreferences.saveUserSession(result.getEmail());
                appPreferences.saveUsername(result.getUsername());
                appPreferences.saveAvatarIndex(result.getAvatar());
                _loginSuccess.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });

    }

    public void logout() {
        com.google.firebase.auth.FirebaseAuth.getInstance().signOut();
        appPreferences.clearSession();
        _logoutSuccess.postValue(true); 
    }

    public boolean isLoggedIn() {
        return appPreferences.isLoggedIn();
    }
}

