package com.example.habitquest.presentation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class TaskViewModel extends AndroidViewModel {

    private final TaskRepository repository;
    private final UserXpLogRepository userXpLogRepository;
    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public LiveData<List<Task>> tasks = _tasks;

    private final MutableLiveData<Boolean> _taskCompleted = new MutableLiveData<>();
    public LiveData<Boolean> taskCompleted = _taskCompleted;


    private Closeable listenerHandle;

    public TaskViewModel(@NonNull Application application) {
        super(application);
        repository = new TaskRepository(application);
        userXpLogRepository = new UserXpLogRepository(application);
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


    public void completeTask(String firebaseUid, Task task) {
        task.setStatus(TaskStatus.COMPLETED);

        repository.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // kad se task updateovao, ubaci XP log
                UserXpLog log = new UserXpLog(
                        null,                   // id (autogenerisan lokalno)
                        task.getUserId(),       // userId
                        firebaseUid,            // firebaseUid
                        task.getId(),           // taskId
                        null,                   // occurrenceId (za ponavljajuće)
                        task.getTotalXp(),      // xpGained
                        System.currentTimeMillis() // completedAt
                );
                userXpLogRepository.insert(log, new RepositoryCallback<UserXpLog>() {
                    @Override
                    public void onSuccess(UserXpLog res) {
                        // ovde možeš postaviti LiveData event "taskCompleted"
                        _taskCompleted.postValue(true);
                    }
                    @Override
                    public void onFailure(Exception e) {
                        _taskCompleted.postValue(false);
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompleted.postValue(false);
            }
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

