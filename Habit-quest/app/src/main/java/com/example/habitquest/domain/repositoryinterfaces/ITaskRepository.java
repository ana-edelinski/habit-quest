package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.util.List;

public interface ITaskRepository {
    void fetchAll(String firebaseUid, long localUserId, RepositoryCallback<List<Task>> cb);
    Closeable listenAll(String firebaseUid, long localUserId, TasksListener listener);

    void create(String firebaseUid, Task task, RepositoryCallback<Task> cb);
    void update(String firebaseUid, Task task, RepositoryCallback<Void> cb);
    void delete(String firebaseUid, String taskId, long localUserId, RepositoryCallback<Void> cb);

    interface TasksListener {
        void onChanged(List<Task> list);
        void onError(Exception e);
    }
}
