package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.utils.RepositoryCallback;
import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.domain.model.CalendarTaskItem;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class CalendarViewModel extends ViewModel {

    private final TaskRepository taskRepo;
    private final CategoryRepository categoryRepo;
    private final TaskOccurrenceRepository occurrenceRepo;
    private final AppPreferences prefs;

    private final MutableLiveData<Map<LocalDate, List<CalendarTaskItem>>> calendarItems =
            new MutableLiveData<>(Collections.<LocalDate, List<CalendarTaskItem>>emptyMap());

    public LiveData<Map<LocalDate, List<CalendarTaskItem>>> getCalendarItems() {
        return calendarItems;
    }

    public CalendarViewModel(AppPreferences prefs,
                             TaskRepository taskRepo,
                             CategoryRepository categoryRepo,
                             TaskOccurrenceRepository occurrenceRepo) {
        this.prefs = prefs;
        this.taskRepo = taskRepo;
        this.categoryRepo = categoryRepo;
        this.occurrenceRepo = occurrenceRepo;
    }

    public void loadData() {
        // 1) Kategorije (repo potpis: getAllOnce(String firebaseUid, Long localUserId, ...))
        categoryRepo.getAllOnce(prefs.getFirebaseUid(), Long.valueOf(prefs.getUserId()),
                new RepositoryCallback<List<Category>>() {
                    @Override
                    public void onSuccess(List<Category> categories) {
                        final Map<String, Category> categoryMap = new HashMap<>();
                        for (Category c : categories) {
                            categoryMap.put(c.getId(), c);
                        }

                        // 2) Taskovi (repo potpis: fetchAll(String firebaseUid, long localUserId, ...))
                        taskRepo.fetchAll(prefs.getFirebaseUid(), Long.parseLong(prefs.getUserId()),
                                new RepositoryCallback<List<Task>>() {
                                    @Override
                                    public void onSuccess(List<Task> tasks) {
                                        final List<CalendarTaskItem> allItems =
                                                Collections.synchronizedList(new ArrayList<>());

                                        final List<Task> recurringTasks = new ArrayList<>();

                                        // One-time odmah
                                        for (Task t : tasks) {
                                            if (!t.isRecurring()) {
                                                Category cat = categoryMap.get(t.getCategoryId());
                                                allItems.add(CalendarTaskItem.fromTask(t, cat));
                                            } else {
                                                recurringTasks.add(t);
                                            }
                                        }

                                        // rano stanje (da UI ne bude prazan)
                                        calendarItems.postValue(groupByDate(allItems));

                                        if (recurringTasks.isEmpty()) return;

                                        // 3) Occurrence po svakom recurring tasku
                                        final AtomicInteger pending = new AtomicInteger(recurringTasks.size());

                                        for (Task rt : recurringTasks) {
                                            final Task taskRef = rt;
                                            occurrenceRepo.fetchAllForTask(
                                                    prefs.getFirebaseUid(),
                                                    taskRef.getId(),
                                                    new RepositoryCallback<List<TaskOccurrence>>() {
                                                        @Override
                                                        public void onSuccess(List<TaskOccurrence> occurrences) {
                                                            Category cat = categoryMap.get(taskRef.getCategoryId());
                                                            for (TaskOccurrence occ : occurrences) {
                                                                allItems.add(CalendarTaskItem.fromOccurrence(occ, taskRef, cat));
                                                            }
                                                            if (pending.decrementAndGet() == 0) {
                                                                calendarItems.postValue(groupByDate(allItems));
                                                            }
                                                        }

                                                        @Override
                                                        public void onFailure(Exception e) {
                                                            if (pending.decrementAndGet() == 0) {
                                                                calendarItems.postValue(groupByDate(allItems));
                                                            }
                                                        }
                                                    }
                                            );
                                        }
                                    }

                                    @Override
                                    public void onFailure(Exception e) {
                                        calendarItems.postValue(Collections.<LocalDate, List<CalendarTaskItem>>emptyMap());
                                    }
                                });
                    }

                    @Override
                    public void onFailure(Exception e) {
                        calendarItems.postValue(Collections.<LocalDate, List<CalendarTaskItem>>emptyMap());
                    }
                });
    }

    // Helper: lista za konkretan dan (za RecyclerView ispod kalendara)
    public List<CalendarTaskItem> getItemsFor(LocalDate date) {
        Map<LocalDate, List<CalendarTaskItem>> map = calendarItems.getValue();
        if (map == null) return Collections.emptyList();
        List<CalendarTaskItem> list = map.get(date);
        return list != null ? list : Collections.<CalendarTaskItem>emptyList();
    }

    private Map<LocalDate, List<CalendarTaskItem>> groupByDate(List<CalendarTaskItem> items) {
        Map<LocalDate, List<CalendarTaskItem>> map = new HashMap<>();
        for (CalendarTaskItem item : items) {
            LocalDate d = Instant.ofEpochMilli(item.getDate())
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            map.computeIfAbsent(d, k -> new ArrayList<>()).add(item);
        }
        return map;
    }
}
