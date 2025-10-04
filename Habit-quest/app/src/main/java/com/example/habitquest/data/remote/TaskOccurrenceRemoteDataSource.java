package com.example.habitquest.data.remote;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.presentation.fragments.OccurrenceDetailFragment;
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
                        occ = applyExpirationLogic(occ, doc.getReference());
                        list.add(occ);
                    }
                    cb.onSuccess(list);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void getById(String firebaseUid, String taskId, String occurrenceId, RepositoryCallback<TaskOccurrence> cb) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users")
                .document(firebaseUid)
                .collection("tasks")
                .document(taskId)
                .collection("occurrences")
                .document(occurrenceId);

        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                TaskOccurrence result = task.getResult().toObject(TaskOccurrence.class);
                if (result != null) {
                    result.setId(task.getResult().getId()); // ako u modelu imaš setId
                    applyExpirationLogic(result, ref);
                }
                cb.onSuccess(result);
            } else {
                cb.onFailure(new Exception("Occurrence not found: " + occurrenceId));
            }
        }).addOnFailureListener(cb::onFailure);
    }


    public static TaskOccurrence applyExpirationLogic(TaskOccurrence o, DocumentReference ref) {
        if (o == null) return null;
        if ( o.getDate() != null) {
            long now = System.currentTimeMillis();
            long diff = now - o.getDate();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff > 3 && o.getStatus() == TaskStatus.ACTIVE) {
                o.setStatus(TaskStatus.NOT_DONE);
                ref.update("status", TaskStatus.NOT_DONE.name());
            }
        }
        return o;
    }


    public void countOccurrencesInPeriod(String firebaseUid, long start, long end, RepositoryCallback<Integer> cb) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Kolekcija 'task_occurrences' u kojoj su svi occurrence dokumenti
        db.collection("task_occurrences")
                .whereEqualTo("firebaseUid", firebaseUid)
                .whereGreaterThanOrEqualTo("date", start)
                .whereLessThanOrEqualTo("date", end)
                .get()
                .addOnSuccessListener(snapshot -> {
                    int count = 0;
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        TaskOccurrence occ = doc.toObject(TaskOccurrence.class);
                        if (occ == null) continue;

                        TaskStatus status = occ.getStatus();
                        if (status == TaskStatus.COMPLETED || status == TaskStatus.NOT_DONE) {
                            count++;
                        }
                    }
                    cb.onSuccess(count);
                })
                .addOnFailureListener(cb::onFailure);
    }


}

