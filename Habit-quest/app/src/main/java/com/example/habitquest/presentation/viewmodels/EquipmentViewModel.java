package com.example.habitquest.presentation.viewmodels;

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
        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user == null) {
                callback.onFailure(new RuntimeException("User not found"));
                return;
            }

            List<ShopItem> current = user.getEquipment();
            if (current == null) current = new ArrayList<>();

            // nadji item
            for (int i = 0; i < current.size(); i++) {
                if (current.get(i).getName().equals(item.getName())) {
                    current.get(i).setActive(true);

                    if (item.isPermanent()) {
                        // trajni -> trajno povecaj PP
                        int newPp = (int) (user.getPp() * (1 + item.getBonus()));
                        user.setPp(newPp);
                    } else {
                        // jednokratni -> samo do sledece borbe
                        user.setTempBonus(item.getBonus());
                    }
                    break;
                }
            }

            user.setEquipment(current);
            equipment.setValue(new ArrayList<>(current));

            // snimi sve promene
            userRef.set(user)
                    .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);
        });
    }

}
