package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvName;

        VH(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvMemberName);
        }
    }
}
