package com.example.habitquest.domain.repositoryinterfaces;

import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.utils.RepositoryCallback;

import java.util.List;

public interface ITaskOccurrenceRepository {
    void generateOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb);

    // ako se parent task edituje → obrisati stare i kreirati nove occurrence
    void updateOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb);

    // ako se parent task obriše → obrisati sve occurrence
    void deleteOccurrences(String firebaseUid, Task task, RepositoryCallback<Void> cb);

    // označi occurrence kao završen (i dodeli XP negde drugde)
    void completeOccurrence(String firebaseUid, String taskId, String occurrenceId, RepositoryCallback<Void> cb);

    // dovlači sve occurrence za jedan task (npr. za detaljan prikaz)
    void fetchAllForTask(String firebaseUid, String taskId, RepositoryCallback<List<TaskOccurrence>> cb);
}
