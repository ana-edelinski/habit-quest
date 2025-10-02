package com.example.habitquest.presentation.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
        Log.d("EQUIP", "activateItem START for " + item.getName());

        userRef.get().addOnSuccessListener(snapshot -> {
            Log.d("EQUIP", "Got snapshot: " + snapshot.exists());

            User user = snapshot.toObject(User.class);
            if (user == null) {
                Log.d("EQUIP", "User is NULL!");
                callback.onFailure(new RuntimeException("User not found"));
                return;
            }

            Log.d("EQUIP", "User PP before calc = " + user.getPp());

            List<ShopItem> current = user.getEquipment();
            if (current == null) current = new ArrayList<>();

            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getName().equals(item.getName())) {
                    current.get(i).setActive(true);

                    if (item.isPermanent()) {
                        int newPp = (int) (user.getPp() * (1 + item.getBonus()));
                        Log.d("EQUIP", "Permanent item bonus=" + item.getBonus() +
                                ", newPP=" + newPp);
                        user.setPp(newPp);
                    } else {
                        Log.d("EQUIP", "Temporary item bonus=" + item.getBonus());
                        user.setTempBonus(item.getBonus());
                    }
                    break;
                }
            }

            user.setEquipment(current);

            // log pre upisa
            Log.d("EQUIP", "Saving user with PP=" + user.getPp());

            userRef.set(user)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("EQUIP", "User saved successfully with PP=" + user.getPp());
                        callback.onSuccess(null);
                    })
                    .addOnFailureListener(e -> {
                        Log.e("EQUIP", "Error saving user", e);
                        callback.onFailure(e);
                    });
        });
    }

}
