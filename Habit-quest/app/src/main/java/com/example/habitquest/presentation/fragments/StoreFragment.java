package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.ShopData;
import com.example.habitquest.presentation.adapters.ShopAdapter;

public class StoreFragment extends Fragment {

    private ShopAdapter potionsAdapter;
    private ShopAdapter clothingAdapter;

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
        potionsAdapter.setItems(ShopData.getByType(EquipmentType.POTION));

        // Clothing
        RecyclerView rvClothing = v.findViewById(R.id.rvClothing);
        GridLayoutManager clothingLayout = new GridLayoutManager(getContext(), 2);
        clothingLayout.setAutoMeasureEnabled(true);
        rvClothing.setLayoutManager(clothingLayout);
        rvClothing.setNestedScrollingEnabled(false);
        clothingAdapter = new ShopAdapter();
        rvClothing.setAdapter(clothingAdapter);
        clothingAdapter.setItems(ShopData.getByType(EquipmentType.CLOTHING));

        return v;
    }
}
