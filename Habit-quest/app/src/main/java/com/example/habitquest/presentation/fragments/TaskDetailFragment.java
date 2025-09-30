package com.example.habitquest.presentation.fragments;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;
import com.example.habitquest.presentation.viewmodels.factories.CategoryViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.TaskViewModelFactory;
import androidx.appcompat.app.AlertDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskDetailFragment extends Fragment {

    private static final String ARG_TASK = "task";

    private Task task;

    public static TaskDetailFragment newInstance(Task task) {
        TaskDetailFragment fragment = new TaskDetailFragment();
        Bundle args = new Bundle();
        args.putParcelable(ARG_TASK, task); // Task treba da implementira Serializable ili Parcelable
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            task = (Task) getArguments().getParcelable(ARG_TASK);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_task_detail, container, false);


        TextView tvName = v.findViewById(R.id.tvTaskNameDetail);
        TextView tvDescription = v.findViewById(R.id.tvTaskDescription);
        TextView tvDate = v.findViewById(R.id.tvTaskDate);
        TextView tvXp = v.findViewById(R.id.tvTaskXp);
        TextView tvStatus = v.findViewById(R.id.tvTaskStatus);

        TaskViewModel taskViewModel =
                new ViewModelProvider(requireActivity(), new TaskViewModelFactory(requireContext())).get(TaskViewModel.class);

        CategoryViewModel categoryViewModel =
                new ViewModelProvider(
                        requireActivity(),
                        new CategoryViewModelFactory(requireContext())
                ).get(CategoryViewModel.class);

        categoryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            for (Category c : categories) {
                if (c.getId() != null && task.getCategoryId() != null &&
                        c.getId().equals(task.getCategoryId())) {

                    TextView tvCategory = v.findViewById(R.id.tvCategoryDetail);
                    tvCategory.setText(c.getName());

                    // oboji kružić
                    Drawable circle = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle);
                    if (circle != null) {
                        circle = DrawableCompat.wrap(circle.mutate());
                        try {
                            DrawableCompat.setTint(circle, Color.parseColor(c.getColorHex()));
                        } catch (Exception e) {
                            DrawableCompat.setTint(circle, Color.GRAY);
                        }
                        tvCategory.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
                    }
                    break;
                }
            }
        });



        Button btnDone = v.findViewById(R.id.btnMarkDone);
        Button btnCancel = v.findViewById(R.id.btnCancel);
        Button btnPause = v.findViewById(R.id.btnPause);
        Button btnEdit = v.findViewById(R.id.btnEdit);
        Button btnDelete = v.findViewById(R.id.btnDelete);

        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.NOT_DONE) {
            btnDone.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        } else {
            btnDone.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
            btnEdit.setVisibility(View.VISIBLE);
            btnCancel.setVisibility(View.VISIBLE);
        }

        //kompletiranje zadatka
        btnDone.setOnClickListener(view1 -> {
            taskViewModel.completeTask(task);
            btnDone.setVisibility(View.GONE);
        });

        taskViewModel.taskCompleted.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Task completed! +" + task.getTotalXp() + " XP", Toast.LENGTH_SHORT).show();

            }
        });

        //otkazivanje zadatka
        btnCancel.setOnClickListener(view1 -> {
            taskViewModel.cancelTask(task);

            // Sakrij sva dugmad jer je task sada otkazan
            btnDone.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
            btnPause.setVisibility(View.GONE);
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);

            Toast.makeText(requireContext(), "Task canceled", Toast.LENGTH_SHORT).show();
        });

        //brisanje zadatka
        btnDelete.setOnClickListener(view1 -> {
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Task")
                    .setMessage("Are you sure you want to delete this task?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        taskViewModel.deleteTask(task.getId());
                        Toast.makeText(requireContext(), "Task successfully deleted", Toast.LENGTH_SHORT).show();
                        requireActivity().getSupportFragmentManager().popBackStack(); // vrati se na listu
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        });

        //edit zadatka
        btnEdit.setOnClickListener(view1 -> {
            // 1. Pretvori kategorije u CategoryItem listu
            ArrayList<AddTaskDialogFragment.CategoryItem> catItems = new ArrayList<>();
            List<Category> currentCats = categoryViewModel.categories.getValue();
            if (currentCats != null) {
                for (Category c : currentCats) {
                    catItems.add(new AddTaskDialogFragment.CategoryItem(c.getId(), c.getName()));
                }
            }

            // 2. Otvori dijalog u "edit mode" (prosleđuješ i task)
            AddTaskDialogFragment dialog = AddTaskDialogFragment.newInstance(catItems, task);
            dialog.setOnTaskSavedListener(new AddTaskDialogFragment.OnTaskSavedListener() {
                @Override
                public void onTaskCreated(Task task) {
                }

                @Override
                public void onTaskUpdated(Task updatedTask) {
                    taskViewModel.updateTask(updatedTask);
                    Toast.makeText(requireContext(), "Task updated", Toast.LENGTH_SHORT).show();

                    // osveži UI odmah
                    tvName.setText(updatedTask.getName());
                    tvDescription.setText(updatedTask.getDescription());
                    tvXp.setText("+" + updatedTask.getTotalXp() + " XP");
                    tvStatus.setText(updatedTask.getStatus().name());
                    if (updatedTask.getDate() != null) {
                        tvDate.setText("One-time: " + formatDateTime(updatedTask.getDate()));
                    } else if (updatedTask.getInterval() != null) {
                        tvDate.setText("Every " + updatedTask.getInterval() + " " + updatedTask.getUnit());
                    }
                }
            });
            dialog.show(getParentFragmentManager(), "editTaskDialog");
        });








        if (task.getInterval() != null && task.getUnit() != null) {
            btnPause.setVisibility(View.VISIBLE);
        } else {
            btnPause.setVisibility(View.GONE);
        }

        if (task != null) {
            tvName.setText(task.getName());
            tvDescription.setText(task.getDescription());
            tvXp.setText("+" + task.getTotalXp() + " XP");
            tvStatus.setText(task.getStatus().name());

            if (task.getDate() != null) {
                tvDate.setText("One-time: " + formatDateTime(task.getDate()));
            } else if (task.getInterval() != null) {
                tvDate.setText("Every " + task.getInterval() + " " + task.getUnit());
            }
        }


        return v;
    }

    private String formatDateTime(Long millis) {
        if (millis == null) return "";
        Date date = new Date(millis);
        SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }
}
