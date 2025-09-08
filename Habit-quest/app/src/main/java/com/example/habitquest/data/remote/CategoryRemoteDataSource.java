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
    public void fetchAll(@NonNull String userId, @NonNull RepositoryCallback<List<Category>> cb) {
        cats(userId)
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

    /** Real-time slušanje cele liste. Obavezno pozovi close() na povratnoj vrednosti kada više ne treba. */
    public Closeable listenAll(@NonNull String userId, @NonNull RemoteListener listener) {
        ListenerRegistration reg = cats(userId)
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

        // Vrati Closeable koji samo poziva reg.remove()
        return new Closeable() {
            @Override public void close() throws IOException { reg.remove(); }
        };
    }

    /** Provera da li je boja već zauzeta (case-insensitive → normalize na UPPER pre slanja). */
    public void isColorTaken(@NonNull String userId, @NonNull String colorHex, @NonNull RepositoryCallback<Boolean> cb) {
        String normalized = colorHex.toUpperCase();
        cats(userId)
                .whereEqualTo("colorHex", normalized)
                .limit(1)
                .get()
                .addOnSuccessListener(q -> cb.onSuccess(!q.isEmpty()))
                .addOnFailureListener(cb::onFailure);
    }

    /** Kreiranje nove kategorije: generišemo long id i koristimo ga kao docId. */
    public void create(@NonNull String userId, @NonNull String name, @NonNull String colorHex,
                       @NonNull RepositoryCallback<Category> cb) {
        long now = System.currentTimeMillis();
        long newId = now; // dovoljno za projekat; možeš dodati random sufiks ako želiš
        Category c = new Category(
                newId,
                parseUserIdOrThrow(userId),
                name.trim(),
                colorHex.toUpperCase(),
                now,
                now
        );

        cats(userId).document(String.valueOf(newId))
                .set(c)
                .addOnSuccessListener(v -> cb.onSuccess(c))
                .addOnFailureListener(cb::onFailure);
    }

    /** Izmena postojeće kategorije (po id-u). */
    public void update(@NonNull Category category, @NonNull RepositoryCallback<Void> cb) {
        if (category.getId() == null) {
            cb.onFailure(new IllegalArgumentException("Category id is null"));
            return;
        }
        long now = System.currentTimeMillis();
        category.setUpdatedAt(now);

        String userIdStr = String.valueOf(category.getUserId());
        String docId = String.valueOf(category.getId());

        cats(userIdStr).document(docId)
                .set(category) // set() je prost; ako želiš delimično: .set(category, SetOptions.merge())
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    /** Da li postoje aktivni taskovi koji koriste ovu kategoriju. */
    public void hasActiveTasks(@NonNull String userId, @NonNull Object categoryId,
                               @NonNull RepositoryCallback<Boolean> cb) {
        long catId = parseId(categoryId);

        tasks(userId)
                .whereEqualTo("categoryId", catId)     // pretpostavka: task.categoryId je LONG u Firestore-u
                .whereEqualTo("active", true)          // pretpostavka: task.active = boolean
                .limit(1)
                .get()
                .addOnSuccessListener(q -> cb.onSuccess(!q.isEmpty()))
                .addOnFailureListener(cb::onFailure);
    }

    /** Brisanje dokumenta po id-u. */
    public void delete(@NonNull String userId, @NonNull Object categoryId, @NonNull RepositoryCallback<Void> cb) {
        String docId = String.valueOf(parseId(categoryId));
        cats(userId).document(docId)
                .delete()
                .addOnSuccessListener(v -> cb.onSuccess(null))
                .addOnFailureListener(cb::onFailure);
    }

    /* ---------- helper interfejs i util-i ---------- */

    public interface RemoteListener {
        void onChanged(List<Category> list);
        void onError(Exception e);
    }

    private CollectionReference cats(String userId) {
        // šema: /users/{uid}/categories/{categoryId}
        return db.collection("users").document(userId).collection("categories");
    }

    private CollectionReference tasks(String userId) {
        // šema: /users/{uid}/tasks/{taskId}
        return db.collection("users").document(userId).collection("tasks");
    }

    private static long parseUserIdOrThrow(String userIdStr) {
        try { return Long.parseLong(userIdStr); }
        catch (Exception e) { throw new IllegalArgumentException("Local domain expects numeric userId, got: " + userIdStr, e); }
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
