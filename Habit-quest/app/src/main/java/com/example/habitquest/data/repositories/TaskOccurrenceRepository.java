package com.example.habitquest.data.repositories;

import android.content.Context;

import com.example.habitquest.data.remote.TaskOccurrenceRemoteDataSource;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.domain.repositoryinterfaces.ITaskOccurrenceRepository;
import com.example.habitquest.utils.OccurrenceHelper;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public class TaskOccurrenceRepository implements ITaskOccurrenceRepository {

    private final TaskOccurrenceRemoteDataSource remote;
    // ⚠️ Opciono kasnije: private final TaskOccurrenceLocalDataSource local;

    public TaskOccurrenceRepository(Context context) {
        this.remote = new TaskOccurrenceRemoteDataSource();
        // this.local = local;
    }

    @Override
    public void generateOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb) {
        // 🔹 Logika: generiši listu occurrence na osnovu kostura
        List<TaskOccurrence> occurrences = OccurrenceHelper.generateOccurrences(task);

        // 🔹 Sačuvaj u remote
        remote.saveOccurrences(firebaseUid, task.getId(), occurrences, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // ⚠️ Kasnije: ovde možeš sačuvati i lokalno
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void updateOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb) {
        // 🔹 Najjednostavnije: obriši stare pa generiši nove
        remote.deleteAllForTask(firebaseUid, task.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                generateOccurrences(firebaseUid, task, cb);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void deleteOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb) {
        remote.deleteAllForTask(firebaseUid, task.getId(), new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // ⚠️ Kasnije: obriši i lokalno
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    @Override
    public void completeOccurrence(String firebaseUid, String taskId, String occurrenceId, RepositoryCallback<Void> cb) {
        remote.updateOccurrenceStatus(firebaseUid, taskId, occurrenceId, TaskStatus.COMPLETED, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // ⚠️ Kasnije: update i u lokalnoj bazi
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }
    @Override
    public void cancelOccurrence(String firebaseUid, String taskId, String occurrenceId, RepositoryCallback<Void> cb) {
        remote.updateOccurrenceStatus(firebaseUid, taskId, occurrenceId, TaskStatus.CANCELED, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                // ⚠️ Kasnije: update i u lokalnoj bazi
                cb.onSuccess(null);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    public void updateOccurrenceStatus(String firebaseUid, String taskId, String occurrenceId, TaskStatus status, RepositoryCallback<Void> cb) {
        remote.updateOccurrenceStatus(firebaseUid, taskId, occurrenceId, status, cb);
    }

    @Override
    public void fetchAllForTask(String firebaseUid, String taskId, RepositoryCallback<List<TaskOccurrence>> cb) {
        remote.getOccurrencesForTask(firebaseUid, taskId, new RepositoryCallback<List<TaskOccurrence>>() {
            @Override
            public void onSuccess(List<TaskOccurrence> result) {
                // ⚠️ Kasnije: osveži lokalnu bazu
                cb.onSuccess(result);
            }

            @Override
            public void onFailure(Exception e) {
                cb.onFailure(e);
            }
        });
    }

    public void getById(String firebaseUid, String taskId, String occurrenceId, RepositoryCallback<TaskOccurrence> cb) {
        remote.getById(firebaseUid, taskId, occurrenceId, cb);
    }

    @Override
    public void countOccurrencesInPeriod(String firebaseUid, long start, long end, RepositoryCallback<Integer> cb) {
        remote.countOccurrencesInPeriod(firebaseUid, start, end, cb);
    }



}
