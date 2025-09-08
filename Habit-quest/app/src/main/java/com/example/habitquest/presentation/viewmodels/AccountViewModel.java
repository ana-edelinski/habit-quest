package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.domain.model.User;
import com.example.habitquest.data.prefs.AppPreferences;

public class AccountViewModel extends ViewModel {

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    public AccountViewModel(AppPreferences prefs) {
        String username = prefs.getUsername();
        int avatar = prefs.getAvatarIndex();

        User u = new User();
        u.setUsername(username);
        u.setAvatar(avatar);

        _user.setValue(u);
    }

    public void updateUser(User newUser) {
        _user.setValue(newUser);
    }
}
