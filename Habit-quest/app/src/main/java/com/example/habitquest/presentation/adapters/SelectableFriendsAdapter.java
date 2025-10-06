package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class SelectableFriendsAdapter extends RecyclerView.Adapter<SelectableFriendsAdapter.VH> {

    private List<User> friends = new ArrayList<>();
    private final List<User> selected = new ArrayList<>();

    public SelectableFriendsAdapter(List<User> friends) {
        this.friends = friends != null ? friends : new ArrayList<>();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_friend_selectable, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User friend = friends.get(position);

        holder.tvName.setText(friend.getUsername());
        holder.imgAvatar.setImageResource(getAvatarRes(friend.getAvatar()));

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(selected.contains(friend));
        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) selected.add(friend);
            else selected.remove(friend);
        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public List<User> getSelectedFriends() {
        return new ArrayList<>(selected);
    }

    public void setFriends(List<User> newFriends) {
        this.friends = newFriends != null ? new ArrayList<>(newFriends) : new ArrayList<>();
        selected.clear();
        notifyDataSetChanged();
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

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;
        CheckBox checkBox;

        VH(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvFriendName);
            checkBox = itemView.findViewById(R.id.cbSelectFriend);
        }
    }
}
