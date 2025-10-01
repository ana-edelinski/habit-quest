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

    public LiveData<List<ShopItem>> getEquipment() {
        return equipment;
    }

    public EquipmentViewModel() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userRef = FirebaseFirestore.getInstance().collection("users").document(uid);

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
}
