package com.example.habitquest.data.remote;


import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.Boss;
import com.example.habitquest.domain.model.BossFightResult;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class BossRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void getCurrentBoss(String bossId, RepositoryCallback<Boss> callback) {
        db.collection("bosses")
                .document(bossId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    if (snapshot.exists()) {
                        callback.onSuccess(snapshot.toObject(Boss.class));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);

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

    public void saveBoss(Boss boss, RepositoryCallback<Void> callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("bosses")
                .document(boss.getId())
                .set(boss)
                .addOnSuccessListener(unused -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getBattleResultForBoss(String bossId, String userId, RepositoryCallback<BossFightResult> callback) {
        db.collection("boss_results")
                .whereEqualTo("bossId", bossId)
                .whereEqualTo("firestoreUid", userId)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        BossFightResult result = query.getDocuments().get(0).toObject(BossFightResult.class);
                        callback.onSuccess(result);
                    } else {
                        callback.onSuccess(null); // nema rezultata
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }
}

