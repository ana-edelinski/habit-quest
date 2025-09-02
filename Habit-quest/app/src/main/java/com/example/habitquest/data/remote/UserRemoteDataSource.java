package com.example.habitquest.data.remote;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    // Insert user
    public void insertUser(User user) {
        db.collection(COLLECTION_NAME)
                .add(user)
                .addOnSuccessListener(documentReference ->
                        Log.d("REMOTE_DB", "User added with ID: " + documentReference.getId()))
                .addOnFailureListener(e ->
                        Log.w("REMOTE_DB", "Error adding user", e));
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
