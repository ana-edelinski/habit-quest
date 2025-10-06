package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.EquipmentViewHolder> {

    private List<ShopItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private boolean isCompact = false;

    public EquipmentAdapter() {
        this(false);
    }

    public EquipmentAdapter(boolean isCompact) {
        this.isCompact = isCompact;
    }

    public interface OnItemClickListener {
        void onActivateClick(ShopItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<ShopItem> items) {
        if (items == null) items = new ArrayList<>();
        Collections.sort(items, Comparator.comparing(ShopItem::isActive).reversed());
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EquipmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = isCompact ? R.layout.item_equipment_small : R.layout.item_equipment;
        View v = LayoutInflater.from(parent.getContext()).inflate(layoutId, parent, false);
        return new EquipmentViewHolder(v, isCompact);
    }

    @Override
    public void onBindViewHolder(@NonNull EquipmentViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EquipmentViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvName;
        Button btnActivate;
        boolean isCompact;

        public EquipmentViewHolder(@NonNull View itemView, boolean isCompact) {
            super(itemView);
            this.isCompact = isCompact;

            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            tvName = itemView.findViewById(R.id.tvName);

            btnActivate = itemView.findViewById(R.id.btnActivate);
        }

        public void bind(ShopItem item, OnItemClickListener listener) {
            ivItemImage.setImageResource(item.getImageResId());
            tvName.setText(item.getName());

            if (isCompact) {
                if (tvName != null) tvName.setVisibility(View.GONE);
                if (btnActivate != null) btnActivate.setVisibility(View.GONE);
                return;
            }

            if (item.isActive()) {
                btnActivate.setText("Activated");
                btnActivate.setEnabled(false);
            } else {
                btnActivate.setText("Activate");
                btnActivate.setEnabled(true);
                btnActivate.setOnClickListener(v -> {
                    if (listener != null) listener.onActivateClick(item);
                });
            }
        }

    }
}
