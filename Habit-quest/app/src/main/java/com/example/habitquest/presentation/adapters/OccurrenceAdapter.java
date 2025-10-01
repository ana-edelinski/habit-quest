package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.domain.model.TaskOccurrence;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OccurrenceAdapter extends RecyclerView.Adapter<OccurrenceAdapter.OccViewHolder> {

    private List<TaskOccurrence> items = new ArrayList<>();

    public void setItems(List<TaskOccurrence> list) {
        this.items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    public interface OccurrenceClickListener {
        void onOccurrenceClick(TaskOccurrence occ);
    }
    private OccurrenceClickListener listener;

    public void setListener(OccurrenceClickListener l) {
        this.listener = l;
    }

    @NonNull
    @Override
    public OccViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new OccViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull OccViewHolder holder, int position) {
        TaskOccurrence occ = items.get(position);
        holder.bind(occ);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onOccurrenceClick(occ);
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class OccViewHolder extends RecyclerView.ViewHolder {
        private final TextView line1, line2;

        public OccViewHolder(@NonNull View itemView) {
            super(itemView);
            line1 = itemView.findViewById(android.R.id.text1);
            line2 = itemView.findViewById(android.R.id.text2);
        }

        void bind(TaskOccurrence occ) {
            String date = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    .format(new Date(occ.getDate()));
            line1.setText(date);
            line2.setText(occ.getStatus().name());
        }
    }
}
