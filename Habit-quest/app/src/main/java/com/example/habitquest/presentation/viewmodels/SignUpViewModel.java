package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

public class SignUpViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final MutableLiveData<Boolean> _registrationSuccess = new MutableLiveData<>();  //samo VM sme da menja vrednost
    public LiveData<Boolean> registrationSuccess = _registrationSuccess;    //activity/fragment samo observe-uje
    private final MutableLiveData<String> _errorMessage = new MutableLiveData<>();
    public LiveData<String> errorMessage = _errorMessage;
    public SignUpViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    private String validateInput(String email, String username, String password, String confirmPassword, int avatar) {
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            return "Please fill in all fields";
        }
        if (!password.equals(confirmPassword)) {
            return "Passwords do not match";
        }
        if (avatar == -1) {
            return "Please select an avatar";
        }
        return null; //validacija prosla
    }

    public void registerUser(String email, String username, String password, String confirmPassword, int avatar) {
        String validationError = validateInput(email, username, password, confirmPassword, avatar);
        if (validationError != null) {
            _errorMessage.postValue(validationError);
            return;
        }

        userRepository.insertUser(email, username, password, avatar, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                _registrationSuccess.postValue(true);
                _errorMessage.postValue("Verification email sent. Please check your inbox.");
            }

            @Override
            public void onFailure(Exception e) {
                _errorMessage.postValue(e.getMessage());
            }
        });
    }

}
