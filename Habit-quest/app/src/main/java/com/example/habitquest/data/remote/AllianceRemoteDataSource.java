package com.example.habitquest.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.presentation.services.AllianceNotificationService;
import com.example.habitquest.utils.NotificationHelper;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AllianceRemoteDataSource {

    private static final String COLLECTION_NAME = "alliances";
    private final FirebaseFirestore db;

    public AllianceRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public void createAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        db.collection(COLLECTION_NAME)
                .document(alliance.getId())
                .set(alliance)
                .addOnSuccessListener(aVoid -> {
                    for (String friendUid : alliance.getRequests()) {
                        if (friendUid.equals(alliance.getLeaderId())) continue;

                        db.collection("users").document(friendUid)
                                .update("allianceInvites", FieldValue.arrayUnion(alliance.getId()))
                                .addOnFailureListener(e -> Log.w("Alliance", "Invite add failed", e));
                    }

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void acceptAllianceInvite(Context context, String allianceId, String userId, String username, RepositoryCallback<Void> callback) {
        DocumentReference allianceRef = db.collection(COLLECTION_NAME).document(allianceId);

        db.runTransaction(transaction -> {
                    transaction.update(allianceRef, "requests", FieldValue.arrayRemove(userId));
                    transaction.update(allianceRef, "members", FieldValue.arrayUnion(userId));
                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(userId)
                            .update("allianceInvites", FieldValue.arrayRemove(allianceId))
                            .addOnSuccessListener(v -> Log.d("Alliance", "Invite removed for " + userId));

                    allianceRef.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String leaderId = snapshot.getString("leaderId");
                            String allianceName = snapshot.getString("name");

                            if (leaderId != null && !leaderId.equals(userId)) {
                                db.collection("users").document(leaderId)
                                        .update("allianceAcceptedNotifications", FieldValue.arrayUnion(
                                                username + " accepted invite to " + allianceName
                                        ))
                                        .addOnSuccessListener(v ->
                                                Log.d("Alliance", "Leader notified: " + leaderId))
                                        .addOnFailureListener(e ->
                                                Log.w("Alliance", "Leader notify failed", e));
                            }
                        }
                    });

                    callback.onSuccess(null);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void rejectAllianceInvite(String allianceId, String userId, RepositoryCallback<Void> callback) {
        DocumentReference allianceRef = db.collection(COLLECTION_NAME).document(allianceId);
        allianceRef.update("requests", FieldValue.arrayRemove(userId))
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(userId)
                            .update("allianceInvites", FieldValue.arrayRemove(allianceId))
                            .addOnSuccessListener(v -> {
                                Log.d("Alliance", "Invite fully removed for " + userId);
                                callback.onSuccess(null);
                            })
                            .addOnFailureListener(e -> {
                                Log.w("Alliance", "Failed to remove invite from user doc", e);
                                callback.onFailure(e);
                            });
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void listenForAllianceInvites(String uid, Context context) {
        db.collection("users").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    List<String> invites = (List<String>) snapshot.get("allianceInvites");
                    if (invites == null || invites.isEmpty()) return;

                    for (String allianceId : invites) {
                        db.collection(COLLECTION_NAME).document(allianceId)
                                .get()
                                .addOnSuccessListener(doc -> {
                                    Alliance alliance = doc.toObject(Alliance.class);
                                    if (alliance == null) return;

                                    if (!AllianceNotificationService.isRunning(allianceId)) {
                                        Intent serviceIntent = new Intent(context, AllianceNotificationService.class);
                                        serviceIntent.putExtra(AllianceNotificationService.EXTRA_ALLIANCE_ID, allianceId);
                                        serviceIntent.putExtra(AllianceNotificationService.EXTRA_ALLIANCE_NAME, alliance.getName());
                                        serviceIntent.putExtra(AllianceNotificationService.EXTRA_INVITER_NAME, alliance.getLeaderName());

                                        Context appContext = context.getApplicationContext();
                                        try {
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                appContext.startForegroundService(serviceIntent);
                                            } else {
                                                appContext.startService(serviceIntent);
                                            }
                                        } catch (Exception ex) {
                                            Log.e("Alliance", "Failed to start notification service", ex);
                                        }
                                    }
                                })
                                .addOnFailureListener(ex ->
                                        Log.e("Alliance", "Failed to fetch alliance details", ex)
                                );
                    }
                });
    }

    public void listenForAllianceAccepts(String uid, Context context) {
        db.collection("users").document(uid)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null || snapshot == null || !snapshot.exists()) return;

                    List<String> accepted = (List<String>) snapshot.get("allianceAcceptedNotifications");
                    if (accepted == null || accepted.isEmpty()) return;

                    for (String message : accepted) {
                        NotificationHelper.createChannel(context);

                        String memberName = "Someone";
                        String allianceName = "";

                        if (message.contains("accepted invite to")) {
                            String[] parts = message.split("accepted invite to");
                            memberName = parts[0].trim();
                            if (parts.length > 1) {
                                allianceName = parts[1].trim();
                            }
                        }

                        NotificationHelper.showAllianceAccepted(context, memberName, allianceName);
                    }


                    db.collection("users").document(uid)
                            .update("allianceAcceptedNotifications", new ArrayList<>());
                });
    }
}
