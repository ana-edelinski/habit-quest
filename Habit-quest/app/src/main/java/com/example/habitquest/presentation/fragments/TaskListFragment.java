package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.presentation.adapters.TaskPagerAdapter;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;
import com.example.habitquest.presentation.viewmodels.factories.CategoryViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.TaskViewModelFactory;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import androidx.lifecycle.ViewModelProvider;


public class TaskListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_task_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TabLayout tabLayout = view.findViewById(R.id.tabLayout);
        ViewPager2 viewPager = view.findViewById(R.id.viewPager);
        FloatingActionButton fab = view.findViewById(R.id.fabAddTask);

        TaskViewModel taskViewModel = new ViewModelProvider(requireActivity(), new TaskViewModelFactory(requireContext())).get(TaskViewModel.class);
        CategoryViewModel categoryViewModel =
                new ViewModelProvider(
                        requireActivity(),
                        new CategoryViewModelFactory(requireContext())
                ).get(CategoryViewModel.class);

        TaskPagerAdapter adapter = new TaskPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(position == 0 ? "One-time" : "Recurring")
        ).attach();

        AppPreferences prefs = new AppPreferences(requireContext());

        // 🔑 Pokreće učitavanje kategorija
        categoryViewModel.startListening();



        fab.setOnClickListener(v1 -> {
            // 🔑 Kada se klikne na FAB, koristi iste kategorije za dijalog
            ArrayList<AddTaskDialogFragment.CategoryItem> cats = new ArrayList<>();
            if (categoryViewModel.categories.getValue() != null) {
                for (Category c : categoryViewModel.categories.getValue()) {
                    cats.add(new AddTaskDialogFragment.CategoryItem(c.getId(), c.getName()));
                }
            }

            AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance(cats, null);
            dialog.setOnTaskSavedListener(new AddTaskDialogFragment.OnTaskSavedListener() {
                @Override
                public void onTaskCreated(Task task) {
                    taskViewModel.createTask(task);
                }

                @Override
                public void onTaskUpdated(Task task) {

                }
            });
            dialog.show(getParentFragmentManager(), "AddTaskDialog");
        });
    }



}
