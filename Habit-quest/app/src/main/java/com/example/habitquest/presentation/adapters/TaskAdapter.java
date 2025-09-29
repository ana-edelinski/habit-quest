package com.example.habitquest.presentation.adapters;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.Category;
import com.example.habitquest.domain.model.Task;

import java.util.ArrayList;
import java.util.List;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> tasks;

    public TaskAdapter(List<Task> tasks) {
        this.tasks = tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
        notifyDataSetChanged();
    }

    private List<Category> categories = new ArrayList<>();

    public void setCategories(List<Category> categories) {
        this.categories = categories != null ? categories : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OnTaskClickListener {
        void onTaskClick(Task task);
    }

    private OnTaskClickListener listener;

    public void setOnTaskClickListener(OnTaskClickListener listener) {
        this.listener = listener;
    }
    private String formatDate(long millis) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(millis));
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.task_list_item, parent, false);
        return new TaskViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task t = tasks.get(position);
        holder.tvTaskName.setText(t.getName());
        Category category = null;
        for (Category c : categories) {
            if (c.getId() != null && t.getCategoryId() != null
                   && c.getId().equals(t.getCategoryId())) {
                category = c;
                break;
            }
        }

        if (category != null) {
            holder.tvCategory.setText(category.getName());

            // oboji kružić
            Drawable circle = ContextCompat.getDrawable(holder.itemView.getContext(), R.drawable.ic_circle);
            if (circle != null) {
                circle = DrawableCompat.wrap(circle.mutate());
                try {
                    DrawableCompat.setTint(circle, Color.parseColor(category.getColorHex()));
                } catch (Exception e) {
                    DrawableCompat.setTint(circle, Color.GRAY);
                }
                holder.tvCategory.setCompoundDrawablesWithIntrinsicBounds(circle, null, null, null);
            }

        } else {
            holder.tvCategory.setText("No category");
            holder.tvCategory.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }


        if (t.getDate() != null) {
            holder.tvDateOrRepeat.setText("One-time: " + formatDate(t.getDate()));
        } else if (t.getInterval() != null && t.getUnit() != null) {
            holder.tvDateOrRepeat.setText("Every " + t.getInterval() + " " + t.getUnit());
        }

        holder.tvXp.setText("+" + t.getTotalXp() + " XP");
        holder.tvStatus.setText(
                t.getStatus() != null ? t.getStatus().name() : "ACTIVE"
        );

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTaskClick(t);
            }
        });
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvCategory, tvDateOrRepeat, tvXp, tvStatus;


        TaskViewHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDateOrRepeat = itemView.findViewById(R.id.tvDateOrRepeat);
            tvXp = itemView.findViewById(R.id.tvXp);
            tvStatus = itemView.findViewById(R.id.tvStatus);
        }
    }
}
