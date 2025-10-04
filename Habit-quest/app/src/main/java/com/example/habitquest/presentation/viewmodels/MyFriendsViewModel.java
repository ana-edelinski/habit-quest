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

public class MyFriendsViewModel extends ViewModel {

    private final UserRepository userRepository;
    private final String remoteUid;

    private final MutableLiveData<List<User>> _friends = new MutableLiveData<>();
    public LiveData<List<User>> friends = _friends;
    private final MutableLiveData<List<User>> _friendRequestsUsers = new MutableLiveData<>();
    public LiveData<List<User>> friendRequestsUsers = _friendRequestsUsers;
    private final MutableLiveData<Integer> _friendRequestsCount = new MutableLiveData<>(0);
    public LiveData<Integer> friendRequestsCount = _friendRequestsCount;

    public MyFriendsViewModel(AppPreferences prefs, UserRepository repo) {
        this.userRepository = repo;
        this.remoteUid = prefs.getFirebaseUid();
    }

    public void loadFriends() {
        userRepository.getFriends(remoteUid, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> friendList) {
                _friends.postValue(friendList);
            }

            @Override
            public void onFailure(Exception e) {
                _friends.postValue(new ArrayList<>());
            }
        });
    }

    public void listenForFriendsRealtime() {
        userRepository.listenForFriends(remoteUid, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> friendList) {
                _friends.postValue(friendList);
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void listenForFriendRequestsRealtime() {
        userRepository.listenForFriendRequests(remoteUid, new RepositoryCallback<List<User>>() {
            @Override
            public void onSuccess(List<User> list) {
                _friendRequestsUsers.postValue(list);
                _friendRequestsCount.postValue(list != null ? list.size() : 0);
            }

            @Override
            public void onFailure(Exception e) {
                _friendRequestsCount.postValue(0);
            }
        });
    }

    public void acceptFriendRequest(String requesterUid) {
        List<User> currentRequests = _friendRequestsUsers.getValue();
        if (currentRequests != null) {
            List<User> updated = new ArrayList<>(currentRequests);
            updated.removeIf(u -> u.getUid().equals(requesterUid));
            _friendRequestsUsers.setValue(updated);
            _friendRequestsCount.setValue(updated.size());
        }

        userRepository.acceptFriendRequest(remoteUid, requesterUid, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                loadFriends();
            }

            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void rejectFriendRequest(String requesterUid) {
        List<User> currentRequests = _friendRequestsUsers.getValue();
        if (currentRequests != null) {
            List<User> updated = new ArrayList<>(currentRequests);
            updated.removeIf(u -> u.getUid().equals(requesterUid));
            _friendRequestsUsers.setValue(updated);
            _friendRequestsCount.setValue(updated.size());
        }

        userRepository.rejectFriendRequest(remoteUid, requesterUid, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

}
