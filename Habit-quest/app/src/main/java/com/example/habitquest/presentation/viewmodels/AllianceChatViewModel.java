package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.remote.AllianceChatRemoteDataSource;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public class AllianceChatViewModel extends ViewModel {

    private final AllianceChatRemoteDataSource dataSource = new AllianceChatRemoteDataSource();
    private final MutableLiveData<List<AllianceMessage>> _messages = new MutableLiveData<>();
    public LiveData<List<AllianceMessage>> messages = _messages;

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
