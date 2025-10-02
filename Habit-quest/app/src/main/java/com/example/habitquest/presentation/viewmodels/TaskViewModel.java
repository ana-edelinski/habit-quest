package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.data.repositories.UserXpLogRepository;
import com.example.habitquest.domain.model.DifficultyLevel;
import com.example.habitquest.domain.model.ImportanceLevel;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.domain.model.UserXpLog;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.utils.LevelUtils;
import com.example.habitquest.utils.ProgressPointsUtils;
import com.example.habitquest.utils.RepositoryCallback;
import com.example.habitquest.utils.XpCalculator;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class TaskViewModel extends ViewModel {

    private final AppPreferences prefs;
    private final TaskRepository repository;
    private final UserXpLogRepository userXpLogRepository;
    private final UserRepository userRepository;
    private final TaskOccurrenceRepository occurrenceRepository;

    private final MutableLiveData<List<Task>> _tasks = new MutableLiveData<>();
    public LiveData<List<Task>> tasks = _tasks;

    private final MutableLiveData<User> _user = new MutableLiveData<>();
    public LiveData<User> getUser() {
        return _user;
    }

    private final MutableLiveData<User> _levelUpEvent = new MutableLiveData<>();
    public LiveData<User> levelUpEvent = _levelUpEvent;

    private final MutableLiveData<Boolean> _taskCompleted = new MutableLiveData<>();
    public LiveData<Boolean> taskCompleted = _taskCompleted;

    private final MutableLiveData<String> _taskCompletedId = new MutableLiveData<>();
    public LiveData<String> taskCompletedId = _taskCompletedId;

    private final MutableLiveData<List<TaskOccurrence>> _occurrences = new MutableLiveData<>();
    public LiveData<List<TaskOccurrence>> occurrences = _occurrences;

    private final MutableLiveData<Task> _selectedTask = new MutableLiveData<>();
    public LiveData<Task> selectedTask = _selectedTask;

    private final MutableLiveData<TaskOccurrence> _selectedOccurrence = new MutableLiveData<>();
    public LiveData<TaskOccurrence> selectedOccurrence = _selectedOccurrence;


    private Closeable listenerHandle;

    public TaskViewModel(AppPreferences prefs,
                         TaskRepository repository,
                         UserXpLogRepository userXpLogRepository,
                         UserRepository userRepository,
                         TaskOccurrenceRepository occurrenceRepository) {
        this.prefs = prefs;
        this.repository = repository;
        this.userXpLogRepository = userXpLogRepository;
        this.userRepository = userRepository;
        this.occurrenceRepository = occurrenceRepository;
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
            public void onSuccess(Task result) {
                if(result.isRecurring()) {
                    occurrenceRepository.generateOccurrences(prefs.getFirebaseUid(), result, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            // sve ok – occurrence kreirani
                        }

                        @Override
                        public void onFailure(Exception e) {
                            // možeš logovati grešku
                        }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void updateTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();

        repository.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (task.isRecurring()) {
                    occurrenceRepository.updateOccurrences(prefs.getFirebaseUid(), task, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) { }
                        @Override
                        public void onFailure(Exception e) { }
                    });
                }
            }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void deleteTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();
        long localUserId = Long.parseLong(prefs.getUserId());

        repository.delete(firebaseUid, task.getId(), localUserId, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (task.isRecurring()) {
                    occurrenceRepository.deleteOccurrences(firebaseUid, task, new RepositoryCallback<Void>() {
                        @Override
                        public void onSuccess(Void unused) { }
                        @Override
                        public void onFailure(Exception e) { }
                    });
                }
            }
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
                grantXpForTask(task, firebaseUid, null);
                _taskCompletedId.postValue(task.getId());
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompletedId.postValue(null);
            }
        });
    }

    public void completeOccurrence(Task task, TaskOccurrence occurrence) {
        String firebaseUid = prefs.getFirebaseUid();

        occurrenceRepository.completeOccurrence(firebaseUid, task.getId(), occurrence.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                grantXpForTask(task, firebaseUid, occurrence.getId()); // XP dodela kao i za one-time
            }

            @Override
            public void onFailure(Exception e) {
                // error handling
            }
        });
    }
    public void cancelOccurrence(Task task, TaskOccurrence occurrence) {
        String firebaseUid = prefs.getFirebaseUid();

        occurrenceRepository.cancelOccurrence(firebaseUid, task.getId(), occurrence.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {

            }

            @Override
            public void onFailure(Exception e) {
                // error handling
            }
        });
    }

    public void loadOccurrencesForTask(String taskId) {
        String firebaseUid = prefs.getFirebaseUid();
        occurrenceRepository.fetchAllForTask(firebaseUid, taskId, new RepositoryCallback<List<TaskOccurrence>>() {
            @Override
            public void onSuccess(List<TaskOccurrence> result) {
                _occurrences.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                _occurrences.postValue(Collections.emptyList()); // ili null
            }
        });
    }


    private void grantXpForTask(Task task, String firebaseUid,String occurrenceId) {

        userRepository.getUser(firebaseUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                handleUserFetched(user, task, prefs.getFirebaseUid(), occurrenceId);
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompleted.postValue(false);
            }
        });
    }

    private void handleUserFetched(User user, Task task, String firebaseUid, String occurrenceId) {
        int userLevel = user.getLevel();

        int earnedXp = XpCalculator.calculateTaskXp(
                task.getDifficultyXp(),
                task.getImportanceXp(),
                userLevel
        );


        UserXpLog log = new UserXpLog(
                null,
                task.getUserId(),
                firebaseUid,
                task.getId(),
                null,
                earnedXp,
                System.currentTimeMillis()
        );

        if(task.isRecurring()) {
            log.setOccurrenceId(occurrenceId);
        }
        
        insertXpLogAndUpdateUser(log, user, task, firebaseUid);
    }

    private void insertXpLogAndUpdateUser(UserXpLog log, User user, Task task, String firebaseUid) {

        userXpLogRepository.insert(log, new RepositoryCallback<UserXpLog>() {
            @Override
            public void onSuccess(UserXpLog res) {
                fetchTotalXpAndUpdateUser(user, task, firebaseUid);
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompleted.postValue(false);
            }
        });
    }

    private void fetchTotalXpAndUpdateUser(User user, Task task, String firebaseUid) {
        userXpLogRepository.getTotalXp(task.getUserId(), prefs.getFirebaseUid(), new RepositoryCallback<Integer>() {
            @Override
            public void onSuccess(Integer totalXp) {
                updateUserXpAndCheckLevelUp(user, task, firebaseUid, totalXp);
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompleted.postValue(false);
            }
        });
    }

    private void updateUserXpAndCheckLevelUp(User user, Task task, String firebaseUid, int totalXp) {
        userRepository.updateUserXp(task.getUserId(), firebaseUid, totalXp, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void ignored) {
                checkLevelUp(user, totalXp);
                _taskCompleted.postValue(true);
            }

            @Override
            public void onFailure(Exception e) {
                _taskCompleted.postValue(true);
            }
        });
    }

    private void checkLevelUp(User user, int totalXp) {
        int newLevel = LevelUtils.calculateLevelFromXp(totalXp);

        if (newLevel > user.getLevel()) {
            int totalPp = ProgressPointsUtils.getTotalPPForLevel(newLevel);

            user.setLevel(newLevel);
            user.setPp(totalPp);
            user.setTotalXp(totalXp);

            _levelUpEvent.postValue(user);
        }
    }

    public void cancelTask(Task task) {
        String firebaseUid = prefs.getFirebaseUid();

        task.setStatus(TaskStatus.CANCELED);

        repository.update(firebaseUid, task, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // Možeš dodati LiveData event ako želiš da UI zna da je zadatak otkazan
            }

            @Override
            public void onFailure(Exception e) {
                // error handling – npr. Toast poruka u UI
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

    public void loadUser(String firebaseUid) {
        userRepository.getUser(firebaseUid, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                _user.postValue(user);
            }

            @Override
            public void onFailure(Exception e) {
                _user.postValue(null);
            }
        });
    }






    public LiveData<Task> getTaskByIdLive(String taskId) {
        MutableLiveData<Task> live = new MutableLiveData<>();
        String firebaseUid = prefs.getFirebaseUid();
        repository.getById(firebaseUid, taskId, new RepositoryCallback<Task>() {
            @Override
            public void onSuccess(Task result) {
                live.postValue(result);
            }
            @Override
            public void onFailure(Exception e) {
                live.postValue(null);
            }
        });
        return live;
    }

    public LiveData<TaskOccurrence> getOccurrenceByIdLive(String taskId, String occurrenceId) {
        MutableLiveData<TaskOccurrence> live = new MutableLiveData<>();
        String firebaseUid = prefs.getFirebaseUid();
        occurrenceRepository.getById(firebaseUid, taskId, occurrenceId, new RepositoryCallback<TaskOccurrence>() {
            @Override
            public void onSuccess(TaskOccurrence result) {
                live.postValue(result);
            }

            @Override
            public void onFailure(Exception e) {
                live.postValue(null);
            }
        });
        return live;
    }


}
