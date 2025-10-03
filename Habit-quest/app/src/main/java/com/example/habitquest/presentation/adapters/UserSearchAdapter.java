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
    private List<String> currentUserSentRequests = new ArrayList<>();

    private OnAddFriendClickListener listener;
    private String currentUid;

    public interface OnAddFriendClickListener {
        void onAddFriendClicked(User user);
    }

    public interface OnCancelFriendRequestClickListener {
        void onCancelFriendRequestClicked(User user);
    }

    private OnCancelFriendRequestClickListener onCancelFriendRequestClickListener;

    public void setOnCancelFriendRequestClickListener(OnCancelFriendRequestClickListener listener) {
        this.onCancelFriendRequestClickListener = listener;
    }

    public void setCurrentUid(String currentUid) {
        this.currentUid = currentUid;
    }

    public UserSearchAdapter(OnAddFriendClickListener listener) {
        this.listener = listener;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentUserSentRequests(List<String> sentRequests) {
        this.currentUserSentRequests = sentRequests != null ? sentRequests : new ArrayList<>();
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

        boolean isSelf = user.getUid().equals(currentUid);

        if (isSelf) {
            holder.btnAddFriend.setVisibility(View.GONE);
        } else {
            holder.btnAddFriend.setVisibility(View.VISIBLE);
        }

        boolean isRequestSent = currentUserSentRequests.contains(user.getUid());
        holder.btnAddFriend.setText(isRequestSent ? "Request Sent" : "Add Friend");

        holder.btnAddFriend.setOnClickListener(v -> {
            if (isRequestSent) {
                if (onCancelFriendRequestClickListener != null) {
                    onCancelFriendRequestClickListener.onCancelFriendRequestClicked(user);
                }
            } else {
                if (listener != null) {
                    listener.onAddFriendClicked(user);
                }
            }
        });

        holder.itemView.setOnClickListener(v -> {
            String currentUid = this.currentUid;
            if (user.getUid().equals(currentUid)) {
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_account);
            } else {
                Bundle args = new Bundle();
                args.putString("userId", user.getUid());
                NavController navController = Navigation.findNavController(v);
                navController.navigate(R.id.nav_user_profile, args);
            }
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
