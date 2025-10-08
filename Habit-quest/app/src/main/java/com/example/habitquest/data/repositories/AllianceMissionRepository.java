package com.example.habitquest.data.repositories;

import com.example.habitquest.domain.repositoryinterfaces.IAllianceMissionRepository;

import com.example.habitquest.domain.model.AllianceMission;
import com.example.habitquest.domain.model.MemberMissionProgress;
import com.example.habitquest.domain.model.MissionAction;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;


import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AllianceMissionRepository implements IAllianceMissionRepository {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION = "alliance_missions";

    @Override
    public void createMission(String allianceId, List<String> memberIds, RepositoryCallback<AllianceMission> callback) {
        String id = db.collection(COLLECTION).document().getId();
        AllianceMission mission = new AllianceMission(id, allianceId, memberIds.size());

        for (String memberId : memberIds) {
            mission.addMember(memberId);
        }

        db.collection(COLLECTION).document(id)
                .set(mission)
                .addOnSuccessListener(aVoid -> callback.onSuccess(mission))
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void updateMemberProgress(String missionId, String userId, MissionAction action, RepositoryCallback<Void> callback) {
        DocumentReference ref = db.collection(COLLECTION).document(missionId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(ref);
                    if (!snapshot.exists()) return null;

                    AllianceMission mission = snapshot.toObject(AllianceMission.class);
                    if (mission == null || !mission.isActive()) return null;

                    Map<String, MemberMissionProgress> map = mission.getMemberProgress();
                    if (map == null) map = new HashMap<>();

                    MemberMissionProgress progress = map.getOrDefault(userId, new MemberMissionProgress(userId));

                    int damage = 0;
                    switch (action) {
                        case SHOP_PURCHASE: damage = progress.onShopPurchase(); break;
                        case BOSS_HIT: damage = progress.onBossHit(); break;
                        case EASY_TASK: damage = progress.onEasyTaskSolved(false); break;
                        case HARD_TASK: damage = progress.onHardTaskSolved(); break;
                        case NO_FAILED_TASKS: damage = progress.onNoFailedTasks(); break;
                        case MESSAGE_SENT: damage = progress.onMessageSent(); break;
                    }

                    if (damage > 0) {
                        // 🔹 ažuriraj mapu članova
                        map.put(userId, progress);
                        mission.setMemberProgress(map);

                        // 🔹 primeni štetu na bossa
                        mission.applyDamage(userId, damage);

                        // 🔹 snimi celu misiju
                        transaction.set(ref, mission);
                    }

                    return null;
                })
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }


    @Override
    public void listenMission(String missionId, RepositoryCallback<AllianceMission> callback) {
        db.collection(COLLECTION).document(missionId)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) {
                        callback.onFailure(e);
                        return;
                    }
                    if (snapshot != null && snapshot.exists()) {
                        callback.onSuccess(snapshot.toObject(AllianceMission.class));
                    }
                });
    }

    @Override
    public void getActiveMission(String allianceId, RepositoryCallback<AllianceMission> callback) {
        db.collection(COLLECTION)
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        callback.onSuccess(query.getDocuments().get(0).toObject(AllianceMission.class));
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

    @Override
    public void finishMission(String missionId, boolean victory, long remainingHP, RepositoryCallback<Void> callback) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("active", false);
        updates.put("finished", true);
        updates.put("victory", victory);
        updates.put("remainingHP", remainingHP);
        updates.put("endDate", Timestamp.now());

        db.collection(COLLECTION)
                .document(missionId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess(null))
                .addOnFailureListener(callback::onFailure);
    }

    public void getLastFinishedMission(String allianceId, RepositoryCallback<AllianceMission> callback) {
        db.collection(COLLECTION)
                .whereEqualTo("allianceId", allianceId)
                .whereEqualTo("finished", true)
                .orderBy("endDate", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        AllianceMission mission = query.getDocuments().get(0).toObject(AllianceMission.class);
                        callback.onSuccess(mission);
                    } else {
                        callback.onSuccess(null);
                    }
                })
                .addOnFailureListener(callback::onFailure);
    }

}
