package com.example.habitquest.presentation.adapters;


import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.domain.model.CalendarTaskItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CalendarItemsAdapter extends RecyclerView.Adapter<CalendarItemsAdapter.VH> {

    public interface OnCalendarItemClickListener {
        void onCalendarItemClick(CalendarTaskItem item);
    }

    private final List<CalendarTaskItem> data = new ArrayList<>();
    private final OnCalendarItemClickListener listener;

    public CalendarItemsAdapter(OnCalendarItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<CalendarTaskItem> items) {
        data.clear();
        if (items != null) data.addAll(items);
        notifyDataSetChanged();
    }

    @NonNull @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        CalendarTaskItem it = data.get(position);
        h.title.setText(it.getTitle());
        h.meta.setText(it.isOccurrence() ? "Recurring task" : "One-time task");

        // mala tačka levo od naslova, obojena po kategoriji
        h.title.setCompoundDrawablePadding(dp(h.title.getContext(), 6));
        h.title.setCompoundDrawablesWithIntrinsicBounds(
                makeDot(h.title.getContext(), it.getCategoryColor(), 10), // 10dp
                null, null, null
        );

        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onCalendarItemClick(it);
        });
    }

    @Override
    public int getItemCount() { return data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final TextView title, meta;
        VH(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(android.R.id.text1);
            meta  = itemView.findViewById(android.R.id.text2);
        }
    }

    private static GradientDrawable makeDot(Context ctx, String hex, int sizeDp) {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(safeColor(hex));
        int px = dp(ctx, sizeDp);
        d.setSize(px, px);
        return d;
    }

    private static int dp(Context ctx, int v) {
        return Math.round(v * ctx.getResources().getDisplayMetrics().density);
    }

    private static int safeColor(String hex) {
        try { return Color.parseColor(hex); } catch (Exception e) { return Color.GRAY; }
    }
}

