package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.User;

import java.util.ArrayList;
import java.util.List;

public class AllianceMemberAdapter extends RecyclerView.Adapter<AllianceMemberAdapter.VH> {

    private List<User> members = new ArrayList<>();

    public void setMembers(List<User> list) {
        members = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        User member = members.get(position);
        holder.tvName.setText(member.getUsername());

        int avatarRes = getAvatarResource(member.getAvatar());
        holder.imgAvatar.setImageResource(avatarRes);
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName;

        VH(View v) {
            super(v);
            imgAvatar = v.findViewById(R.id.imgAvatar);
            tvName = v.findViewById(R.id.tvMemberName);
        }
    }

    private int getAvatarResource(int index) {
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
