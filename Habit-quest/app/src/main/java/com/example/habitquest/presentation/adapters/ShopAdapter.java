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

    public enum Mode { STORE, CART }
    private List<ShopItem> items = new ArrayList<>();
    private OnItemClickListener listener;
    private Mode mode;

    public ShopAdapter(Mode mode) {
        this.mode = mode;
    }

    public interface OnItemClickListener {
        void onCartClick(ShopItem item);
        void onRemoveClick(ShopItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

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
        holder.bind(item, listener, mode);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ShopViewHolder extends RecyclerView.ViewHolder {
        ImageView ivItemImage, ivCart, ivRemove;
        TextView tvPrice, tvName;

        public ShopViewHolder(@NonNull View itemView) {
            super(itemView);
            ivItemImage = itemView.findViewById(R.id.ivItemImage);
            ivCart = itemView.findViewById(R.id.ivCart);
            ivRemove = itemView.findViewById(R.id.ivRemove);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvName = itemView.findViewById(R.id.tvName);
        }

        public void bind(ShopItem item, OnItemClickListener listener, Mode mode) {
            ivItemImage.setImageResource(item.getImageResId());
            tvPrice.setText(item.getCalculatedPrice() + " 🪙");
            tvName.setText(item.getName());

            if (mode == Mode.STORE) {
                ivCart.setVisibility(View.VISIBLE);
                ivRemove.setVisibility(View.GONE);

                ivCart.setOnClickListener(v -> {
                    if (listener != null) listener.onCartClick(item);
                });
            } else if (mode == Mode.CART) {
                ivCart.setVisibility(View.GONE);
                ivRemove.setVisibility(View.VISIBLE);

                ivRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onRemoveClick(item);
                });
            }
        }

    }
}
