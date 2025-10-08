package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.presentation.adapters.AllianceChatAdapter;
import com.example.habitquest.presentation.viewmodels.AllianceChatViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import com.example.habitquest.utils.NotificationHelper;

public class AllianceChatFragment extends Fragment {

    private AllianceChatViewModel viewModel;
    private AllianceChatAdapter adapter;
    private String allianceId;
    private String currentUserId;
    private String currentUsername;

    private EditText etMessage;
    private ImageButton btnSend;
    private List<AllianceMessage> previousMessages = null;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        allianceId = getArguments().getString("allianceId");
        currentUserId = FirebaseAuth.getInstance().getUid();
        currentUsername = "Player"; // kasnije povući iz profila

        RecyclerView recyclerView = view.findViewById(R.id.recyclerMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        adapter = new AllianceChatAdapter(currentUserId);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AllianceChatViewModel.class);
        viewModel.messages.observe(getViewLifecycleOwner(), this::updateMessages);

        btnSend.setOnClickListener(v -> sendMessage());
    }

    private void updateMessages(List<AllianceMessage> messages) {
        adapter.setMessages(messages);

        // 🔹 Provera da li je stigla nova poruka
        if (previousMessages != null && messages.size() > previousMessages.size()) {
            AllianceMessage lastMessage = messages.get(messages.size() - 1);
            if (!lastMessage.getSenderId().equals(currentUserId)) {
                NotificationHelper.showAllianceChatMessage(
                        requireContext(),
                        lastMessage.getSenderName(),
                        lastMessage.getText()
                );
            }
        }
        previousMessages = messages;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        AllianceMessage msg = new AllianceMessage(
                null,
                currentUserId,
                currentUsername,
                text,
                System.currentTimeMillis()
        );
        viewModel.sendMessage(allianceId, msg);
        etMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        viewModel.startListening(allianceId);
    }

    @Override
    public void onStop() {
        super.onStop();
        viewModel.stopListening();
    }
}
