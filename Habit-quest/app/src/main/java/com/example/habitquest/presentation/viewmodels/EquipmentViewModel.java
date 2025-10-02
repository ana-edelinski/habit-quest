package com.example.habitquest.presentation.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.domain.model.ActiveEffects;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class EquipmentViewModel extends ViewModel {

    private final MutableLiveData<List<ShopItem>> equipment = new MutableLiveData<>(new ArrayList<>());
    private final String uid;
    private final DocumentReference userRef;

    public LiveData<List<ShopItem>> getEquipment() {
        return equipment;
    }

    public EquipmentViewModel() {
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

        // sluša promene u Firestore-u
        userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null || snapshot == null || !snapshot.exists()) {
                equipment.setValue(new ArrayList<>());
                return;
            }
            User user = snapshot.toObject(User.class);
            if (user != null && user.getEquipment() != null) {
                equipment.setValue(user.getEquipment());
            }
        });
    }

    public void activateItem(ShopItem item, RepositoryCallback<Void> callback) {
        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user == null) {
                callback.onFailure(new RuntimeException("User not found"));
                return;
            }

            DocumentReference effectsRef = userRef.collection("effects").document("active");
            effectsRef.get().addOnSuccessListener(effectSnap -> {
                final ActiveEffects effects = effectSnap.toObject(ActiveEffects.class) != null
                        ? effectSnap.toObject(ActiveEffects.class)
                        : new ActiveEffects();

                activateInUserEquipment(user, item);

                if (item.isPermanent()) {
                    handlePermanentBoost(user, effects, item, callback, effectsRef);
                } else {
                    handleTemporaryBoost(user, effects, item, callback, effectsRef);
                }
            });
        });
    }

    private void activateInUserEquipment(User user, ShopItem item) {
        List<ShopItem> current = user.getEquipment();
        if (current == null) current = new ArrayList<>();

        for (int i = 0; i < current.size(); i++) {
            if (current.get(i).getName().equals(item.getName())) {
                current.get(i).setActive(true);
                break;
            }
        }
        user.setEquipment(current);
        equipment.setValue(new ArrayList<>(current));
    }

    private void handlePermanentBoost(User user, ActiveEffects effects, ShopItem item,
                                      RepositoryCallback<Void> callback, DocumentReference effectsRef) {
        int newPp = (int) (user.getPp() * (1 + item.getBonus()));
        user.setPp(newPp);
        effects.addPermanentBonus(item.getBonus());

        saveUserAndEffects(user, effects, callback, effectsRef);
    }

    private void handleTemporaryBoost(User user, ActiveEffects effects, ShopItem item,
                                      RepositoryCallback<Void> callback, DocumentReference effectsRef) {
        if (item.getType() == EquipmentType.POTION) {
            effects.addTempBonus(item.getBonus());
        } else if (item.getType() == EquipmentType.CLOTHING) {
            effects.addEquipmentBonus(item.getBonus());
        }

        saveUserAndEffects(user, effects, callback, effectsRef);
    }

    private void saveUserAndEffects(User user, ActiveEffects effects,
                                    RepositoryCallback<Void> callback, DocumentReference effectsRef) {
        userRef.set(user)
                .addOnSuccessListener(aVoid ->
                        effectsRef.set(effects)
                                .addOnSuccessListener(v -> callback.onSuccess(null))
                                .addOnFailureListener(callback::onFailure)
                )
                .addOnFailureListener(callback::onFailure);
    }
    
}
