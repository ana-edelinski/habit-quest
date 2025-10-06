package com.example.habitquest.data.remote;

import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.FirebaseFirestore;

public class AllianceRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void createAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        db.collection("alliances")
                .add(alliance)
                .addOnSuccessListener(docRef -> {
                    alliance.setId(docRef.getId());
                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }
}
