package com.example.habitquest.presentation.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.presentation.adapters.EquipmentAdapter;
import com.example.habitquest.presentation.viewmodels.EquipmentViewModel;
import com.example.habitquest.utils.RepositoryCallback;

public class EquipmentFragment extends Fragment {

    private EquipmentViewModel equipmentViewModel;
    private EquipmentAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_equipment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.recyclerEquipment);
        rv.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new EquipmentAdapter();
        rv.setAdapter(adapter);

        equipmentViewModel = new ViewModelProvider(requireActivity()).get(EquipmentViewModel.class);

        equipmentViewModel.getEquipment().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
        });

        adapter.setOnItemClickListener(item -> {
            equipmentViewModel.activateItem(item, new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    Toast.makeText(getContext(), item.getName() + " activated!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), "Failed to activate " + item.getName(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
