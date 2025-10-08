package com.example.habitquest.presentation.viewmodels;

import android.app.Application;
import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.habitquest.R;
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
    private final MutableLiveData<List<String>> categoryLabels = new MutableLiveData<>();
    public LiveData<List<String>> getCategoryLabels() { return categoryLabels; }

    public StatisticsViewModel(@NonNull Application application) {
        super(application);
        this.taskRepo = new TaskRepository(application.getApplicationContext());
    }

    public void loadStatistics(String firebaseUid, long localUserId) {
        taskRepo.fetchAllCategories(new RepositoryCallback<Map<String, Map<String, String>>>() {
            @Override
            public void onSuccess(Map<String, Map<String, String>> categoryMap) {
                taskRepo.fetchAllForUser(firebaseUid, new RepositoryCallback<List<Task>>() {
                    @Override
                    public void onSuccess(List<Task> tasks) {
                        if (tasks == null || tasks.isEmpty()) return;

                        calculateActiveAndLongestStreak(tasks);
                        generateTaskStatusChart(tasks);
                        generateCategoryChart(tasks, categoryMap);
                        generateAvgDifficultyChart(tasks);
                        generateXP7DaysChart(tasks);
                    }

                    @Override
                    public void onFailure(Exception e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void calculateActiveAndLongestStreak(List<Task> tasks) {
        Map<LocalDate, Boolean> tasksByDay = new HashMap<>();
        Map<LocalDate, Boolean> completedByDay = new HashMap<>();

        for (Task t : tasks) {
            if (t.getDate() == null) continue;
            LocalDate date = Instant.ofEpochMilli(t.getDate())
                    .atZone(ZoneId.systemDefault()).toLocalDate();

            tasksByDay.put(date, true);
            if (t.getStatus() == TaskStatus.COMPLETED) {
                completedByDay.put(date, true);
            }
        }

        int currentStreak = 0;
        int longest = 0;
        LocalDate today = LocalDate.now();

        for (int i = 0; i < 365; i++) {
            LocalDate date = today.minusDays(i);
            if (tasksByDay.containsKey(date) && !completedByDay.containsKey(date)) {
                break;
            }
            if (completedByDay.containsKey(date)) {
                currentStreak++;
                longest = Math.max(longest, currentStreak);
            }
        }

        activeDays.postValue(currentStreak);
        longestStreak.postValue(longest);
    }

    private void generateTaskStatusChart(List<Task> tasks) {
        int created = 0, completed = 0, notDone = 0, canceled = 0;

        for (Task t : tasks) {
            if (t.getStatus() == null) continue;
            switch (t.getStatus()) {
                case ACTIVE: created++; break;
                case COMPLETED: completed++; break;
                case NOT_DONE: notDone++; break;
                case CANCELED: canceled++; break;
                default: break;
            }
        }

        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(created, "Created"));
        entries.add(new PieEntry(completed, "Done"));
        entries.add(new PieEntry(notDone, "Not Done"));
        entries.add(new PieEntry(canceled, "Canceled"));

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setValueTextSize(12f);
        dataSet.setColors(new int[]{
                getApplication().getResources().getColor(R.color.chart_blue),
                getApplication().getResources().getColor(R.color.chart_green),
                getApplication().getResources().getColor(R.color.chart_orange),
                getApplication().getResources().getColor(R.color.chart_red)
        });

        PieData pieData = new PieData(dataSet);
        taskStatusData.postValue(pieData);
    }

    private void generateCategoryChart(List<Task> tasks, Map<String, Map<String, String>> categoryMap) {
        Map<String, Integer> categoryCount = new HashMap<>();
        Map<String, String> categoryColors = new HashMap<>();

        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED) {
                String categoryName = "Other";
                String colorHex = "#9E9E9E";

                if (t.getCategoryId() != null && categoryMap.containsKey(t.getCategoryId())) {
                    Map<String, String> data = categoryMap.get(t.getCategoryId());
                    categoryName = data.get("name");
                    colorHex = data.get("color");
                }

                categoryCount.put(categoryName, categoryCount.getOrDefault(categoryName, 0) + 1);
                categoryColors.put(categoryName, colorHex);
            }
        }

        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        int i = 0;
        for (Map.Entry<String, Integer> e : categoryCount.entrySet()) {
            entries.add(new BarEntry(i++, e.getValue()));
            labels.add(e.getKey());
            try {
                colors.add(Color.parseColor(categoryColors.get(e.getKey())));
            } catch (Exception ex) {
                colors.add(Color.GRAY);
            }
        }

        BarDataSet dataSet = new BarDataSet(entries, "");
        dataSet.setValueTextSize(12f);
        dataSet.setColors(colors);
        dataSet.setDrawValues(true);
        dataSet.setValueTextColor(Color.DKGRAY);
        dataSet.setBarBorderWidth(0f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.7f);

        categoryData.postValue(barData);
        categoryLabels.postValue(labels);

    }


    private void generateAvgDifficultyChart(List<Task> tasks) {
        Map<LocalDate, List<Integer>> xpByDay = new HashMap<>();

        //zavrseni zadaci po datumu - grupisanje
        for (Task t : tasks) {
            if (t.getStatus() == TaskStatus.COMPLETED && t.getDate() != null) {
                LocalDate date = Instant.ofEpochMilli(t.getDate())
                        .atZone(ZoneId.systemDefault()).toLocalDate();
                xpByDay.computeIfAbsent(date, d -> new ArrayList<>()).add(t.getTotalXp());
            }
        }

        //prosecan xp za svaki dan
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

    public LiveData<Integer> getActiveDays() { return activeDays; }
    public LiveData<Integer> getLongestStreak() { return longestStreak; }
    public LiveData<PieData> getTaskStatusData() { return taskStatusData; }
    public LiveData<BarData> getCategoryData() { return categoryData; }
    public LiveData<LineData> getAvgDifficultyData() { return avgDifficultyData; }
    public LiveData<LineData> getXP7DaysData() { return xp7DaysData; }
}
