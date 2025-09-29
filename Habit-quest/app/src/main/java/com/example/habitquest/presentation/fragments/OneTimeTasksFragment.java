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
import com.example.habitquest.presentation.viewmodels.factories.CategoryViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.TaskViewModelFactory;

import java.util.ArrayList;
import java.util.List;

public class OneTimeTasksFragment extends Fragment {

    private TaskAdapter adapter;
    private TaskViewModel viewModel;
    private CategoryViewModel categoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_one_time_tasks, container, false);

        RecyclerView recycler = v.findViewById(R.id.recyclerTasks);
        recycler.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(new ArrayList<>());
        recycler.setAdapter(adapter);

        // ViewModel init
        viewModel = new ViewModelProvider(requireActivity(), new TaskViewModelFactory(requireContext())).get(TaskViewModel.class);
        CategoryViewModel categoryViewModel =
                new ViewModelProvider(
                        requireActivity(),
                        new CategoryViewModelFactory(requireContext())
                ).get(CategoryViewModel.class);

        // posmatranje taskova
        viewModel.tasks.observe(getViewLifecycleOwner(), tasks -> {
            if (tasks != null) {
                // filtriraj samo one-time taskove (koji imaju `date` postavljen, a nemaju interval)
                List<Task> oneTimeTasks = new ArrayList<>();
                for (Task t : tasks) {
                    if (t.getDate() != null && t.getInterval() == null) {
                        oneTimeTasks.add(t);
                    }
                }
                adapter.setTasks(oneTimeTasks);
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
