package com.example.habitquest.data.repositories;

import com.example.habitquest.domain.model.ShopData;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class CartRepository {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

    public void addItem(ShopItem item, RepositoryCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.update("cart", FieldValue.arrayUnion(item))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void removeItem(ShopItem item, RepositoryCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(uid);
        userRef.update("cart", FieldValue.arrayRemove(item))
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public ListenerRegistration observeCart(RepositoryCallback<List<ShopItem>> callback) {
        DocumentReference userRef = db.collection("users").document(uid);
        return userRef.addSnapshotListener((snapshot, e) -> {
            if (e != null) {
                callback.onFailure(e);
                return;
            }
            if (snapshot != null && snapshot.exists()) {
                User user = snapshot.toObject(User.class);
                List<ShopItem> items = (user != null && user.getCart() != null)
                        ? user.getCart()
                        : new ArrayList<>();
                callback.onSuccess(items);
            }
        });
    }

    private int calculateBossReward(int level) {
        if (level <= 0) {
            return 200;
        }
        int reward = 200;
        for (int i = 1; i < level; i++) {
            reward = (int) Math.round(reward * 1.2);
        }
        return reward;
    }


    public void buyItems(RepositoryCallback<Void> callback) {
        DocumentReference userRef = db.collection("users").document(uid);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(userRef);

                    User user = snapshot.toObject(User.class);
                    if (user == null) throw new RuntimeException("User not found");

                    List<ShopItem> cart = user.getCart();

                    int total = 0;
                    for (ShopItem item : cart) {
                        total += item.getPrice();
                    }

                    if (user.getCoins() < total) {
                        throw new RuntimeException("Not enough coins");
                    }

                    user.setCoins(user.getCoins() - total);

                    List<ShopItem> equipment = user.getEquipment() != null ? user.getEquipment() : new ArrayList<>();
                    for (ShopItem si : cart) {
                        si.setActive(false);
                        equipment.add(si);
                    }
                    user.setEquipment(equipment);

                    user.setCart(new ArrayList<>());

                    transaction.set(userRef, user);

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }


    public List<ShopItem> getShopItemsForUser(User user) {
        int previousBossReward = calculateBossReward(user.getLevel() - 1);
        List<ShopItem> result = new ArrayList<>();
        for (ShopItem base : ShopData.ITEMS) {
            result.add(new ShopItem(base, previousBossReward));
        }
        return result;
    }

}