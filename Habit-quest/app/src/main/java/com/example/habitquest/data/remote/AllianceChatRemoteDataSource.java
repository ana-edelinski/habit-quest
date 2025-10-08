package com.example.habitquest.data.remote;

import androidx.annotation.Nullable;

import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class AllianceChatRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private ListenerRegistration listenerRegistration;

    private CollectionReference getMessagesRef(String allianceId) {
        return db.collection("alliances")
                .document(allianceId)
                .collection("messages");
    }

    public void sendMessage(String allianceId, AllianceMessage message, RepositoryCallback<Void> cb) {
        DocumentReference docRef = getMessagesRef(allianceId).document();
        message.setId(docRef.getId());
        docRef.set(message)
                .addOnSuccessListener(a -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    public void listenForMessages(String allianceId, RepositoryCallback<List<AllianceMessage>> cb) {
        listenerRegistration = getMessagesRef(allianceId)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    List<AllianceMessage> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshots.getDocuments()) {
                        AllianceMessage msg = doc.toObject(AllianceMessage.class);
                        if (msg != null) {
                            Object avatarObj = doc.get("senderAvatar");
                            if (avatarObj instanceof Long) {
                                msg.setSenderAvatar(((Long) avatarObj).intValue());
                            }

                            msg.setId(doc.getId());
                            list.add(msg);
                        }
                    }

                    cb.onSuccess(list);
                });
    }

    public void removeListener() {
        if (listenerRegistration != null) listenerRegistration.remove();
    }
}
