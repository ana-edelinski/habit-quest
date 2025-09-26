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
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;

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

        AppPreferences prefs = new AppPreferences(requireContext());
        String firebaseUid = prefs.getFirebaseUid();
        String localUserId = prefs.getUserId();

        TextView tvName = v.findViewById(R.id.tvTaskNameDetail);
        TextView tvDescription = v.findViewById(R.id.tvTaskDescription);
        TextView tvDate = v.findViewById(R.id.tvTaskDate);
        TextView tvXp = v.findViewById(R.id.tvTaskXp);
        TextView tvStatus = v.findViewById(R.id.tvTaskStatus);

        TaskViewModel taskViewModel =
                new ViewModelProvider(requireActivity()).get(TaskViewModel.class);

        CategoryViewModel categoryViewModel =
                new ViewModelProvider(requireActivity()).get(CategoryViewModel.class);

        categoryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
            for (Category c : categories) {
                if (c.getId() != null && task.getCategoryId() != null &&
                        c.getId().longValue() == task.getCategoryId().longValue()) {

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


        btnDone.setOnClickListener(view1 -> {
            taskViewModel.completeTask(firebaseUid, task);
        });

        taskViewModel.taskCompleted.observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                Toast.makeText(requireContext(), "Task completed! +" + task.getTotalXp() + " XP", Toast.LENGTH_SHORT).show();
                btnDone.setVisibility(View.GONE);
            }
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
                tvDate.setText("One-time: " + task.getDate());
            } else if (task.getInterval() != null) {
                tvDate.setText("Every " + task.getInterval() + " " + task.getUnit());
            }
        }


        return v;
    }
}
