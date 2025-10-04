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
    private User currentUser; // 🔹 kompletan ulogovani korisnik
    private String currentUid;

    private OnAddFriendClickListener listener;
    private OnCancelFriendRequestClickListener onCancelFriendRequestClickListener;

    public interface OnAddFriendClickListener {
        void onAddFriendClicked(User user);
    }

    public interface OnCancelFriendRequestClickListener {
        void onCancelFriendRequestClicked(User user);
    }

    public UserSearchAdapter(OnAddFriendClickListener listener) {
        this.listener = listener;
    }

    public void setOnCancelFriendRequestClickListener(OnCancelFriendRequestClickListener listener) {
        this.onCancelFriendRequestClickListener = listener;
    }

    public void setCurrentUid(String currentUid) {
        this.currentUid = currentUid;
    }

    public void setUsers(List<User> users) {
        this.users = users != null ? users : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
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
            case 3: resId = R.drawable.avatar3; break;
            case 4: resId = R.drawable.avatar4; break;
            case 5: resId = R.drawable.avatar5; break;
            default: resId = R.drawable.avatar5;
        }
        holder.imgAvatar.setImageResource(resId);

        boolean isSelf = user.getUid().equals(currentUid);
        if (isSelf) {
            holder.btnAddFriend.setVisibility(View.GONE);
            return;
        } else {
            holder.btnAddFriend.setVisibility(View.VISIBLE);
        }

        boolean isFriend = currentUser != null && currentUser.getFriends() != null && currentUser.getFriends().contains(user.getUid());
        boolean isRequestSent = currentUser != null && currentUser.getFriendRequestsSent() != null && currentUser.getFriendRequestsSent().contains(user.getUid());
        boolean isRequestReceived = currentUser != null && currentUser.getFriendRequestsReceived() != null && currentUser.getFriendRequestsReceived().contains(user.getUid());

        if (isFriend) {
            holder.btnAddFriend.setText("Friends");
            holder.btnAddFriend.setEnabled(false);
        } else if (isRequestSent) {
            holder.btnAddFriend.setText("Request Sent");
            holder.btnAddFriend.setEnabled(true);
            holder.btnAddFriend.setOnClickListener(v -> {
                if (onCancelFriendRequestClickListener != null)
                    onCancelFriendRequestClickListener.onCancelFriendRequestClicked(user);
            });
        } else if (isRequestReceived) {
            holder.btnAddFriend.setText("Request Received");
            holder.btnAddFriend.setEnabled(false);
        } else {
            holder.btnAddFriend.setText("Add Friend");
            holder.btnAddFriend.setEnabled(true);
            holder.btnAddFriend.setOnClickListener(v -> {
                if (listener != null) listener.onAddFriendClicked(user);
            });
        }

        // 🔹 Klik na korisnika otvara profil
        holder.itemView.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(v);
            if (user.getUid().equals(currentUid)) {
                navController.navigate(R.id.nav_account);
            } else {
                Bundle args = new Bundle();
                args.putString("userId", user.getUid());
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
