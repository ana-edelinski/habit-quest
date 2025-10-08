package com.example.habitquest.presentation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.repositories.UserRepository;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.utils.RepositoryCallback;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AllianceChatAdapter extends RecyclerView.Adapter<AllianceChatAdapter.MessageViewHolder> {

    private List<AllianceMessage> messages;
    private final String currentUserId;
    private final Map<String, Integer> avatarCache = new HashMap<>();

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
        int layout = viewType == 1
                ? R.layout.item_message_outgoing
                : R.layout.item_message_incoming;
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return new MessageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        AllianceMessage msg = messages.get(position);
        holder.tvText.setText(msg.getText());
        holder.tvInfo.setText(msg.getSenderName() + " • " +
                new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(msg.getTimestamp())));


        String senderId = msg.getSenderId();

        if (avatarCache.containsKey(senderId)) {
            int cachedAvatar = avatarCache.get(senderId);
            holder.imgAvatar.setImageResource(getAvatarResource(cachedAvatar));
            return;
        }

        if (msg.getSenderAvatar() != 0) {
            avatarCache.put(senderId, msg.getSenderAvatar());
            holder.imgAvatar.setImageResource(getAvatarResource(msg.getSenderAvatar()));
            return;
        }

        UserRepository repo = new UserRepository(holder.itemView.getContext());
        repo.getUser(senderId, new RepositoryCallback<User>() {
            @Override
            public void onSuccess(User user) {
                if (user != null) {
                    int avatarIndex = user.getAvatar();
                    avatarCache.put(senderId, avatarIndex);
                    holder.imgAvatar.setImageResource(getAvatarResource(avatarIndex));
                }
            }

            @Override
            public void onFailure(Exception e) {
            }
        });
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

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvText, tvInfo;
        ImageView imgAvatar;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvText = itemView.findViewById(R.id.tvText);
            tvInfo = itemView.findViewById(R.id.tvInfo);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return messages == null ? 0 : messages.size();
    }
}
