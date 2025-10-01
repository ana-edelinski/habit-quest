package com.example.habitquest.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();

                    if (firebaseUser == null) {
                        callback.onFailure(new Exception("User creation failed"));
                        return;
                    }

                    String uid = firebaseUser.getUid();

                    db.collection("users")
                            .whereEqualTo("username", username)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    firebaseUser.delete();
                                    callback.onFailure(new Exception("Username is already taken"));
                                } else {
                                    firebaseUser.sendEmailVerification();

                                    User user = new User(null, email, username, avatar,
                                            0, 0, "Beginner", 0, 0);

                                    db.collection("users")
                                            .document(uid)
                                            .set(user)
                                            .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                                            .addOnFailureListener(e -> {
                                                firebaseUser.delete();
                                                callback.onFailure(e);
                                            });
                                }
                            })
                            .addOnFailureListener(e -> {
                                firebaseUser.delete();
                                callback.onFailure(e);
                            });

                })
                .addOnFailureListener(callback::onFailure);
    }


    public void loginUser(String email, String password, RepositoryCallback<User> callback) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = auth.getCurrentUser();
                    if (firebaseUser != null && firebaseUser.isEmailVerified()) {
                        String uid = firebaseUser.getUid();

                        db.collection(COLLECTION_NAME).document(uid)
                                .get()
                                .addOnSuccessListener(document -> {
                                    if (document.exists()) {
                                        User user = document.toObject(User.class);
                                        callback.onSuccess(user);
                                    } else {
                                        callback.onFailure(new Exception("User data not found."));
                                    }
                                })
                                .addOnFailureListener(callback::onFailure);

                    } else {
                        callback.onFailure(new Exception("Please verify your email before logging in."));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void changePassword(String oldPassword, String newPassword, RepositoryCallback<Void> callback) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || user.getEmail() == null) {
            callback.onFailure(new Exception("No authenticated user"));
            return;
        }

        // Re-authenticate
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);
        user.reauthenticate(credential)
                .addOnSuccessListener(aVoid -> {
                    user.updatePassword(newPassword)
                            .addOnSuccessListener(unused -> {
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateUserXpAndLevel(String uid, int newXp, int newLevel, String title, int pp, RepositoryCallback<Void> callback) {
        db.collection("users").document(uid)
                .update(
                        "totalXp", newXp,
                        "level", newLevel,
                        "title", title,
                        "pp", pp
                )
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getUser(String uid, RepositoryCallback<User> callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        User user = document.toObject(User.class);
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


}
