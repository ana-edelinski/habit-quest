package com.example.habitquest.presentation.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.habitquest.R;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.adapters.SelectableFriendsAdapter;
import com.google.android.material.button.MaterialButton;
import java.util.ArrayList;
import java.util.List;

public class AllianceCreateFragment extends Fragment {

    private EditText etAllianceName;
    private RecyclerView rvFriends;
    private MaterialButton btnCreate;
    private SelectableFriendsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_alliance_create, container, false);

        etAllianceName = v.findViewById(R.id.etAllianceName);
        rvFriends = v.findViewById(R.id.rvFriendsList);
        btnCreate = v.findViewById(R.id.btnCreateAlliance);

        rvFriends.setLayoutManager(new LinearLayoutManager(getContext()));

        rvFriends.setAdapter(adapter);

        btnCreate.setOnClickListener(v1 -> {
            String allianceName = etAllianceName.getText().toString().trim();
            List<User> selectedFriends = adapter.getSelectedFriends();
            // TODO
        });

        return v;
    }
}
