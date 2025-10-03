package com.example.habitquest.presentation.viewmodels;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

public class UserProfileViewModel extends ViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    public UserProfileViewModel(Context context) {
        this.userRepository = new UserRepository(context);
    }

    public void loadUser(String userId) {
        userRepository.getUser(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User userObj) {
                _user.postValue(userObj);
            }

            @Override
            public void onFailure(Exception e) {
                _user.postValue(null);
            }
        });
    }

    public void sendFriendRequest(String currentUid, String targetUid, RepositoryCallback<Void> cb) {
        userRepository.sendFriendRequest(currentUid, targetUid, cb);
    }

    public void cancelFriendRequest(String currentUid, String targetUid, RepositoryCallback<Void> cb) {
        userRepository.cancelFriendRequest(currentUid, targetUid, cb);
    }

    private final MutableLiveData<User> _currentUser = new MutableLiveData<>();
    public LiveData<User> currentUser = _currentUser;

    public void loadCurrentUser(String currentUid) {
        userRepository.getUser(currentUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User userObj) {
                _currentUser.postValue(userObj);
            }

            @Override
            public void onFailure(Exception e) {
                _currentUser.postValue(null);
            }
        });
    }

}
