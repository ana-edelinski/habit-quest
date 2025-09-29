package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.utils.RepositoryCallback;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;

public class TaskViewModel extends ViewModel {

    private final AppPreferences prefs;
    private final TaskRepository repository;
    private final UserXpLogRepository userXpLogRepository;

    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public LiveData<List<Task>> tasks = _tasks;

    private final MutableLiveData<Boolean> _taskCompleted = new MutableLiveData<>();
    public LiveData<Boolean> taskCompleted = _taskCompleted;

    private Closeable listenerHandle;

    public TaskViewModel(AppPreferences prefs,
                         TaskRepository repository,
                         UserXpLogRepository userXpLogRepository) {
        this.prefs = prefs;
        this.repository = repository;
        this.userXpLogRepository = userXpLogRepository;
    }

    /** Start real-time listening for tasks */
    public void startListening() {
        String firebaseUid = prefs.getFirebaseUid();
        long localUserId = Long.parseLong(prefs.getUserId());

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

    public void createTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();

        repository.create(firebaseUid, task, new RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void updateTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();

        repository.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void deleteTask(String taskId) {
        String firebaseUid = prefs.getFirebaseUid();
        long localUserId = Long.parseLong(prefs.getUserId());

        repository.delete(firebaseUid, taskId, localUserId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void completeTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();

        task.setStatus(TaskStatus.COMPLETED);

        repository.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                UserXpLog log = new UserXpLog(
                        null,                     // Firestore ID se setuje u remote
                        task.getUserId(),         // lokalni userId
                        firebaseUid,              // firebaseUid
                        task.getId(),             // taskId (Firestore ID)
                        null,                     // occurrenceId (ako ga bude)
                        task.getTotalXp(),        // xpGained
                        System.currentTimeMillis() // completedAt
                );
                userXpLogRepository.insert(log, new RepositoryCallback<UserXpLog>() {
                    @Override
                    public void onSuccess(UserXpLog res) {
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
