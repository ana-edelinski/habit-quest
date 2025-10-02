package com.example.habitquest.presentation.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;
import com.example.habitquest.presentation.adapters.ShopAdapter;
import com.example.habitquest.presentation.viewmodels.AccountViewModel;
import com.example.habitquest.presentation.viewmodels.CartViewModel;
import com.example.habitquest.presentation.viewmodels.ShopViewModel;

public class ShopFragment extends Fragment {

    private ShopAdapter potionsAdapter;
    private ShopAdapter clothingAdapter;
    private ShopViewModel viewModel;
    private CartViewModel cartViewModel;
    private AccountViewModel accountViewModel;
    private User currentUser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_shop, container, false);

        TextView tvLockedMessage = v.findViewById(R.id.tvLockedMessage);
        TextView tvPotions = v.findViewById(R.id.tvPotions);
        TextView tvClothing = v.findViewById(R.id.tvClothing);

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
        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);
        accountViewModel = new ViewModelProvider(requireActivity()).get(AccountViewModel.class);

        // Load User and Shop Items
        accountViewModel.user.observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;

                Log.d("TEST", "Bosses defeated: " + user.getBossesDefeated());
                Log.d("TEST", "Previous boss reward: " + user.getPreviousBossReward());

                if (user.getBossesDefeated() == 0) {
                    rvPotions.setVisibility(View.GONE);
                    rvClothing.setVisibility(View.GONE);
                    tvPotions.setVisibility(View.GONE);
                    tvClothing.setVisibility(View.GONE);
                    tvLockedMessage.setVisibility(View.VISIBLE);
                } else {
                    rvPotions.setVisibility(View.VISIBLE);
                    rvClothing.setVisibility(View.VISIBLE);
                    tvPotions.setVisibility(View.VISIBLE);
                    tvClothing.setVisibility(View.VISIBLE);
                    tvLockedMessage.setVisibility(View.GONE);

                    viewModel.loadShopItems(user);
                }
            }
        });

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
                if (currentUser != null) {
                    cartViewModel.addItem(item, currentUser);

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
            }

            @Override
            public void onRemoveClick(ShopItem item) {
                // u ShopFragment-u se ne koristi remove
            }
        });

        clothingAdapter.setOnItemClickListener(new ShopAdapter.OnItemClickListener() {
            @Override
            public void onCartClick(ShopItem item) {
                if (currentUser != null) {
                    cartViewModel.addItem(item, currentUser);

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
            }

            @Override
            public void onRemoveClick(ShopItem item) { }
        });

        return v;
    }
}
