package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.MemberMissionProgress;

import java.util.List;
import java.util.Map;

public class AllianceProgressAdapter extends RecyclerView.Adapter<AllianceProgressAdapter.MemberViewHolder> {

    private final List<MemberMissionProgress> members;
    private final long bossMaxHp;
    private final Map<String, String> memberNames; // uid -> name

    public AllianceProgressAdapter(List<MemberMissionProgress> members, Map<String, String> memberNames, long bossMaxHp) {
        this.members = members;
        this.memberNames = memberNames;
        this.bossMaxHp = bossMaxHp;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member_progress, parent, false);
        return new MemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        MemberMissionProgress progress = members.get(position);
        String name = memberNames.getOrDefault(progress.getUserId(), "Unknown");
        int damage = progress.getHpReduced();
        int percent = (int) ((damage * 100.0) / bossMaxHp);

        holder.tvMemberName.setText(name);
        holder.progressMember.setProgress(percent);
        holder.tvMemberHp.setText("Dealt " + damage + " HP (" + percent + "%)");
    }

    @Override
    public int getItemCount() {
        return members.size();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvMemberName, tvMemberHp;
        ProgressBar progressMember;

        public MemberViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMemberName = itemView.findViewById(R.id.tvMemberName);
            tvMemberHp = itemView.findViewById(R.id.tvMemberHp);
            progressMember = itemView.findViewById(R.id.progressMember);
        }
    }
}
