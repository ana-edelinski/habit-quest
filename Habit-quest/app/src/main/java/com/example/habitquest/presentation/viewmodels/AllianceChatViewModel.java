package com.example.habitquest.presentation.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.remote.AllianceChatRemoteDataSource;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public class AllianceChatViewModel extends AndroidViewModel {

    private final AllianceChatRemoteDataSource dataSource = new AllianceChatRemoteDataSource();
    private final UserRepository userRepository;

    private final MutableLiveData<List<AllianceMessage>> _messages = new MutableLiveData<>();
    public LiveData<List<AllianceMessage>> messages = _messages;

    private final MutableLiveData<String> _currentUsername = new MutableLiveData<>();
    public LiveData<String> currentUsername = _currentUsername;

    public AllianceChatViewModel(@NonNull Application application) {
        super(application);
        userRepository = new UserRepository(application);
    }

    public void loadUsername(String userId) {
        AppPreferences prefs = new AppPreferences(getApplication());
        String cachedUsername = prefs.getUsername();
        Log.d("PrefsDebug", "Loaded cached username: " + cachedUsername);

        if (cachedUsername != null && !cachedUsername.isEmpty()) {
            _currentUsername.postValue(cachedUsername);
        } else {
            _currentUsername.postValue("Player");
        }

        userRepository.getUser(userId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                Log.d("PrefsDebug", "Firestore username: " + (user != null ? user.getUsername() : "null"));
                if (user != null && user.getUsername() != null) {
                    _currentUsername.postValue(user.getUsername());
                    prefs.saveUsername(user.getUsername());
                }
            }

            @Override
            public void onFailure(Exception e) {
                Log.e("PrefsDebug", "Failed to load Firestore user", e);
            }
        });
    }


    public void startListening(String allianceId) {
        dataSource.listenForMessages(allianceId, new RepositoryCallback<List<AllianceMessage>>() {
            @Override
            public void onSuccess(List<AllianceMessage> result) {
                _messages.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stopListening() {
        dataSource.removeListener();
    }

    public void sendMessage(String allianceId, AllianceMessage msg) {
        dataSource.sendMessage(allianceId, msg, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {}
            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }
}
