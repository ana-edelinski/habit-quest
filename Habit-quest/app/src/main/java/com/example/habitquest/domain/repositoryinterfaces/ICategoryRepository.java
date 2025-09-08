package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.Category;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.util.List;

public interface ICategoryRepository {
    // 1) Jednokratno čitanje (ako ti treba)
    void getAllOnce(String userId, RepositoryCallback<List<Category>> cb);

    // 2) Real-time slušanje (preporučeno za listu u UI)
    Closeable listenAll(String userId, CategoriesListener listener);
    interface CategoriesListener {
        void onChanged(List<Category> categories);
        void onError(Exception e);
    }

    // 3) Kreiranje sa validacijom (#RRGGBB, jedinstvena boja)
    void create(String userId, String name, String colorHex, RepositoryCallback<Category> cb);

    // 4) Izmena (ako je promenjena boja → ponovo proveri jedinstvenost)
    void update(Category category, RepositoryCallback<Void> cb);

    // 5) Brisanje dozvoljeno samo ako nema aktivnih taskova u toj kategoriji
    void delete(String userId, /*String ili Long*/ Object categoryId, RepositoryCallback<Void> cb);

    // 6) Instant validacija u formi
    void isColorAvailable(String userId, String colorHex, RepositoryCallback<Boolean> cb);
}
