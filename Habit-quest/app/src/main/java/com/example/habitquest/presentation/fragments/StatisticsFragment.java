package com.example.habitquest.presentation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.habitquest.R;
import com.example.habitquest.presentation.viewmodels.StatisticsViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;

    private TextView txtActiveDays, txtLongestStreak, txtMissions;
    private PieChart chartTasks;
    private BarChart chartCategories;
    private LineChart chartAvgDifficulty, chartXP7Days;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        txtActiveDays = view.findViewById(R.id.txtActiveDays);
        txtLongestStreak = view.findViewById(R.id.txtLongestStreak);
        txtMissions = view.findViewById(R.id.txtMissions);
        chartTasks = view.findViewById(R.id.chartTasks);
        chartCategories = view.findViewById(R.id.chartCategories);
        chartAvgDifficulty = view.findViewById(R.id.chartAvgDifficulty);
        chartXP7Days = view.findViewById(R.id.chartXP7Days);

        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        setupCharts();
        observeViewModel();

        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            auth.addAuthStateListener(firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() != null) {
                    String firebaseUid = firebaseAuth.getCurrentUser().getUid();
                    viewModel.loadStatistics(firebaseUid, 3L);
                }
            });
        } else {
            String firebaseUid = auth.getCurrentUser().getUid();
            viewModel.loadStatistics(firebaseUid, 3L);
        }

        return view;
    }

    private void setupCharts() {
        // --- Pie Chart (Task Status) ---
        chartTasks.getDescription().setEnabled(false);
        chartTasks.setHoleRadius(45f);
        chartTasks.setTransparentCircleRadius(50f);
        chartTasks.setUsePercentValues(false);

        // 🔹 ne prikazuj etikete unutar kolača
        chartTasks.setDrawEntryLabels(false);
        chartTasks.setDrawCenterText(true);
        chartTasks.setCenterText("Task Status");
        chartTasks.setCenterTextSize(14f);
        chartTasks.animateY(1400, Easing.EaseInOutQuad);
        chartTasks.setExtraOffsets(10, 10, 10, 20);

        // 🔹 legenda ispod
        Legend pieLegend = chartTasks.getLegend();
        pieLegend.setEnabled(true);
        pieLegend.setTextSize(12f);
        pieLegend.setForm(Legend.LegendForm.CIRCLE);
        pieLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieLegend.setDrawInside(false);
        pieLegend.setWordWrapEnabled(true);
        pieLegend.setXEntrySpace(10f);
        pieLegend.setYEntrySpace(5f);

        // --- Bar Chart ---
        chartCategories.getDescription().setEnabled(false);
        chartCategories.setFitBars(true);
        chartCategories.getAxisRight().setEnabled(false);
        chartCategories.getAxisLeft().setTextColor(Color.DKGRAY);
        chartCategories.getXAxis().setTextColor(Color.DKGRAY);
        chartCategories.getLegend().setEnabled(false);
        chartCategories.setDrawGridBackground(false);
        chartCategories.setNoDataText("No completed tasks yet");
        chartCategories.setNoDataTextColor(Color.GRAY);
        chartCategories.animateY(1200, Easing.EaseInOutQuad);
        chartCategories.setExtraOffsets(10, 10, 10, 10);
        chartCategories.setDrawValueAboveBar(true);
    }

    private void observeViewModel() {
        viewModel.getActiveDays().observe(getViewLifecycleOwner(), days ->
                txtActiveDays.setText(getString(R.string.active_streak, days)));

        viewModel.getLongestStreak().observe(getViewLifecycleOwner(), streak ->
                txtLongestStreak.setText(getString(R.string.longest_streak, streak)));

        // --- Pie Chart podaci ---
        viewModel.getTaskStatusData().observe(getViewLifecycleOwner(), pieData -> {
            if (pieData != null && pieData.getDataSet() != null) {
                PieDataSet set = (PieDataSet) pieData.getDataSet();
                set.setColors(ColorTemplate.MATERIAL_COLORS);
                set.setValueTextColor(Color.BLACK);
                set.setValueTextSize(12f);

                set.setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getPieLabel(float value, PieEntry entry) {
                        if (value < 1f) return "";
                        String label = entry.getLabel();
                        return label + " " + (int) value;
                    }
                });
            }

            chartTasks.setData(pieData);
            chartTasks.invalidate();
        });

        // --- Bar Chart podaci ---
        viewModel.getCategoryData().observe(getViewLifecycleOwner(), barData -> {
            if (barData != null && barData.getDataSetCount() > 0) {
                BarDataSet set = (BarDataSet) barData.getDataSetByIndex(0);
                set.setValueTextSize(12f);
                set.setValueTextColor(Color.DKGRAY);
                set.setBarBorderWidth(1f);
                set.setBarBorderColor(Color.LTGRAY);
                chartCategories.getLegend().setEnabled(false);
            }

            chartCategories.setData(barData);

            viewModel.getCategoryLabels().observe(getViewLifecycleOwner(), labels -> {
                if (labels != null) {
                    XAxis xAxis = chartCategories.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(labels.size());
                    xAxis.setTextSize(9f);
                    xAxis.setTextColor(Color.DKGRAY);
                    xAxis.setDrawGridLines(false);
                    xAxis.setLabelRotationAngle(0f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setYOffset(20f);
                    chartCategories.setExtraBottomOffset(45f);
                }
            });

            chartCategories.invalidate();
        });

        // --- Linijski grafikoni ---
        viewModel.getAvgDifficultyData().observe(getViewLifecycleOwner(), lineData -> {
            if (lineData != null && lineData.getDataSetCount() > 0) {
                LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);
                set.setColor(getResources().getColor(R.color.chart_orange));
                set.setCircleColor(getResources().getColor(R.color.chart_orange));
                set.setLineWidth(2f);
                set.setCircleRadius(4f);
                set.setDrawValues(false);
            }
            chartAvgDifficulty.setData(lineData);
            chartAvgDifficulty.invalidate();
        });

        viewModel.getXP7DaysData().observe(getViewLifecycleOwner(), lineData -> {
            if (lineData != null && lineData.getDataSetCount() > 0) {
                LineDataSet set = (LineDataSet) lineData.getDataSetByIndex(0);
                set.setColor(getResources().getColor(R.color.chart_green));
                set.setCircleColor(getResources().getColor(R.color.chart_green));
                set.setLineWidth(2f);
                set.setCircleRadius(4f);
                set.setDrawValues(false);
            }
            chartXP7Days.setData(lineData);
            chartXP7Days.invalidate();
        });

        viewModel.getAllianceMissionStats().observe(getViewLifecycleOwner(), missionStats -> {
            if (missionStats != null && missionStats.size() >= 2) {
                int started = missionStats.get(0);
                int finished = missionStats.get(1);
                txtMissions.setText("Special Missions: " + started + " started, " + finished + " finished");
            } else {
                txtMissions.setText("Special Missions: no data");
            }
        });

    }
}
