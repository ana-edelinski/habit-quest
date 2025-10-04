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
import com.example.habitquest.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestsAdapter extends RecyclerView.Adapter<FriendRequestsAdapter.VH> {

    private List<User> users = new ArrayList<>();

    public void submitList(List<User> list) {
        users = (list != null) ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_request, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());
        holder.ivAvatar.setImageResource(getAvatarRes(user.getAvatar()));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView ivAvatar;
        TextView tvUsername;
        Button btnAccept, btnReject;

        VH(@NonNull View itemView) {
            super(itemView);
            ivAvatar = itemView.findViewById(R.id.ivAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAccept = itemView.findViewById(R.id.btnAccept);
            btnReject = itemView.findViewById(R.id.btnReject);
        }
    }

    private int getAvatarRes(int index) {
        switch (index) {
            case 1: return R.drawable.avatar1;
            case 2: return R.drawable.avatar2;
            case 3: return R.drawable.avatar3;
            case 4: return R.drawable.avatar4;
            case 5: return R.drawable.avatar5;
            default: return R.drawable.avatar1;
        }
    }
}
