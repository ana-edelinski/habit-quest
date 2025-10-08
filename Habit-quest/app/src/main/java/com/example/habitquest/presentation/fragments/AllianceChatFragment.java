package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.habitquest.R;
import com.example.habitquest.data.prefs.AppPreferences;
import com.example.habitquest.domain.model.AllianceMessage;
import com.example.habitquest.presentation.adapters.AllianceChatAdapter;
import com.example.habitquest.presentation.viewmodels.AllianceChatViewModel;
import com.example.habitquest.utils.NotificationHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

public class AllianceChatFragment extends Fragment {

    private AllianceChatViewModel viewModel;
    private AllianceChatAdapter adapter;
    private String allianceId;
    private String currentUserId;
    private String currentUsername;

    private EditText etMessage;
    private ImageButton btnSend;
    private List<AllianceMessage> previousMessages;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_alliance_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.GONE);

        allianceId = getArguments().getString("allianceId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        AppPreferences prefs = new AppPreferences(requireContext());
        currentUsername = prefs.getUsername();
        if (TextUtils.isEmpty(currentUsername)) currentUsername = "Player";

        RecyclerView recyclerView = view.findViewById(R.id.recyclerMessages);
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        adapter = new AllianceChatAdapter(currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        viewModel = new ViewModelProvider(this).get(AllianceChatViewModel.class);
        viewModel.messages.observe(getViewLifecycleOwner(), this::updateMessages);

        viewModel.currentUsername.observe(getViewLifecycleOwner(), username -> {
            if (!TextUtils.isEmpty(username)) {
                currentUsername = username;
            }
        });

        viewModel.loadUsername(currentUserId);

        btnSend.setOnClickListener(v -> sendMessage());

        setupToolbar();
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    private void setupToolbar() {
        MaterialToolbar toolbar = requireActivity().findViewById(R.id.topAppBar);
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
        toolbar.setNavigationOnClickListener(v -> requireActivity().onBackPressed());
    }

    private void updateMessages(List<AllianceMessage> messages) {
        if (messages == null || messages.isEmpty()) return;

        adapter.setMessages(messages);

        RecyclerView recyclerView = requireView().findViewById(R.id.recyclerMessages);

        if (previousMessages == null || messages.size() > previousMessages.size()) {
            recyclerView.scrollToPosition(messages.size() - 1);
        }

        previousMessages = messages;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        if (TextUtils.isEmpty(currentUsername)) {
            Toast.makeText(getContext(), "Loading your profile...", Toast.LENGTH_SHORT).show();
            return;
        }

        AppPreferences prefs = new AppPreferences(requireContext());
        int avatarIndex = prefs.getAvatarIndex();

        AllianceMessage msg = new AllianceMessage(
                null,
                currentUserId,
                currentUsername,
                text,
                System.currentTimeMillis(),
                avatarIndex
        );


        viewModel.sendMessage(allianceId, msg);
        etMessage.setText("");
    }

    @Override
    public void onStart() {
        super.onStart();
        new AppPreferences(requireContext()).setChatOpen(true);
        viewModel.startListening(allianceId);
    }

    @Override
    public void onStop() {
        super.onStop();
        new AppPreferences(requireContext()).setChatOpen(false);
        viewModel.stopListening();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireActivity().findViewById(R.id.bottomNavigation).setVisibility(View.VISIBLE);

        MaterialToolbar toolbar = requireActivity().findViewById(R.id.topAppBar);
        toolbar.setNavigationIcon(R.drawable.ic_menu);
        toolbar.setNavigationOnClickListener(v -> {
            androidx.drawerlayout.widget.DrawerLayout drawer = requireActivity().findViewById(R.id.drawerLayout);
            drawer.open();
        });
    }
}
