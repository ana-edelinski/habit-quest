package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.presentation.adapters.TaskAdapter;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;

import java.util.ArrayList;
import java.util.List;

public class RecurringTasksFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;
    private CategoryViewModel categoryViewModel;

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
        categoryViewModel = new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

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

        // posmatranje kategorija
        categoryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            adapter.setCategories(categories);
        });

        adapter.setOnTaskClickListener(task -> {
            Bundle args = new Bundle();
            args.putParcelable("task", task);
            NavHostFragment.findNavController(this)
                    .navigate(R.id.taskDetailFragment, args);
        });



        viewModel.startListening();

        return v;
    }
}
