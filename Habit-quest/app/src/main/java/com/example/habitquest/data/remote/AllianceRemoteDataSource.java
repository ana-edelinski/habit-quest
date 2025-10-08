package com.example.habitquest.data.remote;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.Alliance;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.presentation.services.AllianceNotificationService;
import com.example.habitquest.utils.NotificationHelper;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class AllianceRemoteDataSource {

    private static final String COLLECTION_NAME = "alliances";
    private final FirebaseFirestore db;
    private ListenerRegistration chatListener;
    private String lastShownMessageId = null;

    public AllianceRemoteDataSource() {
        db = FirebaseFirestore.getInstance();
    }

    public void createAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        String leaderId = alliance.getLeaderId();

        db.collection("users").document(leaderId).get()
                .addOnSuccessListener(userDoc -> {
                    String currentAllianceId = userDoc.getString("allianceId");
                    if (currentAllianceId != null) {
                        callback.onFailure(new Exception("You are already a member of another alliance and cannot create a new one."));
                        return;
                    }

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

                                db.collection("users").document(leaderId)
                                        .update("allianceId", alliance.getId())
                                        .addOnSuccessListener(x -> callback.onSuccess(null))
                                        .addOnFailureListener(callback::onFailure);
                            })
                            .addOnFailureListener(callback::onFailure);
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void getUserAlliance(String userId, RepositoryCallback<Alliance> callback) {
        db.collection(COLLECTION_NAME)
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Alliance alliance = query.getDocuments().get(0).toObject(Alliance.class);
                        callback.onSuccess(alliance);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    public void updateAlliance(Alliance alliance, RepositoryCallback<Void> callback) {
        if (alliance == null || alliance.getId() == null) {
            callback.onFailure(new Exception("Alliance or ID is null"));
            return;
        }

        db.collection(COLLECTION_NAME)
                .document(alliance.getId())
                .set(alliance)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void leaveAlliance(String allianceId, String userId, RepositoryCallback<Void> callback) {
        DocumentReference allianceRef = db.collection(COLLECTION_NAME).document(allianceId);
        allianceRef.get().addOnSuccessListener(doc -> {
            Alliance alliance = doc.toObject(Alliance.class);
            if (alliance == null) {
                callback.onFailure(new Exception("Alliance not found."));
                return;
            }
            if (alliance.isMissionActive()) {
                callback.onFailure(new Exception("You cannot leave the alliance while a mission is active."));
                return;
            }
            alliance.getMembers().remove(userId);
            allianceRef.update("members", alliance.getMembers())
                    .addOnSuccessListener(aVoid -> db.collection("users").document(userId)
                            .update("allianceId", null)
                            .addOnSuccessListener(v -> callback.onSuccess(null))
                            .addOnFailureListener(callback::onFailure))
                    .addOnFailureListener(callback::onFailure);
        }).addOnFailureListener(callback::onFailure);
    }

    public void acceptAllianceInvite(Context context, String allianceId, String userId, String username, RepositoryCallback<Void> callback) {
        getUserAlliance(userId, new RepositoryCallback<Alliance>() {
            @Override
            public void onSuccess(Alliance currentAlliance) {
                if (currentAlliance != null) {
                    if (currentAlliance.getLeaderId().equals(userId)) {
                        callback.onFailure(new Exception("You are the leader of your current alliance and cannot leave it."));
                        return;
                    }

                    if (currentAlliance.isMissionActive()) {
                        callback.onFailure(new Exception("You cannot leave your current alliance while a mission is active."));
                        return;
                    }

                    leaveAlliance(currentAlliance.getId(), userId, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            joinNewAlliance(context, allianceId, userId, username, callback);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            callback.onFailure(e);
                        }
                    });
                } else {
                    joinNewAlliance(context, allianceId, userId, username, callback);
                }
            }

            @Override
            public void onFailure(Exception e) {
                callback.onFailure(e);
            }
        });
    }

    private void joinNewAlliance(Context context, String allianceId, String userId, String username, RepositoryCallback<Void> callback) {
        DocumentReference allianceRef = db.collection(COLLECTION_NAME).document(allianceId);
        db.runTransaction(transaction -> {
                    transaction.update(allianceRef, "requests", FieldValue.arrayRemove(userId));
                    transaction.update(allianceRef, "members", FieldValue.arrayUnion(userId));
                    return null;
                })
                .addOnSuccessListener(aVoid -> {
                    db.collection("users").document(userId)
                            .update("allianceId", allianceId, "allianceInvites", FieldValue.arrayRemove(allianceId))
                            .addOnSuccessListener(v -> Log.d("Alliance", "Joined new alliance: " + allianceId));
                    allianceRef.get().addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String leaderId = snapshot.getString("leaderId");
                            String allianceName = snapshot.getString("name");
                            if (leaderId != null && !leaderId.equals(userId)) {
                                db.collection("users").document(leaderId)
                                        .update("allianceAcceptedNotifications", FieldValue.arrayUnion(
                                                username + " accepted invite to " + allianceName
                                        ));
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
                .addOnSuccessListener(aVoid -> db.collection("users").document(userId)
                        .update("allianceInvites", FieldValue.arrayRemove(allianceId))
                        .addOnSuccessListener(v -> {
                            Log.d("Alliance", "Invite fully removed for " + userId);
                            callback.onSuccess(null);
                        })
                        .addOnFailureListener(callback::onFailure))
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
                                .addOnFailureListener(ex -> Log.e("Alliance", "Failed to fetch alliance details", ex));
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
                        NotificationHelper.createChannels(context);
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

    public void disbandAlliance(String allianceId, RepositoryCallback<Void> callback) {
        DocumentReference allianceRef = db.collection(COLLECTION_NAME).document(allianceId);
        allianceRef.get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                callback.onFailure(new Exception("Alliance not found"));
                return;
            }

            Alliance alliance = doc.toObject(Alliance.class);
            if (alliance == null) {
                callback.onFailure(new Exception("Invalid alliance data"));
                return;
            }

            if (alliance.isMissionActive()) {
                callback.onFailure(new Exception("Cannot disband while a mission is active."));
                return;
            }

            List<String> allMembers = alliance.getMembers();
            if (allMembers != null) {
                for (String uid : allMembers) {
                    db.collection("users").document(uid).update("allianceId", null);
                }
            }

            allianceRef.delete()
                    .addOnSuccessListener(unused -> callback.onSuccess(null))
                    .addOnFailureListener(callback::onFailure);

        }).addOnFailureListener(callback::onFailure);
    }

    public void getUserAllianceId(String userId, RepositoryCallback<String> cb) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("alliances")
                .whereArrayContains("members", userId)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String allianceId = query.getDocuments().get(0).getId();
                        cb.onSuccess(allianceId);
                    } else {
                        cb.onSuccess(null);
                    }
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void listenForAllianceChatMessages(String allianceId, String currentUserId, Context context) {
        if (chatListener != null) return;

        chatListener = db.collection("alliances")
                .document(allianceId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(1)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null || snapshots.isEmpty()) return;

                    AllianceMessage msg = snapshots.getDocuments().get(0).toObject(AllianceMessage.class);
                    if (msg == null || msg.getSenderId().equals(currentUserId)) return;

                    if (msg.getId() != null && msg.getId().equals(lastShownMessageId)) return;
                    lastShownMessageId = msg.getId();

                    AppPreferences prefs = new AppPreferences(context);
                    if (prefs.isChatOpen()) return;

                    NotificationHelper.showAllianceChatMessage(
                            context,
                            msg.getSenderName(),
                            msg.getText(),
                            allianceId
                    );
                });
    }

    public void removeChatListener() {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
            lastShownMessageId = null;
        }
    }
}
