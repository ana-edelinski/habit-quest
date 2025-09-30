package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.habitquest.R;
import com.example.habitquest.presentation.adapters.ShopAdapter;
import com.example.habitquest.presentation.viewmodels.ShopViewModel;

public class StoreFragment extends Fragment {

    private ShopAdapter potionsAdapter;
    private ShopAdapter clothingAdapter;
    private ShopViewModel viewModel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_store, container, false);

        // Potions
        RecyclerView rvPotions = v.findViewById(R.id.rvPotions);
        GridLayoutManager potionsLayout = new GridLayoutManager(getContext(), 2);
        potionsLayout.setAutoMeasureEnabled(true);
        rvPotions.setLayoutManager(potionsLayout);
        rvPotions.setNestedScrollingEnabled(false);
        potionsAdapter = new ShopAdapter();
        rvPotions.setAdapter(potionsAdapter);

        // Clothing
        RecyclerView rvClothing = v.findViewById(R.id.rvClothing);
        GridLayoutManager clothingLayout = new GridLayoutManager(getContext(), 2);
        clothingLayout.setAutoMeasureEnabled(true);
        rvClothing.setLayoutManager(clothingLayout);
        rvClothing.setNestedScrollingEnabled(false);
        clothingAdapter = new ShopAdapter();
        rvClothing.setAdapter(clothingAdapter);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);

        // Observers
        viewModel.getPotions().observe(getViewLifecycleOwner(), items -> {
            potionsAdapter.setItems(items);
        });

        viewModel.getClothing().observe(getViewLifecycleOwner(), items -> {
            clothingAdapter.setItems(items);
        });

        return v;
    }
}
