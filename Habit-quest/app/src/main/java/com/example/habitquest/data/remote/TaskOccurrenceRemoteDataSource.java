package com.example.habitquest.data.remote;

import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

public class TaskOccurrenceRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public void saveOccurrences(String firebaseUid, String taskId, List<TaskOccurrence> occurrences, RepositoryCallback<Void> cb) {
        WriteBatch batch = db.batch();
        CollectionReference colRef = db.collection("users").document(firebaseUid)
                .collection("tasks").document(taskId).collection("occurrences");

        for (TaskOccurrence occurrence : occurrences) {
            DocumentReference docRef = colRef.document();
            occurrence.setId(docRef.getId());
            batch.set(docRef, occurrence);
        }

        batch.commit()
                .addOnSuccessListener(unused -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    public void deleteAllForTask(String firebaseUid, String taskId, RepositoryCallback<Void> cb) {
        CollectionReference colRef = db.collection("users").document(firebaseUid)
                .collection("tasks").document(taskId).collection("occurrences");

        colRef.get().addOnSuccessListener(qs -> {
            WriteBatch batch = db.batch();
            for (DocumentSnapshot doc : qs.getDocuments()) {
                batch.delete(doc.getReference());
            }
            batch.commit()
                    .addOnSuccessListener(unused -> cb.onSuccess(null))
                    .addOnFailureListener(cb::onFailure);
        }).addOnFailureListener(cb::onFailure);
    }

    public void updateOccurrenceStatus(String firebaseUid, String taskId, String occurrenceId, TaskStatus status, RepositoryCallback<Void> cb) {
        DocumentReference docRef = db.collection("users").document(firebaseUid)
                .collection("tasks").document(taskId)
                .collection("occurrences").document(occurrenceId);

        docRef.update("status", status.name())
                .addOnSuccessListener(unused -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    public void getOccurrencesForTask(String firebaseUid, String taskId, RepositoryCallback<List<TaskOccurrence>> cb) {
        db.collection("users").document(firebaseUid)
                .collection("tasks").document(taskId)
                .collection("occurrences")
                .get()
                .addOnSuccessListener(qs -> {
                    List<TaskOccurrence> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        TaskOccurrence occ = doc.toObject(TaskOccurrence.class);
                        list.add(occ);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onFailure);
    }
}

