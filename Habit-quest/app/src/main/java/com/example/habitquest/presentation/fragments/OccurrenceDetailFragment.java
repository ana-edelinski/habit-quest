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
import com.example.habitquest.domain.model.TaskOccurrence;
import com.example.habitquest.domain.model.TaskStatus;
import com.example.habitquest.presentation.viewmodels.CategoryViewModel;
import com.example.habitquest.presentation.viewmodels.TaskViewModel;
import com.example.habitquest.presentation.viewmodels.factories.CategoryViewModelFactory;
import com.example.habitquest.presentation.viewmodels.factories.TaskViewModelFactory;
import com.example.habitquest.utils.MissionProgressHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class OccurrenceDetailFragment extends Fragment {

    private static final String ARG_OCCURRENCE = "occurrence";
    private static final String ARG_TASK = "task";

    private TaskOccurrence occurrence;
    private Task task;

    public static OccurrenceDetailFragment newInstance(TaskOccurrence occ, Task task) {
        OccurrenceDetailFragment f = new OccurrenceDetailFragment();
        Bundle b = new Bundle();
        b.putParcelable(ARG_OCCURRENCE, occ);
        b.putParcelable(ARG_TASK, task);
        f.setArguments(b);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            occurrence = getArguments().getParcelable(ARG_OCCURRENCE);
            task = getArguments().getParcelable(ARG_TASK);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_occurrence_detail, container, false);

        long now = System.currentTimeMillis();
        boolean canMarkDone = false;

        if (occurrence.getDate() != null) {
            long taskTime = occurrence.getDate();

            // prošlo vreme izvršenja
            boolean isPast = now >= taskTime;

            // razlika u danima
            long diff = now - taskTime;
            long daysDiff = diff / (1000 * 60 * 60 * 24);

            // može se kompletirati ako je prošlo vreme izvršenja i ako nije prošlo više od 3 dana
            canMarkDone = isPast && daysDiff <= 3;
        }


        TextView tvName = v.findViewById(R.id.tvTaskNameDetail);
        TextView tvCategory = v.findViewById(R.id.tvCategoryDetail);
        TextView tvDate = v.findViewById(R.id.tvTaskDate);
        TextView tvXp = v.findViewById(R.id.tvTaskXp);
        TextView tvStatus = v.findViewById(R.id.tvTaskStatus);
        TextView tvRepeat = v.findViewById(R.id.tvTaskRepeat);



        Button btnDone = v.findViewById(R.id.btnMarkDone);
        Button btnCancel = v.findViewById(R.id.btnCancel);

        if(occurrence.getStatus() != TaskStatus.ACTIVE || !canMarkDone){
            btnDone.setVisibility(View.GONE);
            btnCancel.setVisibility(View.GONE);
        }

        TaskViewModel taskViewModel =
                new ViewModelProvider(requireActivity(), new TaskViewModelFactory(requireContext()))
                        .get(TaskViewModel.class);




        // postavi vrednosti
        if (task != null && task.getInterval() != null && task.getUnit() != null) {
            tvRepeat.setText("Every " + task.getInterval() + " " + task.getUnit());
        } else {
            tvRepeat.setVisibility(View.GONE);
        }

        if (task != null) {
            tvName.setText(task.getName());
            tvXp.setText("+" + task.getTotalXp() + " XP");
        }

        if (occurrence != null) {
            String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(new Date(occurrence.getDate()));
            tvDate.setText(date);
            tvStatus.setText(occurrence.getStatus().name());
        }

        // kategorija (ako je task setovan)
        if (task != null) {
            // za ovo koristi CategoryViewModel ako ti je već vezan na Activity
            CategoryViewModel categoryViewModel =
                    new ViewModelProvider(requireActivity(), new CategoryViewModelFactory(requireContext()))
                            .get(CategoryViewModel.class);

            categoryViewModel.categories.observe(getViewLifecycleOwner(), categories -> {
                for (Category c : categories) {
                    if (c.getId().equals(task.getCategoryId())) {
                        tvCategory.setText(c.getName());
                        Drawable circle = ContextCompat.getDrawable(requireContext(), R.drawable.ic_circle);
                        if (circle != null) {
                            circle = DrawableCompat.wrap(circle.mutate());
                            DrawableCompat.setTint(circle, Color.parseColor(c.getColorHex()));
                            tvCategory.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
                        }
                        break;
                    }
                }
            });
        }



        // dugmad
        btnDone.setOnClickListener(view -> {
            if (task != null && occurrence != null) {
                taskViewModel.completeOccurrence(task, occurrence);
                if (task.getDifficultyXp() >= 4)
                    MissionProgressHelper.reportHardTask(requireActivity());
                else
                    MissionProgressHelper.reportEasyTask(requireActivity());


                int xp = task.getTotalXp(); // XP parent taska
                Toast.makeText(requireContext(),
                        "Occurrence completed! +" + xp + " XP",
                        Toast.LENGTH_SHORT).show();

                tvStatus.setText("COMPLETED");
                btnDone.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        taskViewModel.xpQuotaMessage.observe(getViewLifecycleOwner(), message -> {
            if (message != null) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
            }
        });


        btnCancel.setOnClickListener(view -> {
            if (task != null && occurrence != null) {
                taskViewModel.cancelOccurrence(task, occurrence);

                Toast.makeText(requireContext(),
                        "Occurrence canceled!",
                        Toast.LENGTH_SHORT).show();

                tvStatus.setText("CANCELED");
                btnDone.setVisibility(View.GONE);
                btnCancel.setVisibility(View.GONE);
                requireActivity().getSupportFragmentManager().popBackStack();
            }
        });

        return v;
    }
}
