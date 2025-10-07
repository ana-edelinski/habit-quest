package com.example.habitquest.data.repositories;

import com.example.habitquest.domain.model.ActiveEffects;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EquipmentRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private final DocumentReference userRef = db.collection("users").document(uid);
    private final DocumentReference effectsRef = userRef.collection("effects").document("active");

    public void observeEquipment(RepositoryCallback<List<ShopItem>> callback) {
        userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) {
                callback.onSuccess(new ArrayList<>());
                return;
            }
            User user = snapshot.toObject(User.class);
            callback.onSuccess(user != null && user.getEquipment() != null ? user.getEquipment() : new ArrayList<>());
        });
    }

    public void saveUserAndEffects(User user, ActiveEffects effects, RepositoryCallback<Void> callback) {
        userRef.set(user)
                .addOnSuccessListener(aVoid ->
                        effectsRef.set(effects)
                                .addOnSuccessListener(v -> callback.onSuccess(null))
                                .addOnFailureListener(callback::onFailure)
                )
                .addOnFailureListener(callback::onFailure);
    }

    public void getActiveEffects(RepositoryCallback<ActiveEffects> callback) {
        effectsRef.get()
                .addOnSuccessListener(snapshot -> {
                    ActiveEffects effects = snapshot.toObject(ActiveEffects.class);
                    if (effects == null) effects = new ActiveEffects();
                    callback.onSuccess(effects);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void resetAfterBattle(RepositoryCallback<Void> callback) {
        getActiveEffects(new RepositoryCallback<ActiveEffects>() {
            @Override
            public void onSuccess(ActiveEffects effects) {
                effects.afterBattle();
                effectsRef.set(effects)
                        .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                        .addOnFailureListener(callback::onFailure);
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }



    public DocumentReference getUserRef() { return userRef; }
    public DocumentReference getEffectsRef() { return effectsRef; }
}
