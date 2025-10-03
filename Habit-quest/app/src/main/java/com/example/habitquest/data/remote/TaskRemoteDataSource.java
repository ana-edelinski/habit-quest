package com.example.habitquest.data.remote;

import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.SetOptions;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public class TaskRemoteDataSource {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference tasks(String firebaseUid) {
        return db.collection("users").document(firebaseUid).collection("tasks");
    }

    public void fetchAll(@NonNull String firebaseUid, @NonNull RepositoryCallback<List<Task>> cb) {
        tasks(firebaseUid)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Task> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Task t = d.toObject(Task.class);
                        if (t != null) {
                            t.setId(d.getId()); // koristimo Firestore documentId
                            t = applyExpirationLogic(t, d.getReference());
                        }
                        out.add(t);
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    public void create(@NonNull String firebaseUid, @NonNull Task task, @NonNull RepositoryCallback<Task> cb) {
        // Firestore sam generiše ID
        DocumentReference docRef = tasks(firebaseUid).document();
        String newId = docRef.getId();
        task.setId(newId);

        docRef.set(task)
                .addOnSuccessListener(v -> cb.onSuccess(task))
                .addOnFailureListener(cb::onFailure);
    }

    public void update(@NonNull String firebaseUid, @NonNull Task task, @NonNull RepositoryCallback<Void> cb) {
        if (task.getId() == null) {
            cb.onFailure(new IllegalArgumentException("Task id is null"));
            return;
        }
        tasks(firebaseUid).document(task.getId())
                .set(task, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    public void delete(@NonNull String firebaseUid, @NonNull String taskId, @NonNull RepositoryCallback<Void> cb) {
        tasks(firebaseUid).document(taskId)
                .delete()
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    public Closeable listenAll(@NonNull String firebaseUid, @NonNull RemoteListener listener) {
        ListenerRegistration reg = tasks(firebaseUid)
                .orderBy("date", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }
                    if (snap == null) {
                        listener.onChanged(new ArrayList<>());
                        return;
                    }
                    List<Task> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Task t = d.toObject(Task.class);
                        if (t != null) {
                            t.setId(d.getId()); // uvek postavljamo Firestore documentId
                            t = applyExpirationLogic(t, d.getReference());
                        }
                        out.add(t);
                    }
                    listener.onChanged(out);
                });

        // vrati Closeable koji prekida listener
        return reg::remove;
    }

    public void getById(String firebaseUid, String taskId, RepositoryCallback<Task> cb) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference ref = db.collection("users")
                .document(firebaseUid)
                .collection("tasks")
                .document(taskId);


        ref.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                Task result = task.getResult().toObject(Task.class);
                if (result != null) {
                    result.setId(task.getResult().getId()); // postavi ID dokumenta
                    result = applyExpirationLogic(result, ref);
                }
                cb.onSuccess(result);
            } else {
                cb.onFailure(new Exception("Task not found: " + taskId));
            }
        }).addOnFailureListener(cb::onFailure);
    }


    public static Task applyExpirationLogic(Task t, DocumentReference ref) {
        if (t == null) return null;
        if (!t.isRecurring() && t.getDate() != null) {
            long now = System.currentTimeMillis();
            long diff = now - t.getDate();
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            if (daysDiff > 3 && t.getStatus() == TaskStatus.ACTIVE) {
                t.setStatus(TaskStatus.NOT_DONE);
                ref.update("status", TaskStatus.NOT_DONE.name());
            }
        }
        return t;
    }



    // interfejs za eventove
    public interface RemoteListener {
        void onChanged(List<Task> list);
        void onError(Exception e);
    }
}
