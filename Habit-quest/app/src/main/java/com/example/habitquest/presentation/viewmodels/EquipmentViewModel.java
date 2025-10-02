package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.EquipmentRepository;
import com.example.habitquest.domain.model.ActiveEffects;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class EquipmentViewModel extends ViewModel {

    private final EquipmentRepository repository = new EquipmentRepository();
    private final MutableLiveData<List<ShopItem>> equipment = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ShopItem>> getEquipment() {
        return equipment;
    }

    public EquipmentViewModel() {
        repository.observeEquipment(new RepositoryCallback<List<ShopItem>>() {
            @Override
            public void onSuccess(List<ShopItem> result) {
                equipment.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                equipment.postValue(new ArrayList<>());
            }
        });
    }


    public void activateItem(ShopItem item, RepositoryCallback<Void> callback) {
        DocumentReference userRef = repository.getUserRef();
        DocumentReference effectsRef = repository.getEffectsRef();

        userRef.get().addOnSuccessListener(snapshot -> {
            User user = snapshot.toObject(User.class);
            if (user == null) {
                callback.onFailure(new RuntimeException("User not found"));
                return;
            }

            effectsRef.get().addOnSuccessListener(effectSnap -> {
                final ActiveEffects effects = effectSnap.toObject(ActiveEffects.class) != null
                        ? effectSnap.toObject(ActiveEffects.class)
                        : new ActiveEffects();

                activateInUserEquipment(user, item);

                if (item.isPermanent()) {
                    handlePermanentBoost(user, effects, item, callback);
                } else {
                    handleTemporaryBoost(user, effects, item, callback);
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
                                      RepositoryCallback<Void> callback) {
        int newPp = (int) (user.getPp() * (1 + item.getBonus()));
        user.setPp(newPp);
        effects.addPermanentBonus(item.getBonus());

        repository.saveUserAndEffects(user, effects, callback);
    }

    private void handleTemporaryBoost(User user, ActiveEffects effects, ShopItem item,
                                      RepositoryCallback<Void> callback) {
        if (item.getType() == EquipmentType.POTION) {
            effects.addTempBonus(item.getBonus());
        } else if (item.getType() == EquipmentType.CLOTHING) {
            effects.addEquipmentBonus(item.getBonus());
        }

        repository.saveUserAndEffects(user, effects, callback);
    }
}
