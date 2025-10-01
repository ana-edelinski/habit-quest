package com.example.habitquest.presentation.viewmodels;


import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.data.repositories.CategoryRepository;
import com.example.habitquest.data.repositories.TaskOccurrenceRepository;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.CalendarTaskItem;

import java.util.*;

public class CalendarViewModel extends AndroidViewModel {

    private final TaskRepository taskRepo;
    private final CategoryRepository categoryRepo;
    private final TaskOccurrenceRepository occurrenceRepo;

    private final MutableLiveData<Map<Long, List<CalendarTaskItem>>> calendarItems = new MutableLiveData<>();

    public LiveData<Map<Long, List<CalendarTaskItem>>> getCalendarItems() {
        return calendarItems;
    }

    public CalendarViewModel(@NonNull Application application,
                             TaskRepository taskRepo,
                             CategoryRepository categoryRepo,
                             TaskOccurrenceRepository occurrenceRepo) {
        super(application);
        this.taskRepo = taskRepo;
        this.categoryRepo = categoryRepo;
        this.occurrenceRepo = occurrenceRepo;
    }

    public void loadData(String firebaseUid, long localUserId) {
        categoryRepo.getAllOnce(firebaseUid, localUserId, categories -> {
            Map<String, Category> categoryMap = new HashMap<>();
            for (Category c : categories) {
                categoryMap.put(c.getId(), c);
            }

            taskRepo.fetchAll(firebaseUid, localUserId, tasks -> {
                List<CalendarTaskItem> items = new ArrayList<>();

                for (Task t : tasks) {
                    if (!t.isRecurring()) {
                        Category cat = categoryMap.get(t.getCategoryId());
                        if (cat != null) {
                            items.add(new CalendarTaskItem(
                                    t.getId(),
                                    t.getDate(),
                                    t.getName(),
                                    cat.getColorHex(),
                                    false
                            ));
                        }
                    } else {
                        occurrenceRepo.fetchAllForTask(firebaseUid, t.getId(), occurrences -> {
                            for (TaskOccurrence occ : occurrences) {
                                Category cat = categoryMap.get(t.getCategoryId());
                                if (cat != null) {
                                    items.add(new CalendarTaskItem(
                                            occ.getId(),
                                            occ.getDate(),
                                            t.getName(),
                                            cat.getColorHex(),
                                            true
                                    ));
                                }
                            }
                            // kada su učitane occurrence, ažuriraj listu
                            calendarItems.postValue(groupByDate(items));
                        });
                    }
                }

                // odmah prikaži one-time zadatke
                calendarItems.postValue(groupByDate(items));
            });
        });
    }

    private Map<Long, List<CalendarTaskItem>> groupByDate(List<CalendarTaskItem> items) {
        Map<Long, List<CalendarTaskItem>> map = new HashMap<>();
        for (CalendarTaskItem item : items) {
            map.computeIfAbsent(item.getDate(), k -> new ArrayList<>()).add(item);
        }
        return map;
    }
}
