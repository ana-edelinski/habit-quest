package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.presentation.adapters.TaskAdapter;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecurringTasksFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_recurring_tasks, container, false);

        RecyclerView recycler = v.findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        // ViewModel init
        viewModel = new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        // posmatranje taskova
        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                List<Task> recurringTasks = new ArrayList<>();
                for (Task t : tasks) {
                    if (t.getInterval() != null && t.getUnit() != null) {
                        recurringTasks.add(t);
                    }
                }
                adapter.setTasks(recurringTasks);
            }
        });

        // povuci uid + local id iz sesije
        AppPreferences prefs = new AppPreferences(requireContext());
        String firebaseUid = prefs.getFirebaseUid();
        String localUserIdStr = prefs.getUserId();

        long localUserId = -1;
        if (localUserIdStr != null && !localUserIdStr.isEmpty()) {
            try {
                localUserId = Long.parseLong(localUserIdStr);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }

        viewModel.startListening(firebaseUid, localUserId);

        return v;
    }
}
