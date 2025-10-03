package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.ArrayList;
import java.util.List;

public class ExploreViewModel extends ViewModel {

    private final UserRepository userRepository;

    private final MutableLiveData<List<User>> _userSearchResults = new MutableLiveData<>();
    public LiveData<List<User>> userSearchResults = _userSearchResults;

    private final MutableLiveData<User> _currentUser = new MutableLiveData<>();
    public LiveData<User> currentUser = _currentUser;

    public ExploreViewModel(UserRepository repo) {
        this.userRepository = repo;
    }

    public void loadCurrentUser(String uid) {
        userRepository.getUser(uid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                _currentUser.postValue(user);
            }

            @Override
            public void onFailure(Exception e) {
                _currentUser.postValue(null);
            }
        });
    }

    public void searchUsers(String query) {
        userRepository.searchUsersByUsername(query, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> users) {
                _userSearchResults.postValue(users);
            }

            @Override
            public void onFailure(Exception e) {
                _userSearchResults.postValue(new ArrayList<>());
            }
        });
    }

    public void sendFriendRequest(String fromUid, String toUid, RepositoryCallback<Void> callback) {
        userRepository.sendFriendRequest(fromUid, toUid, callback);
    }

    public void cancelFriendRequest(String fromUid, String toUid, RepositoryCallback<Void> callback) {
        userRepository.cancelFriendRequest(fromUid, toUid, callback);
    }


}
