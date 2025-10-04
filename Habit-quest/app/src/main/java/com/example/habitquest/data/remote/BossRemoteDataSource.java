package com.example.habitquest.data.remote;


import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BossRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getCurrentBoss(String firestoreUid, RepositoryCallback<Boss> callback) {
        db.collection("bosses")
                .document(firestoreUid)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        Boss boss = snapshot.toObject(Boss.class);
                        callback.onSuccess(boss);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void createNextBoss(Boss newBoss, RepositoryCallback<Boss> callback) {
        db.collection("bosses")
                .document(newBoss.getId())
                .set(newBoss)
                .addOnSuccessListener(unused -> callback.onSuccess(newBoss))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void updateBoss(Boss boss, RepositoryCallback<Void> callback) {
        db.collection("bosses")
                .document(boss.getId())
                .set(boss)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }

    public void saveBattleResult(BossFightResult result, RepositoryCallback<Void> callback) {
        db.collection("boss_results")
                .add(result)
                .addOnSuccessListener(ref -> callback.onSuccess(null))
                .addOnFailureListener(e -> callback.onFailure(e));
    }
}

