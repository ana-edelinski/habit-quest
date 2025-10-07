package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;

import java.util.ArrayList;
import java.util.List;

public class ActiveEquipmentAdapter extends RecyclerView.Adapter<ActiveEquipmentAdapter.VH> {

    private List<ShopItem> items = new ArrayList<>();

    public void submitList(List<ShopItem> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_equipment_small_fight, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        ShopItem item = items.get(position);
        holder.tvName.setText(item.getName());
        holder.ivItemImage.setImageResource(item.getImageResId());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivItemImage;
        TextView tvName;
        VH(@NonNull View v) {
            super(v);
            ivItemImage = v.findViewById(R.id.ivItemImage);
            tvName = v.findViewById(R.id.tvName);
        }
    }
}


