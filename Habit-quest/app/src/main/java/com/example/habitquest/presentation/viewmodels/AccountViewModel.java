package com.example.habitquest.presentation.viewmodels;

import android.app.Application;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.utils.RepositoryCallback;

public class AccountViewModel extends ViewModel {

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> user = _user;

    private final UserXpLogRepository xpLogRepository;
    private final MutableLiveData<Integer> _totalXp = new MutableLiveData<>();
    public LiveData<Integer> totalXp = _totalXp;

    public AccountViewModel(AppPreferences prefs, UserXpLogRepository repo) {
        String username = prefs.getUsername();
        int avatar = prefs.getAvatarIndex();

        User u = new User();
        u.setUsername(username);
        u.setAvatar(avatar);

        _user.setValue(u);

        xpLogRepository = repo;
    }

    public void updateUser(User newUser) {
        _user.setValue(newUser);
    }

    public void loadTotalXp(long localUserId) {
        xpLogRepository.getTotalXp(localUserId, new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer totalXp) {
                _totalXp.postValue(totalXp);
            }

            @Override
            public void onFailure(Exception e) {
                _totalXp.postValue(0);
            }
        });
    }
}
