package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.Task;

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
        holder.tvCategory.setText(t.getCategoryId() != null ? "Category #" + t.getCategoryId() : "No category");

        if (t.getDate() != null) {
            holder.tvDateOrRepeat.setText("One-time: " + formatDate(t.getDate()));
        } else if (t.getInterval() != null && t.getUnit() != null) {
            holder.tvDateOrRepeat.setText("Every " + t.getInterval() + " " + t.getUnit());
        }

        holder.tvXp.setText("+" + t.getTotalXp() + " XP");
        holder.cbTaskStatus.setChecked(t.isCompleted());
    }

    @Override
    public int getItemCount() {
        return tasks != null ? tasks.size() : 0;
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTaskName, tvCategory, tvDateOrRepeat, tvXp;
        CheckBox cbTaskStatus;

        TaskViewHolder(View itemView) {
            super(itemView);
            tvTaskName = itemView.findViewById(R.id.tvTaskName);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDateOrRepeat = itemView.findViewById(R.id.tvDateOrRepeat);
            tvXp = itemView.findViewById(R.id.tvXp);
            cbTaskStatus = itemView.findViewById(R.id.cbTaskStatus);
        }
    }
}
