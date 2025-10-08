package com.example.habitquest.presentation.fragments;

import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.habitquest.R;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.presentation.adapters.ShopAdapter;
import com.example.habitquest.presentation.viewmodels.CartViewModel;
import com.example.habitquest.utils.MissionProgressHelper;
import com.example.habitquest.utils.RepositoryCallback;

public class CartFragment extends Fragment {

    private CartViewModel cartViewModel;
    private ShopAdapter cartAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rvCart = view.findViewById(R.id.rvCart);
        rvCart.setLayoutManager(new GridLayoutManager(getContext(), 2));
        cartAdapter = new ShopAdapter(ShopAdapter.Mode.CART);
        rvCart.setAdapter(cartAdapter);

        TextView tvTotal = view.findViewById(R.id.tvTotal);
        Button btnBuy = view.findViewById(R.id.btnBuy);

        cartViewModel = new ViewModelProvider(requireActivity()).get(CartViewModel.class);

        // Observers
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), items -> {
            cartAdapter.setItems(items);
        });

        cartViewModel.getTotalPrice().observe(getViewLifecycleOwner(), total -> {
            tvTotal.setText("Total: " + total + " coins");
        });

        // Klikovi na X (remove)
        cartAdapter.setOnItemClickListener(new ShopAdapter.OnItemClickListener() {
            @Override
            public void onCartClick(ShopItem item) {
                // ovde nema dodavanja u korpu, pa ostaje prazno
            }

            @Override
            public void onRemoveClick(ShopItem item) {
                cartViewModel.removeItem(item);
            }
        });

        btnBuy.setOnClickListener(v -> {
            cartViewModel.buyItems(new RepositoryCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    MissionProgressHelper.reportShopPurchase(requireActivity());
                    Toast.makeText(getContext(), "Purchase successful!", Toast.LENGTH_SHORT).show();
                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                    navController.navigate(R.id.nav_equipment);
                }

                @Override
                public void onFailure(Exception e) {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
