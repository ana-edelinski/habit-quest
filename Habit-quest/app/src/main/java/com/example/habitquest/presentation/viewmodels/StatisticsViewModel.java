package com.example.habitquest.presentation.viewmodels;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.data.repositories.TaskRepository;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.utils.RepositoryCallback;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatisticsViewModel extends AndroidViewModel {

    private final TaskRepository taskRepo;

    private final MutableLiveData<Integer> activeDays = new MutableLiveData<>();
    private final MutableLiveData<Integer> longestStreak = new MutableLiveData<>();
    private final MutableLiveData<PieData> taskStatusData = new MutableLiveData<>();
    private final MutableLiveData<BarData> categoryData = new MutableLiveData<>();
    private final MutableLiveData<LineData> avgDifficultyData = new MutableLiveData<>();
    private final MutableLiveData<LineData> xp7DaysData = new MutableLiveData<>();

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        this.taskRepo = new TaskRepository(application.getApplicationContext());
    }

    /** Loads all tasks and prepares statistics data. */
    public void loadStatistics(String firebaseUid, long localUserId) {
        taskRepo.fetchAllForUser(firebaseUid, new RepositoryCallback<List<Task>>() {
            @Override
            public void onSuccess(List<Task> tasks) {
                Log.d("STATISTICS_DEBUG", "Fetched tasks: " + (tasks != null ? tasks.size() : -1));
                if (tasks == null || tasks.isEmpty()) {
                    Log.d("STATISTICS_DEBUG", "⚠️ No tasks returned from Firestore!");
                    return;
                }                calculateActiveAndLongestStreak(tasks);
                generateTaskStatusChart(tasks);
                generateCategoryChart(tasks);
                generateAvgDifficultyChart(tasks);
                generateXP7DaysChart(tasks);
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    /** Calculates the number of active days and the longest streak of completed tasks. */
    private void calculateActiveAndLongestStreak(List<Task> tasks) {
        Map<LocalDate, Boolean> completedByDay = new HashMap<>();

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED && t.getDate() != null) {
                LocalDate date = Instant.ofEpochMilli(t.getDate())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                completedByDay.put(date, true);
            }
        }

        int currentStreak = 0;
        int longest = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 1000; i++) {
            LocalDate date = today.minusDays(i);
            if (completedByDay.containsKey(date)) {
                currentStreak++;
                longest = Math.max(longest, currentStreak);
            } else {
                if (i == 0) continue; // don't break if there is no task today
                else break;
            }
        }

        activeDays.postValue(currentStreak);
        longestStreak.postValue(longest);
    }

    /** Generates PieChart data for task status distribution. */
    private void generateTaskStatusChart(List<Task> tasks) {
        int active = 0, paused = 0, completed = 0, canceled = 0, notDone = 0;

        for (Task t : tasks) {
            if (t.getStatus() == null) continue;
            switch (t.getStatus()) {
                case ACTIVE: active++; break;
                case PAUSED: paused++; break;
                case COMPLETED: completed++; break;
                case CANCELED: canceled++; break;
                case NOT_DONE: notDone++; break;
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(active, "Active"));
        entries.add(new PieEntry(paused, "Paused"));
        entries.add(new PieEntry(completed, "Completed"));
        entries.add(new PieEntry(canceled, "Canceled"));
        entries.add(new PieEntry(notDone, "Not done"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueTextSize(12f);
        PieData pieData = new PieData(dataSet);
        taskStatusData.postValue(pieData);
    }

    /** Generates BarChart data for completed tasks by category. */
    private void generateCategoryChart(List<Task> tasks) {
        Map<String, Integer> categoryCount = new HashMap<>();

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) {
                String category = (t.getCategoryId() != null) ? t.getCategoryId() : "Other";
                categoryCount.put(category, categoryCount.getOrDefault(category, 0) + 1);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            entries.add(new BarEntry(i++, e.getValue()));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Tasks by category");
        BarData barData = new BarData(dataSet);
        categoryData.postValue(barData);
    }

    /** Generates LineChart data for average XP (totalXp) per day. */
    private void generateAvgDifficultyChart(List<Task> tasks) {
        Map<LocalDate, List<Integer>> xpByDay = new HashMap<>();

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED && t.getDate() != null) {
                LocalDate date = Instant.ofEpochMilli(t.getDate())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                xpByDay.computeIfAbsent(date, d -> new ArrayList<>()).add(t.getTotalXp());
            }
        }

        List<Entry> entries = new ArrayList<>();
        int i = 0;
        for (Map.Entry<LocalDate, List<Integer>> e : xpByDay.entrySet()) {
            double avg = e.getValue().stream().mapToInt(Integer::intValue).average().orElse(0);
            entries.add(new Entry(i++, (float) avg));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Average XP per day");
        LineData lineData = new LineData(dataSet);
        avgDifficultyData.postValue(lineData);
    }

    /** Generates LineChart data for total XP earned in the last 7 days. */
    private void generateXP7DaysChart(List<Task> tasks) {
        LocalDate today = LocalDate.now();
        List<Entry> entries = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = today.minusDays(i);
            int totalXP = 0;

            for (Task t : tasks) {
                if (t.getStatus() == TaskStatus.COMPLETED && t.getDate() != null) {
                    LocalDate taskDate = Instant.ofEpochMilli(t.getDate())
                            .atZone(ZoneId.systemDefault()).toLocalDate();
                    if (taskDate.equals(day)) {
                        totalXP += t.getTotalXp();
                    }
                }
            }
            entries.add(new Entry(7 - i, totalXP));
        }

        LineDataSet dataSet = new LineDataSet(entries, "XP gained in last 7 days");
        LineData lineData = new LineData(dataSet);
        xp7DaysData.postValue(lineData);
    }

    // --- Getters for LiveData ---

    public LiveData<Integer> getActiveDays() { return activeDays; }
    public LiveData<Integer> getLongestStreak() { return longestStreak; }
    public LiveData<PieData> getTaskStatusData() { return taskStatusData; }
    public LiveData<BarData> getCategoryData() { return categoryData; }
    public LiveData<LineData> getAvgDifficultyData() { return avgDifficultyData; }
    public LiveData<LineData> getXP7DaysData() { return xp7DaysData; }
}
