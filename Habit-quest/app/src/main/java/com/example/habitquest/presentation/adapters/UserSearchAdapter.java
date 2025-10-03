package com.example.habitquest.presentation.adapters;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserSearchAdapter extends RecyclerView.Adapter<UserSearchAdapter.UserViewHolder> {

    private List<User> users = new ArrayList<>();
    private OnAddFriendClickListener listener;

    public interface OnAddFriendClickListener {
        void onAddFriendClicked(User user);
    }

    public UserSearchAdapter(OnAddFriendClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_search, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        holder.tvUsername.setText(user.getUsername());

        int resId;
        switch (user.getAvatar()) {
            case 1: resId = R.drawable.avatar1; break;
            case 2: resId = R.drawable.avatar2; break;
            default: resId = R.drawable.avatar5;
        }
        holder.imgAvatar.setImageResource(resId);

        holder.btnAddFriend.setOnClickListener(v -> {
            if (listener != null) listener.onAddFriendClicked(user);
        });

        holder.itemView.setOnClickListener(v -> {
            Bundle args = new Bundle();
            args.putString("userId", user.getUid());
            NavController navController = Navigation.findNavController(v);
            navController.navigate(R.id.nav_user_profile, args);
        });
    }


    @Override
    public int getItemCount() {
        return users.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvUsername;
        Button btnAddFriend;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            btnAddFriend = itemView.findViewById(R.id.btnAddFriend);
        }
    }
}
