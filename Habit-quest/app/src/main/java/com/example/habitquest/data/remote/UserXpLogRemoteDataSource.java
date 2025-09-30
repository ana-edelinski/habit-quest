package com.example.habitquest.data.remote;

import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserXpLogRemoteDataSource {
    private static final String COLLECTION_NAME = "userXpLogs";
    private final FirebaseFirestore db;

    public UserXpLogRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    /** Kreiranje novog loga sa Firestore documentId */
    public void insert(UserXpLog log, RepositoryCallback<UserXpLog> cb) {
        DocumentReference docRef = db.collection(COLLECTION_NAME).document();
        String newId = docRef.getId();
        log.setId(newId);

        docRef.set(log)
                .addOnSuccessListener(v -> cb.onSuccess(log))
                .addOnFailureListener(cb::onFailure);
    }

    /** Čitanje svih logova korisnika */
    public void fetchAll(String firebaseUid, RepositoryCallback<List<UserXpLog>> cb) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(query -> {
                    List<UserXpLog> logs = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        UserXpLog log = doc.toObject(UserXpLog.class);
                        log.setId(doc.getId()); // setuj Firestore documentId
                        logs.add(log);
                    }
                    cb.onSuccess(logs);
                })
                .addOnFailureListener(cb::onFailure);
    }

    /** Brisanje svih logova korisnika */
    public void deleteAllForUser(String firebaseUid, RepositoryCallback<Void> cb) {
        db.collection(COLLECTION_NAME)
                .whereEqualTo("firebaseUid", firebaseUid)
                .get()
                .addOnSuccessListener(query -> {
                    for (QueryDocumentSnapshot doc : query) {
                        doc.getReference().delete();
                    }
                    cb.onSuccess(null);
                })
                .addOnFailureListener(cb::onFailure);
    }
}
