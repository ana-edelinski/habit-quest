package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.local.datasource.TaskLocalDataSource;
import com.example.habitquest.data.remote.TaskRemoteDataSource;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.repositoryinterfaces.ITaskRepository;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.util.List;

public class TaskRepository implements ITaskRepository {
    private final TaskLocalDataSource local;
    private final TaskRemoteDataSource remote;

    public TaskRepository(Context ctx) {
        local = new TaskLocalDataSource(ctx);
        remote = new TaskRemoteDataSource();
    }

    @Override
    public void fetchAll(String firebaseUid, long localUserId, RepositoryCallback<List<Task>> cb) {
        remote.fetchAll(firebaseUid, new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> result) {
                // 1. očisti sve stare taskove za ovog usera
                local.deleteAllByUser(localUserId);

                // 2. ubaci nove sa Firestore-a
                for (Task t : result) {
                    t.setUserId(localUserId);
                    local.upsert(t);
                }
                cb.onSuccess(result);
            }

            @Override public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public Closeable listenAll(String firebaseUid, long localUserId, TasksListener listener) {
        return remote.listenAll(firebaseUid, new TaskRemoteDataSource.RemoteListener() {
            @Override
            public void onChanged(List<Task> list) {
                // 1. očisti sve stare taskove za ovog usera
                local.deleteAllByUser(localUserId);

                // 2. ubaci nove sa Firestore-a
                for (Task t : list) {
                    if (t.getUserId() == null) {
                        t.setUserId(localUserId);
                    }
                    local.upsert(t);
                }
                listener.onChanged(list);
            }

            @Override
            public void onError(Exception e) {
                listener.onError(e);
            }
        });
    }

    @Override
    public void create(String firebaseUid, Task task, RepositoryCallback<Task> cb) {
        remote.create(firebaseUid, task, new RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                if (result.getUserId() == null) {
                    result.setUserId(task.getUserId());
                }
                local.upsert(result);
                cb.onSuccess(result);
            }

            @Override public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void update(String firebaseUid, Task task, RepositoryCallback<Void> cb) {
        remote.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                local.upsert(task);
                cb.onSuccess(null);
            }

            @Override public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void delete(String firebaseUid, String taskId, long localUserId, RepositoryCallback<Void> cb) {
        remote.delete(firebaseUid, taskId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                local.delete(taskId, localUserId);
                cb.onSuccess(null);
            }

            @Override public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    public void getById(String firebaseUid, String taskId, RepositoryCallback<Task> cb) {
        remote.getById(firebaseUid, taskId, cb);
    }

    @Override
    public void countOneTimeTasksInPeriod(String firebaseUid, long start, long end, RepositoryCallback<Integer> cb) {
        remote.countOneTimeTasksInPeriod(firebaseUid, start, end, cb);
    }
}
