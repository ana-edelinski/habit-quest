package com.example.habitquest.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.domain.model.Task;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public LiveData<List<Task>> tasks = _tasks;

    private Closeable listenerHandle;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
    }

    /** Start real-time listening for tasks */
    public void startListening(String firebaseUid, long localUserId) {
        if (listenerHandle != null) {
            try { listenerHandle.close(); } catch (IOException ignored) {}
        }

        listenerHandle = repository.listenAll(firebaseUid, localUserId, new TaskRepository.TasksListener() {
            @Override
            public void onChanged(List<Task> list) {
                _tasks.postValue(list);
            }

            @Override
            public void onError(Exception e) {
                // error handling
            }
        });
    }

    public void createTask(String firebaseUid, Task task) {
        repository.create(firebaseUid, task, new com.example.habitquest.utils.RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void updateTask(String firebaseUid, Task task) {
        repository.update(firebaseUid, task, new com.example.habitquest.utils.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void deleteTask(String firebaseUid, long taskId, long localUserId) {
        repository.delete(firebaseUid, taskId, localUserId, new com.example.habitquest.utils.RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (listenerHandle != null) {
            try { listenerHandle.close(); } catch (IOException ignored) {}
        }
    }
}

