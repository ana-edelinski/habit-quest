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

    // Get all users
    public void getAllUsers() {
        db.collection(COLLECTION_NAME)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            User user = doc.toObject(User.class);
                            users.add(user);
                            Log.d("REMOTE_DB", doc.getId() + " => " + user.toString());
                        }
                    } else {
                        Log.w("REMOTE_DB", "Error getting users", task.getException());
                    }
                });
    }

    // Get user by Firestore ID
    public void getUserById(String id) {
        db.collection(COLLECTION_NAME).document(id)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        User user = documentSnapshot.toObject(User.class);
                        Log.d("REMOTE_DB", "Found user: " + user);
                    } else {
                        Log.d("REMOTE_DB", "No user found with ID " + id);
                    }
                })
                .addOnFailureListener(e ->
                        Log.w("REMOTE_DB", "Error getting user", e));
    }

    // Delete user
    public void deleteUser(String id) {
        db.collection(COLLECTION_NAME).document(id)
                .delete()
                .addOnSuccessListener(aVoid ->
                        Log.d("REMOTE_DB", "User deleted"))
                .addOnFailureListener(e ->
                        Log.w("REMOTE_DB", "Error deleting user", e));
    }
}
