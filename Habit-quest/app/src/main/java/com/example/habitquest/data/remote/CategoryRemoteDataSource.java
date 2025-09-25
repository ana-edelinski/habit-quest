package com.example.habitquest.data.remote;

import androidx.annotation.NonNull;

import com.example.habitquest.domain.model.Category;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CategoryRemoteDataSource {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    /* ---------- public API koji koristi tvoj CategoryRepository ---------- */

    /** Jednokratno čitanje svih kategorija korisnika. */
    public void fetchAll(@NonNull String firebaseUid, @NonNull RepositoryCallback<List<Category>> cb) {
        cats(firebaseUid)
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(snap -> {
                    List<Category> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Category c = d.toObject(Category.class);
                        if (c != null) {
                            // Ako u dokumentu nema polja id, preuzmi ga iz docId
                            if (c.getId() == null) {
                                try { c.setId(Long.parseLong(d.getId())); } catch (Exception ignored) {}
                            }
                            out.add(c);
                        }
                    }
                    cb.onSuccess(out);
                })
                .addOnFailureListener(cb::onFailure);
    }

    /** Real-time slušanje cele liste. */
    public Closeable listenAll(@NonNull String firebaseUid, @NonNull RemoteListener listener) {
        ListenerRegistration reg = cats(firebaseUid)
                .orderBy("name", Query.Direction.ASCENDING)
                .addSnapshotListener((snap, e) -> {
                    if (e != null) { listener.onError(e); return; }
                    if (snap == null) { listener.onChanged(new ArrayList<>()); return; }
                    List<Category> out = new ArrayList<>();
                    for (DocumentSnapshot d : snap.getDocuments()) {
                        Category c = d.toObject(Category.class);
                        if (c != null) {
                            if (c.getId() == null) {
                                try { c.setId(Long.parseLong(d.getId())); } catch (Exception ignored) {}
                            }
                            out.add(c);
                        }
                    }
                    listener.onChanged(out);
                });

        return () -> reg.remove();
    }

    /** Provera da li je boja već zauzeta. */
    public void isColorTaken(@NonNull String firebaseUid, @NonNull String colorHex, @NonNull RepositoryCallback<Boolean> cb) {
        String normalized = colorHex.toUpperCase();
        cats(firebaseUid)
                .whereEqualTo("colorHex", normalized)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> cb.onSuccess(!q.isEmpty()))
                .addOnFailureListener(cb::onFailure);
    }

    /** Kreiranje nove kategorije. */
    public void create(@NonNull String firebaseUid, @NonNull String name, @NonNull String colorHex,
                       @NonNull RepositoryCallback<Category> cb) {
        long now = System.currentTimeMillis();
        long newId = now; // docId

        Category c = new Category(
                newId,
                0L, // local userId nije bitan za Firestore
                name.trim(),
                colorHex.toUpperCase(),
                now,
                now
        );

        cats(firebaseUid).document(String.valueOf(newId))
                .set(c)
                .addOnSuccessListener(v -> cb.onSuccess(c))
                .addOnFailureListener(cb::onFailure);
    }

    /** Izmena postojeće kategorije. */
    public void update(@NonNull String firebaseUid, @NonNull Category category, @NonNull RepositoryCallback<Void> cb) {
        if (category.getId() == null) {
            cb.onFailure(new IllegalArgumentException("Category id is null"));
            return;
        }
        long now = System.currentTimeMillis();
        category.setUpdatedAt(now);

        String docId = String.valueOf(category.getId());

        cats(firebaseUid).document(docId)
                .set(category)
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    /** Da li postoje aktivni taskovi koji koriste ovu kategoriju. */
    public void hasActiveTasks(@NonNull String firebaseUid, @NonNull Object categoryId,
                               @NonNull RepositoryCallback<Boolean> cb) {
        long catId = parseId(categoryId);

        tasks(firebaseUid)
                .whereEqualTo("categoryId", catId)
                .whereEqualTo("active", true)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> cb.onSuccess(!q.isEmpty()))
                .addOnFailureListener(cb::onFailure);
    }

    /** Brisanje kategorije. */
    public void delete(@NonNull String firebaseUid, @NonNull Object categoryId, @NonNull RepositoryCallback<Void> cb) {
        String docId = String.valueOf(parseId(categoryId));
        cats(firebaseUid).document(docId)
                .delete()
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    /* ---------- helpers ---------- */

    public interface RemoteListener {
        void onChanged(List<Category> list);
        void onError(Exception e);
    }

    private CollectionReference cats(String firebaseUid) {
        return db.collection("users").document(firebaseUid).collection("categories");
    }

    private CollectionReference tasks(String firebaseUid) {
        return db.collection("users").document(firebaseUid).collection("tasks");
    }

    private static long parseId(Object idObj) {
        if (idObj instanceof Long) return (Long) idObj;
        if (idObj instanceof Integer) return ((Integer) idObj).longValue();
        if (idObj instanceof String) {
            try { return Long.parseLong((String) idObj); } catch (Exception ignored) {}
        }
        throw new IllegalArgumentException("Unsupported id type: " + idObj);
    }
}
