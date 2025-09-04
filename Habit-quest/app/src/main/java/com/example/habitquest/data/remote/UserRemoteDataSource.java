package com.example.habitquest.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserRemoteDataSource {

    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;

    public UserRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public void registerUser(String email, String password, String username, int avatar,
                                           RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        callback.onFailure(new Exception("Username is already taken"));
                    } else {
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.createUserWithEmailAndPassword(email, password)
                                .addOnSuccessListener(authResult -> {
                                    FirebaseUser firebaseUser = auth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        firebaseUser.sendEmailVerification();

                                        String uid = firebaseUser.getUid();
                                        User user = new User(null, email, username, password, avatar, false);
                                        db.collection(COLLECTION_NAME).document(uid).set(user)
                                                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                                                .addOnFailureListener(callback::onFailure);
                                    }
                                })
                                .addOnFailureListener(callback::onFailure);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void loginUser(String email, String password, RepositoryCallback<Void> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser user = auth.getCurrentUser();
                    if (user != null && user.isEmailVerified()) {
                        callback.onSuccess(null);
                    } else {
                        callback.onFailure(new Exception("Please verify your email before logging in."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

}
