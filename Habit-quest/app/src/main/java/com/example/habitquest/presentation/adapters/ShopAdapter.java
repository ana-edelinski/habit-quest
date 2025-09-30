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

public class ShopAdapter extends RecyclerView.Adapter<ShopAdapter.ShopViewHolder> {

    private List<ShopItem> items = new ArrayList<>();

    public void setItems(List<ShopItem> items) {
        this.items = items;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ShopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_shop, parent, false);
        return new ShopViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ShopViewHolder holder, int position) {
        ShopItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage, ivCart;
        TextView tvPrice, tvName;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            ivCart = itemView.findViewById(R.id.ivCart);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bind(ShopItem item) {
            ivItemImage.setImageResource(item.getImageResId());
            tvPrice.setText(item.getPrice() + " 🪙");
            tvName.setText(item.getName());
            // za sada korpa nema funkcionalnost
        }
    }
}
