package com.example.habitquest.presentation.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class StatisticsFragment extends Fragment {

    private StatisticsViewModel viewModel;

    private TextView txtActiveDays, txtLongestStreak;
    private PieChart chartTasks;
    private BarChart chartCategories;
    private LineChart chartAvgDifficulty, chartXP7Days;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_statistics, container, false);

        // --- Initialize UI elements ---
        txtActiveDays = view.findViewById(R.id.txtActiveDays);
        txtLongestStreak = view.findViewById(R.id.txtLongestStreak);
        chartTasks = view.findViewById(R.id.chartTasks);
        chartCategories = view.findViewById(R.id.chartCategories);
        chartAvgDifficulty = view.findViewById(R.id.chartAvgDifficulty);
        chartXP7Days = view.findViewById(R.id.chartXP7Days);

        // --- ViewModel ---
        viewModel = new ViewModelProvider(this).get(StatisticsViewModel.class);

        setupCharts();
        observeViewModel();

        // --- Load real user data ---
        FirebaseAuth auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Log.w("STATISTICS", "User not available yet — waiting for FirebaseAuth…");
            auth.addAuthStateListener(firebaseAuth -> {
                if (firebaseAuth.getCurrentUser() != null) {
                    String firebaseUid = firebaseAuth.getCurrentUser().getUid();
                    Log.d("STATISTICS", "✅ User loaded: " + firebaseUid);
                    viewModel.loadStatistics(firebaseUid, 3L);
                } else {
                    Log.e("STATISTICS", "❌ AuthStateListener triggered, but user is still null");
                }
            });
        } else {
            String firebaseUid = auth.getCurrentUser().getUid();
            Log.d("STATISTICS", "✅ User already available: " + firebaseUid);
            viewModel.loadStatistics(firebaseUid, 3L);
        }

        return view;
    }

    /** Configure chart properties for consistent styling and appearance. */
    private void setupCharts() {
        // --- Pie Chart (Task Status) ---
        chartTasks.getDescription().setEnabled(false);
        chartTasks.setHoleRadius(45f);
        chartTasks.setTransparentCircleRadius(50f);
        chartTasks.setUsePercentValues(true);
        chartTasks.setEntryLabelTextSize(12f);
        chartTasks.setEntryLabelColor(Color.BLACK);
        chartTasks.setDrawEntryLabels(true);
        chartTasks.setDrawCenterText(true);
        chartTasks.setCenterText("Task Status");
        chartTasks.setCenterTextSize(14f);
        chartTasks.animateY(1400, Easing.EaseInOutQuad);

        Legend pieLegend = chartTasks.getLegend();
        pieLegend.setEnabled(true);
        pieLegend.setTextSize(12f);
        pieLegend.setForm(Legend.LegendForm.CIRCLE);
        pieLegend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        pieLegend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        pieLegend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        pieLegend.setDrawInside(false);

        // --- Bar Chart (Tasks by Category) ---
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

    /** Observe LiveData from the ViewModel and update UI reactively. */
    private void observeViewModel() {
        // Active days
        viewModel.getActiveDays().observe(getViewLifecycleOwner(), days ->
                txtActiveDays.setText("Active streak: " + days + " days"));

        // Longest streak
        viewModel.getLongestStreak().observe(getViewLifecycleOwner(), streak ->
                txtLongestStreak.setText("Longest streak: " + streak + " days"));

        // --- Task status pie chart ---
        viewModel.getTaskStatusData().observe(getViewLifecycleOwner(), pieData -> {
            if (pieData != null && pieData.getDataSet() != null) {
                PieDataSet set = (PieDataSet) pieData.getDataSet();
                set.setColors(ColorTemplate.MATERIAL_COLORS);
                set.setValueTextColor(Color.BLACK);
                set.setValueTextSize(12f);
            }
            chartTasks.setData(pieData);
            chartTasks.invalidate();
        });

        // --- Tasks by category bar chart ---
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

            // ✅ Nazivi kategorija ispod stubova (bez lambdi)
            viewModel.getCategoryLabels().observe(getViewLifecycleOwner(), labels -> {
                if (labels != null) {
                    XAxis xAxis = chartCategories.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(labels.size());
                    xAxis.setTextSize(12f);
                    xAxis.setTextColor(Color.DKGRAY);
                    xAxis.setDrawGridLines(false);
                    xAxis.setLabelRotationAngle(-25f);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                }
            });

            chartCategories.invalidate();
        });

        // --- Average XP per day line chart ---
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

        // --- XP earned in last 7 days line chart ---
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
    }
}
