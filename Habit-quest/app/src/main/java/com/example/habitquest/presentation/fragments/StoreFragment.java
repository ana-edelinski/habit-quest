package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.presentation.activities.HomeActivity;
import com.example.habitquest.presentation.adapters.ShopAdapter;
import com.example.habitquest.presentation.viewmodels.CartViewModel;
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
        potionsAdapter = new ShopAdapter(ShopAdapter.Mode.STORE);
        rvPotions.setAdapter(potionsAdapter);

        // Clothing
        RecyclerView rvClothing = v.findViewById(R.id.rvClothing);
        GridLayoutManager clothingLayout = new GridLayoutManager(getContext(), 2);
        clothingLayout.setAutoMeasureEnabled(true);
        rvClothing.setLayoutManager(clothingLayout);
        rvClothing.setNestedScrollingEnabled(false);
        clothingAdapter = new ShopAdapter(ShopAdapter.Mode.STORE);
        rvClothing.setAdapter(clothingAdapter);

        // ViewModels
        viewModel = new ViewModelProvider(this).get(ShopViewModel.class);
        CartViewModel cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // Observers
        viewModel.getPotions().observe(getViewLifecycleOwner(), items -> {
            potionsAdapter.setItems(items);
        });

        viewModel.getClothing().observe(getViewLifecycleOwner(), items -> {
            clothingAdapter.setItems(items);
        });

        // Listeners
        potionsAdapter.setOnItemClickListener(new ShopAdapter.OnItemClickListener() {
            @Override
            public void onCartClick(ShopItem item) {
                cartViewModel.addItem(item);

                Toast.makeText(getContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();

                // mala animacija na ikoni korpe u toolbaru
                View cartIcon = requireActivity().findViewById(R.id.action_cart);
                if (cartIcon != null) {
                    cartIcon.animate()
                            .scaleX(1.2f).scaleY(1.2f)
                            .setDuration(150)
                            .withEndAction(() -> cartIcon.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(150));
                }
            }

            @Override
            public void onRemoveClick(ShopItem item) {
                // u StoreFragment-u se ne koristi remove
            }
        });

        clothingAdapter.setOnItemClickListener(new ShopAdapter.OnItemClickListener() {
            @Override
            public void onCartClick(ShopItem item) {
                cartViewModel.addItem(item);

                Toast.makeText(getContext(), item.getName() + " added to cart", Toast.LENGTH_SHORT).show();

                requireActivity().findViewById(R.id.action_cart).animate()
                        .scaleX(1.2f).scaleY(1.2f)
                        .setDuration(150)
                        .withEndAction(() -> requireActivity().findViewById(R.id.action_cart)
                                .animate().scaleX(1f).scaleY(1f).setDuration(150));
            }

            @Override
            public void onRemoveClick(ShopItem item) { }
        });


        return v;
    }
}
