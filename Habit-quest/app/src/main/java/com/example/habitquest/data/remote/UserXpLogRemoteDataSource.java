package com.example.habitquest.data.remote;

import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;
import com.example.habitquest.domain.model.ImportanceLevel;
import com.example.habitquest.domain.model.DifficultyLevel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.Query;


import java.util.ArrayList;
import java.util.Calendar;
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



    public void countLogsForToday(String firebaseUid, DifficultyLevel difficulty, ImportanceLevel importance,
                                  RepositoryCallback<Integer> cb) {
        long startOfDay = getStartOfDayMillis();

        Query query = FirebaseFirestore.getInstance()
                .collection("userXpLogs")
                .whereEqualTo("firebaseUid", firebaseUid)
                .whereGreaterThanOrEqualTo("completedAt", startOfDay);

        if (difficulty != null) {
            query = query.whereEqualTo("difficultyLevel", difficulty.name());
        }
        if (importance != null) {
            query = query.whereEqualTo("importanceLevel", importance.name());
        }

        query.get()
                .addOnSuccessListener(snap -> cb.onSuccess(snap.size()))
                .addOnFailureListener(cb::onFailure);
    }


    public void countLogsForThisWeek(String firebaseUid, DifficultyLevel difficulty, RepositoryCallback<Integer> cb) {
        long startOfWeek = getStartOfWeekMillis();

        db.collection("userXpLogs")
                .whereEqualTo("firebaseUid", firebaseUid)
                .whereEqualTo("difficultyLevel", difficulty.name())
                .whereGreaterThanOrEqualTo("completedAt", startOfWeek)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(snap.size()))
                .addOnFailureListener(cb::onFailure);
    }

    public void countLogsForThisMonth(String firebaseUid, ImportanceLevel importance, RepositoryCallback<Integer> cb) {
        long startOfMonth = getStartOfMonthMillis();

        db.collection("userXpLogs")
                .whereEqualTo("firebaseUid", firebaseUid)
                .whereEqualTo("importanceLevel", importance.name())
                .whereGreaterThanOrEqualTo("completedAt", startOfMonth)
                .get()
                .addOnSuccessListener(snap -> cb.onSuccess(snap.size()))
                .addOnFailureListener(cb::onFailure);
    }

    private long getStartOfDayMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getStartOfWeekMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    private long getStartOfMonthMillis() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }


}
