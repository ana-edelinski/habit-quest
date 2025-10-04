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
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserRemoteDataSource {

    private static final String COLLECTION_NAME = "users";
    private final FirebaseFirestore db;
    private ListenerRegistration friendsListener;
    private ListenerRegistration friendRequestsListener;

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

                    // Provera da li username postoji
                    db.collection(COLLECTION_NAME)
                            .whereEqualTo("usernameLowercase", username.toLowerCase())
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                if (!querySnapshot.isEmpty()) {
                                    firebaseUser.delete();
                                    callback.onFailure(new Exception("Username is already taken"));
                                } else {
                                    firebaseUser.sendEmailVerification();

                                    // 🔹 upisujemo i username i usernameLowercase
                                    Map<String, Object> userMap = new HashMap<>();
                                    userMap.put("email", email);
                                    userMap.put("username", username);
                                    userMap.put("usernameLowercase", username.toLowerCase());
                                    userMap.put("avatar", avatar);
                                    userMap.put("totalXp", 0);
                                    userMap.put("level", 0);
                                    userMap.put("title", "Beginner");
                                    userMap.put("pp", 0);
                                    userMap.put("coins", 0);
                                    userMap.put("bossesDefeated", 0);
                                    userMap.put("friends", new ArrayList<>()); // prazna lista prijatelja

                                    db.collection(COLLECTION_NAME)
                                            .document(uid)
                                            .set(userMap)
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
                        if (user != null) {
                            user.setUid(document.getId());
                        }
                        callback.onSuccess(user);
                    } else {
                        callback.onFailure(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getFriends(String uid, RepositoryCallback<List<User>> callback) {
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> friendUids = (List<String>) documentSnapshot.get("friends");
                    if (friendUids == null || friendUids.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    db.collection(COLLECTION_NAME)
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), friendUids)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                List<User> friends = new ArrayList<>();
                                for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                                    User friend = doc.toObject(User.class);
                                    if (friend != null) {
                                        friend.setUid(doc.getId());
                                        friends.add(friend);
                                    }
                                }
                                callback.onSuccess(friends);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void getFriendRequestsReceived(String uid, RepositoryCallback<List<User>> callback) {
        db.collection(COLLECTION_NAME).document(uid).get()
                .addOnSuccessListener(snapshot -> {
                    if (!snapshot.exists()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    List<String> requestUids = (List<String>) snapshot.get("friendRequestsReceived");
                    if (requestUids == null || requestUids.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    db.collection(COLLECTION_NAME)
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), requestUids)
                            .get()
                            .addOnSuccessListener(query -> {
                                List<User> list = new ArrayList<>();
                                for (DocumentSnapshot doc : query.getDocuments()) {
                                    User u = doc.toObject(User.class);
                                    if (u != null) {
                                        u.setUid(doc.getId());
                                        list.add(u);
                                    }
                                }
                                callback.onSuccess(list);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    //vrv ne treba
    public void getFriendRequestsSent(String uid, RepositoryCallback<List<String>> callback) {
        db.collection(COLLECTION_NAME).document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> requests = (List<String>) documentSnapshot.get("friendRequestsSent");
                        if (requests == null) requests = new ArrayList<>();
                        callback.onSuccess(requests);
                    } else {
                        callback.onSuccess(new ArrayList<>());
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }


    public void searchUsersByUsername(String query, RepositoryCallback<List<User>> callback) {
        if (query == null || query.trim().isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }

        String searchTerm = query.trim().toLowerCase();

        db.collection(COLLECTION_NAME)
                .whereGreaterThanOrEqualTo("usernameLowercase", searchTerm)
                .whereLessThanOrEqualTo("usernameLowercase", searchTerm + "\uf8ff")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<User> result = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                        }
                        result.add(user);
                    }

                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void sendFriendRequest(String fromUid, String toUid, RepositoryCallback<Void> callback) {
        DocumentReference fromRef = db.collection(COLLECTION_NAME).document(fromUid);
        DocumentReference toRef = db.collection(COLLECTION_NAME).document(toUid);

        db.runTransaction(transaction -> {
                    transaction.update(fromRef, "friendRequestsSent", FieldValue.arrayUnion(toUid));
                    transaction.update(toRef, "friendRequestsReceived", FieldValue.arrayUnion(fromUid));
                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void cancelFriendRequest(String fromUid, String toUid, RepositoryCallback<Void> callback) {
        DocumentReference fromRef = db.collection(COLLECTION_NAME).document(fromUid);
        DocumentReference toRef = db.collection(COLLECTION_NAME).document(toUid);

        db.runTransaction(transaction -> {
                    transaction.update(fromRef, "friendRequestsSent", FieldValue.arrayRemove(toUid));
                    transaction.update(toRef, "friendRequestsReceived", FieldValue.arrayRemove(fromUid));
                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void acceptFriendRequest(String currentUid, String requesterUid, RepositoryCallback<Void> callback) {
        DocumentReference currentUserRef = db.collection(COLLECTION_NAME).document(currentUid);
        DocumentReference requesterRef = db.collection(COLLECTION_NAME).document(requesterUid);

        db.runTransaction(transaction -> {
                    transaction.update(currentUserRef, "friends", FieldValue.arrayUnion(requesterUid));
                    transaction.update(requesterRef, "friends", FieldValue.arrayUnion(currentUid));

                    transaction.update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(requesterUid));
                    transaction.update(requesterRef, "friendRequestsSent", FieldValue.arrayRemove(currentUid));

                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void rejectFriendRequest(String currentUid, String requesterUid, RepositoryCallback<Void> callback) {
        DocumentReference currentUserRef = db.collection(COLLECTION_NAME).document(currentUid);
        DocumentReference requesterRef = db.collection(COLLECTION_NAME).document(requesterUid);

        db.runTransaction(transaction -> {
                    transaction.update(currentUserRef, "friendRequestsReceived", FieldValue.arrayRemove(requesterUid));
                    transaction.update(requesterRef, "friendRequestsSent", FieldValue.arrayRemove(currentUid));
                    return null;
                }).addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void listenForFriends(String uid, RepositoryCallback<List<User>> callback) {
        if (friendsListener != null) friendsListener.remove();

        friendsListener = db.collection(COLLECTION_NAME)
                .document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) {
                        callback.onFailure(e);
                        return;
                    }

                    List<String> friendUids = (List<String>) snapshot.get("friends");
                    if (friendUids == null || friendUids.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    db.collection(COLLECTION_NAME)
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), friendUids)
                            .get()
                            .addOnSuccessListener(query -> {
                                List<User> list = new ArrayList<>();
                                for (DocumentSnapshot doc : query.getDocuments()) {
                                    User u = doc.toObject(User.class);
                                    if (u != null) {
                                        u.setUid(doc.getId());
                                        list.add(u);
                                    }
                                }
                                callback.onSuccess(list);
                            })
                            .addOnFailureListener(callback::onFailure);
                });
    }


    public void listenForFriendRequests(String uid, RepositoryCallback<List<User>> callback) {
        if (friendRequestsListener != null) friendRequestsListener.remove();

        friendRequestsListener = db.collection(COLLECTION_NAME)
                .document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    List<String> requestUids = (List<String>) snapshot.get("friendRequestsReceived");
                    if (requestUids == null || requestUids.isEmpty()) {
                        callback.onSuccess(new ArrayList<>());
                        return;
                    }

                    db.collection(COLLECTION_NAME)
                            .whereIn(com.google.firebase.firestore.FieldPath.documentId(), requestUids)
                            .get()
                            .addOnSuccessListener(query -> {
                                List<User> list = new ArrayList<>();
                                for (DocumentSnapshot doc : query.getDocuments()) {
                                    User u = doc.toObject(User.class);
                                    if (u != null) {
                                        u.setUid(doc.getId());
                                        list.add(u);
                                    }
                                }
                                callback.onSuccess(list);
                            });
                });
    }



}
