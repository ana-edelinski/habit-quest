package com.example.habitquest.data.remote;


import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserXpLogRemoteDataSource {
    private static final String COLLECTION_NAME = "userXpLogs";
    private final FirebaseFirestore db;

    public UserXpLogRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public void insert(UserXpLog log, RepositoryCallback<UserXpLog> cb) {
        db.collection(COLLECTION_NAME)
                .add(log)
                .addOnSuccessListener(ref -> cb.onSuccess(log))
                .addOnFailureListener(cb::onFailure);
    }

    public void fetchAll(long userId, RepositoryCallback<List<UserXpLog>> cb) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    List<UserXpLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        UserXpLog log = doc.toObject(UserXpLog.class);
                        logs.add(log);
                    }
                    cb.onSuccess(logs);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void deleteAllForUser(long userId, RepositoryCallback<Void> cb) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                    cb.onSuccess(null);
                })
                .addOnFailureListener(cb::onFailure);
    }
}

