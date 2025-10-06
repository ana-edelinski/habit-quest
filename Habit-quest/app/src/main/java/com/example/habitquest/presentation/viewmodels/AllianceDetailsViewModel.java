package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.AllianceRepository;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class AllianceDetailsViewModel extends ViewModel {

    private final MutableLiveData<Alliance> _alliance = new MutableLiveData<>();
    public LiveData<Alliance> alliance = _alliance;

    private final MutableLiveData<List<User>> _members = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<User>> members = _members;

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final AllianceRepository repo = new AllianceRepository();

    public void loadAlliance(String allianceId) {
        db.collection("alliances").document(allianceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    Alliance a = snapshot.toObject(Alliance.class);
                    _alliance.postValue(a);
                    if (a != null && a.getMembers() != null) loadMembers(a.getMembers());
                });
    }

    private void loadMembers(List<String> memberIds) {
        List<User> users = new ArrayList<>();
        if (memberIds == null || memberIds.isEmpty()) {
            _members.postValue(users);
            return;
        }

        for (String uid : memberIds) {
            db.collection("users").document(uid)
                    .get()
                    .addOnSuccessListener(doc -> {
                        User u = doc.toObject(User.class);
                        if (u != null) users.add(u);
                        _members.postValue(new ArrayList<>(users));
                    });
        }
    }

    public void leaveAlliance(String allianceId, String userId, Consumer<Boolean> callback) {
        repo.leaveAlliance(allianceId, userId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.accept(true);
            }

            @Override
            public void onFailure(Exception e) {
                callback.accept(false);
            }
        });
    }

    public void disbandAlliance(String allianceId, Consumer<Boolean> callback) {
        repo.disbandAlliance(allianceId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                callback.accept(true);
            }

            @Override
            public void onFailure(Exception e) {
                callback.accept(false);
            }
        });
    }
}
