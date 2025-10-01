package com.example.habitquest.data.repositories;

import android.content.Context;
import android.util.Log;

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
    public void getAllOnce(String firebaseUid, Long localUserId, RepositoryCallback<List<Category>> cb) {
        remote.fetchAll(firebaseUid, new RepositoryCallback<List<Category>>() {
            @Override public void onSuccess(List<Category> remoteList) {
                local.replaceAll(localUserId, remoteList); // cache u SQLite
                cb.onSuccess(remoteList);
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public Closeable listenAll(String firebaseUid, Long localUserId, CategoriesListener listener) {
        return remote.listenAll(firebaseUid, new CategoryRemoteDataSource.RemoteListener() {
            @Override public void onChanged(List<Category> list) {

                local.replaceAll(localUserId, list);

                listener.onChanged(list);
            }
            @Override public void onError(Exception e) { listener.onError(e); }
        });
    }

    @Override
    public void create(String firebaseUid, Long localUserId, String name, String colorHex, RepositoryCallback<Category> cb) {


        remote.isColorTaken(firebaseUid, colorHex, new RepositoryCallback<Boolean>() {
            @Override public void onSuccess(Boolean taken) {
                if (Boolean.TRUE.equals(taken)) {
                    cb.onFailure(new IllegalStateException("Color already used"));
                    return;
                }
                remote.create(firebaseUid, name, colorHex, new RepositoryCallback<Category>() {
                    @Override public void onSuccess(Category created) {
                        local.upsert(localUserId, created);
                        cb.onSuccess(created);
                    }
                    @Override public void onFailure(Exception e) { cb.onFailure(e); }
                });
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public void update(String firebaseUid, Category category, RepositoryCallback<Void> cb) {
        remote.update(firebaseUid, category, new RepositoryCallback<Void>() {
            @Override public void onSuccess(Void ignored) {
                local.upsert(category.getUserId(), category);
                cb.onSuccess(null);
            }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }

    @Override
    public void delete(String firebaseUid, Long localUserId, String categoryId, RepositoryCallback<Void> cb) {
        remote.hasActiveTasks(firebaseUid, categoryId, new RepositoryCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean exists) {
                if (Boolean.TRUE.equals(exists)) {
                    cb.onFailure(new IllegalStateException("Cannot delete category: active tasks exist"));
                    return;
                }

                remote.delete(firebaseUid, categoryId, new RepositoryCallback<Void>() {
                    @Override
                    public void onSuccess(Void ignored) {
                        local.delete(localUserId, categoryId);
                        cb.onSuccess(null);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        cb.onFailure(e);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(new Exception("Failed to check active tasks", e));
            }
        });
    }


    @Override
    public void isColorAvailable(String firebaseUid, String colorHex, RepositoryCallback<Boolean> cb) {
        remote.isColorTaken(firebaseUid, colorHex, new RepositoryCallback<Boolean>() {
            @Override public void onSuccess(Boolean taken) { cb.onSuccess(!taken); }
            @Override public void onFailure(Exception e) { cb.onFailure(e); }
        });
    }



}
