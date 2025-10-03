package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class AccountViewModel extends ViewModel {

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    private final MutableLiveData<Integer> _totalXp = new MutableLiveData<>();
    public LiveData<Integer> totalXp = _totalXp;

    private final UserRepository userRepository;
    private final String remoteUid;

    private final MutableLiveData<List<String>> _friends = new MutableLiveData<>();
    public LiveData<List<String>> friends = _friends;

    public AccountViewModel(AppPreferences prefs, UserRepository repo) {
        this.userRepository = repo;
        this.remoteUid = prefs.getFirebaseUid();

        // postavi basic podatke iz prefs dok ne dođu podaci sa servera
        User u = new User();
        u.setUsername(prefs.getUsername());
        u.setAvatar(prefs.getAvatarIndex());
        _user.setValue(u);
    }

    public void updateUser(User newUser) {
        _user.setValue(newUser);
    }

    public void loadUser() {
        userRepository.getUser(remoteUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User remoteUser) {
                _user.postValue(remoteUser);
                _totalXp.postValue(remoteUser.getTotalXp());
            }

            @Override
            public void onFailure(Exception e) {
                _totalXp.postValue(0);
            }
        });
    }

    public void loadFriends() {
        userRepository.getFriends(remoteUid, new RepositoryCallback<List<String>>() {
            @Override
            public void onSuccess(List<String> friendList) {
                _friends.postValue(friendList);
            }

            @Override
            public void onFailure(Exception e) {
                _friends.postValue(new ArrayList<>());
            }
        });
    }

}
