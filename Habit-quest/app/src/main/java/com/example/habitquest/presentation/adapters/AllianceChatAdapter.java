package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitquest.R;
import com.example.habitquest.domain.model.AllianceMessage;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AllianceChatAdapter extends RecyclerView.Adapter<AllianceChatAdapter.MessageViewHolder> {

    private List<AllianceMessage> messages;
    private final String currentUserId;

    public AllianceChatAdapter(String currentUserId) {
        this.currentUserId = currentUserId;
    }

    public void setMessages(List<AllianceMessage> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).getSenderId().equals(currentUserId) ? 1 : 0;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = viewType == 1 ? R.layout.item_message_outgoing : R.layout.item_message_incoming;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        AllianceMessage msg = messages.get(position);
        holder.tvText.setText(msg.getText());
        holder.tvInfo.setText(msg.getSenderName() + " • " +
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(msg.getTimestamp())));
    }

    @Override
    public int getItemCount() { return messages == null ? 0 : messages.size(); }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvInfo;
        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvInfo = itemView.findViewById(R.id.tvInfo);
        }
    }
}
