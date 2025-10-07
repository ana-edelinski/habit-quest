package com.example.habitquest.data.remote;


import com.example.habitquest.domain.model.BattleStats;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class BattleStatsRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION = "battle_stats";

    public void getActiveBattle(String firebaseUid, RepositoryCallback<BattleStats> callback) {
        db.collection(COLLECTION)
                .document(firebaseUid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        BattleStats stats = document.toObject(BattleStats.class);
                        callback.onSuccess(stats);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void saveBattle(BattleStats stats, RepositoryCallback<Void> callback) {
        if (stats.getFirebaseUid() == null) {
            callback.onFailure(new Exception("User ID is null"));
            return;
        }

        db.collection(COLLECTION)
                .document(stats.getFirebaseUid())
                .set(stats)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void updateBattle(BattleStats stats, RepositoryCallback<Void> callback) {
        if (stats.getFirebaseUid() == null) {
            callback.onFailure(new Exception("User ID is null"));
            return;
        }

        DocumentReference ref = db.collection(COLLECTION).document(stats.getFirebaseUid());
        ref.set(stats)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void deleteBattle(String firebaseUid, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION)
                .document(firebaseUid)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }
}

