package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.local.datasource.CategoriesLocalDataSource;
import com.example.habitquest.data.remote.CategoryRemoteDataSource;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.repositoryinterfaces.ICategoryRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.util.List;

public class CategoryRepository implements ICategoryRepository {
    private final CategoriesLocalDataSource local;   // SQLite
    private final CategoryRemoteDataSource remote;   // Firestore

    public CategoryRepository(Context context) {
        this.local  = new CategoriesLocalDataSource(context);
        this.remote = new CategoryRemoteDataSource();
    }

    @Override
    public void getAllOnce(String userId, RepositoryCallback<List<Category>> cb) {
        remote.fetchAll(userId, new RepositoryCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> remoteList) {
                local.replaceAll(userId, remoteList); // cache
                cb.onSuccess(remoteList);
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public Closeable listenAll(String userId, CategoriesListener listener) {
        // Forward-uj Firestore snapshot u UI i drži lokalni cache usklađen
        return remote.listenAll(userId, new CategoryRemoteDataSource.RemoteListener() {
            @Override public void onChanged(List<Category> list) {
                local.replaceAll(userId, list);
                listener.onChanged(list);
            }
            @Override public void onError(Exception e) { listener.onError(e); }
        });
    }

    @Override
    public void create(String userId, String name, String colorHex, RepositoryCallback<Category> cb) {
        // (1) trivijalne validacije
        // (2) proveri jedinstvenost boje remote-om (ili prvo lokalno, pa potvrdi remote-om)
        Long userIdLong = Long.parseLong(userId);
        remote.isColorTaken(userId, colorHex, new RepositoryCallback<Boolean>() {
            @Override public void onSuccess(Boolean taken) {
                if (Boolean.TRUE.equals(taken)) {
                    cb.onFailure(new IllegalStateException("Color already used"));
                    return;
                }
                // (3) kreiraj na Firestore → na success upiši u lokalni cache
                remote.create(userId, name, colorHex, new RepositoryCallback<Category>() {
                    @Override public void onSuccess(Category created) {
                        local.upsert(userIdLong, created);
                        cb.onSuccess(created);
                    }
                    @Override public void onFailure(Exception e) { cb.onFailure(e); }
                });
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public void update(Category category, RepositoryCallback<Void> cb) {
        // (opciono) ako je promenjena boja → check jedinstvenosti
        remote.update(category, new RepositoryCallback<Void>() {
            @Override public void onSuccess(Void ignored) {
                local.upsert(/*userId*/ category.getUserId(), category);
                cb.onSuccess(null);
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public void delete(String userId, Object categoryId, RepositoryCallback<Void> cb) {
        // pre brisanja: check da nema aktivnih taskova u toj kategoriji
        remote.hasActiveTasks(userId, categoryId, new RepositoryCallback<Boolean>() {
            @Override public void onSuccess(Boolean exists) {
                if (Boolean.TRUE.equals(exists)) {
                    cb.onFailure(new IllegalStateException("Cannot delete: active tasks exist"));
                    return;
                }
                remote.delete(userId, categoryId, new RepositoryCallback<Void>() {
                    @Override public void onSuccess(Void ignored) {
                        local.delete(userId, categoryId);
                        cb.onSuccess(null);
                    }
                    @Override public void onFailure(Exception e) { cb.onFailure(e); }
                });
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public void isColorAvailable(String userId, String colorHex, RepositoryCallback<Boolean> cb) {
        remote.isColorTaken(userId, colorHex, new RepositoryCallback<Boolean>() {
            @Override public void onSuccess(Boolean taken) { cb.onSuccess(!taken); }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

}
