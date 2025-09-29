package com.example.habitquest.presentation.fragments;


import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.domain.model.DifficultyLevel;
import com.example.habitquest.domain.model.ImportanceLevel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddTaskDialogFragment extends DialogFragment {

    public static class CategoryItem implements Serializable {
        public String id; public String name;
        public CategoryItem(String id, String name){ this.id=id; this.name=name; }
        @Override public String toString(){ return name; }
    }

    public interface OnTaskCreatedListener { void onTaskCreated(Task task); }

    private static final String ARG_CATEGORIES = "ARG_CATEGORIES";
    private OnTaskCreatedListener listener;

    public static AddTaskDialogFragment newInstance(ArrayList<CategoryItem> categories) {
        AddTaskDialogFragment f = new AddTaskDialogFragment();
        Bundle b = new Bundle();
        b.putSerializable(ARG_CATEGORIES, categories);
        f.setArguments(b);
        return f;
    }
    public void setOnTaskCreatedListener(OnTaskCreatedListener l){ this.listener = l; }

    // UI
    private EditText etName, etDescription, etInterval;
    private Spinner spCategory, spUnit, spDifficulty, spImportance;
    private RadioGroup rgType;
    private LinearLayout boxOneTime, boxRecurring;
    private Button btnPickDate, btnPickTime, btnPickStartDate, btnPickStartTime, btnPickEndDate, btnPickEndTime;

    // state
    private final Calendar oneTimeCal = Calendar.getInstance();
    private final Calendar startCal = Calendar.getInstance();
    private final Calendar endCal = Calendar.getInstance();

    @NonNull @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View v = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_task, null);

        // bind
        etName = v.findViewById(R.id.etName);
        etDescription = v.findViewById(R.id.etDescription);
        spCategory = v.findViewById(R.id.spCategory);
        rgType = v.findViewById(R.id.rgType);
        boxOneTime = v.findViewById(R.id.boxOneTime);
        boxRecurring = v.findViewById(R.id.boxRecurring);
        btnPickDate = v.findViewById(R.id.btnPickDate);
        btnPickTime = v.findViewById(R.id.btnPickTime);
        btnPickStartDate = v.findViewById(R.id.btnPickStartDate);
        btnPickStartTime = v.findViewById(R.id.btnPickStartTime);
        btnPickEndDate = v.findViewById(R.id.btnPickEndDate);
        btnPickEndTime = v.findViewById(R.id.btnPickEndTime);
        etInterval = v.findViewById(R.id.etInterval);
        spUnit = v.findViewById(R.id.spUnit);
        spDifficulty = v.findViewById(R.id.spDifficulty);
        spImportance = v.findViewById(R.id.spImportance);

        // categories
        ArrayList<CategoryItem> categories = (ArrayList<CategoryItem>) getArguments().getSerializable(ARG_CATEGORIES);
        ArrayAdapter<CategoryItem> catAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_item, categories);
        catAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spCategory.setAdapter(catAdapter);

        // unit
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item,
                new String[]{"day","week","month","year"});
        unitAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spUnit.setAdapter(unitAdapter);

        // difficulty & importance
        ArrayAdapter<DifficultyLevel> diffAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, DifficultyLevel.values());
        diffAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDifficulty.setAdapter(diffAdapter);

        ArrayAdapter<ImportanceLevel> impAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, ImportanceLevel.values());
        impAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spImportance.setAdapter(impAdapter);

        // toggle one-time / recurring
        rgType.setOnCheckedChangeListener((group, checkedId) -> {
            boolean isOne = checkedId == R.id.rbOneTime;
            boxOneTime.setVisibility(isOne ? View.VISIBLE : View.GONE);
            boxRecurring.setVisibility(isOne ? View.GONE : View.VISIBLE);
        });

        // pickers
        btnPickDate.setOnClickListener(v1 -> showDatePicker(oneTimeCal, btnPickDate));
        btnPickTime.setOnClickListener(v12 -> showTimePicker(oneTimeCal, btnPickTime));
        btnPickStartDate.setOnClickListener(v13 -> showDatePicker(startCal, btnPickStartDate));
        btnPickStartTime.setOnClickListener(v14 -> showTimePicker(startCal, btnPickStartTime));
        btnPickEndDate.setOnClickListener(v15 -> showDatePicker(endCal, btnPickEndDate));
        btnPickEndTime.setOnClickListener(v16 -> showTimePicker(endCal, btnPickEndTime));

        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setTitle("Add new task")
                .setView(v)
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .setPositiveButton("Create", (d, w) -> {
                    Task t = buildTaskFromInputs(categories);
                    if (t != null && listener != null) listener.onTaskCreated(t);
                });

        return b.create();
    }

    private void showDatePicker(Calendar cal, Button target) {
        new DatePickerDialog(requireContext(),
                (view, y, m, day) -> { cal.set(Calendar.YEAR,y); cal.set(Calendar.MONTH,m); cal.set(Calendar.DAY_OF_MONTH,day);
                    target.setText(String.format("%02d.%02d.%04d", day, m+1, y));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showTimePicker(Calendar cal, Button target) {
        new TimePickerDialog(requireContext(),
                (view, h, min) -> { cal.set(Calendar.HOUR_OF_DAY,h); cal.set(Calendar.MINUTE,min); cal.set(Calendar.SECOND,0);
                    target.setText(String.format("%02d:%02d", h, min));
                },
                cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show();
    }

    @Nullable
    private Task buildTaskFromInputs(List<CategoryItem> categories) {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) { Toast.makeText(getContext(),"Name is required",Toast.LENGTH_SHORT).show(); return null; }
        CategoryItem sel = (CategoryItem) spCategory.getSelectedItem();
        if (sel == null) { Toast.makeText(getContext(),"Pick a category",Toast.LENGTH_SHORT).show(); return null; }

        boolean isOneTime = ((RadioButton) requireDialog().findViewById(R.id.rbOneTime)).isChecked();

        DifficultyLevel diff = (DifficultyLevel) spDifficulty.getSelectedItem();
        ImportanceLevel imp = (ImportanceLevel) spImportance.getSelectedItem();

        Task t = new Task();
        t.setName(name);
        t.setDescription(etDescription.getText().toString().trim());
        t.setCategoryId(sel.id);
        t.setDifficultyXp(diff.xp);
        t.setImportanceXp(imp.xp);
        t.setTotalXp(diff.xp + imp.xp);
        t.setStatus(TaskStatus.ACTIVE); // default

        if (isOneTime) {
            // koristi postojeća polja: date (epoch millis)
            t.setDate(oneTimeCal.getTimeInMillis());
            t.setStartDate(null);
            t.setEndDate(null);
            t.setInterval(null);
            t.setUnit(null);
        } else {
            // recurring
            String intervalStr = etInterval.getText().toString().trim();
            if (intervalStr.isEmpty()) { Toast.makeText(getContext(),"Interval is required",Toast.LENGTH_SHORT).show(); return null; }
            int interval = Integer.parseInt(intervalStr);
            String unit = (String) spUnit.getSelectedItem();

            long startMs = startCal.getTimeInMillis();
            long endMs = endCal.getTimeInMillis();
            if (endMs <= startMs) { Toast.makeText(getContext(),"End must be after start",Toast.LENGTH_SHORT).show(); return null; }

            t.setDate(null);
            t.setStartDate(startMs);
            t.setEndDate(endMs);
            t.setInterval(interval);
            t.setUnit(unit);
        }
        return t;
    }
}

