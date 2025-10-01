package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.CartRepository;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.utils.RepositoryCallback;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {
    private final CartRepository repository = new CartRepository();

    private final MutableLiveData<List<ShopItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Integer> totalPrice = new MutableLiveData<>(0);
    private ListenerRegistration cartListener;

    public CartViewModel() {
        cartListener = repository.observeCart(new RepositoryCallback<List<ShopItem>>() {
            @Override
            public void onSuccess(List<ShopItem> result) {
                cartItems.setValue(result);
                recalcTotal(result);
            }

            @Override
            public void onFailure(Exception e) {
                cartItems.setValue(new ArrayList<>());
                totalPrice.setValue(0);
            }
        });
    }

    public LiveData<List<ShopItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Integer> getTotalPrice() {
        return totalPrice;
    }

    public void addItem(ShopItem item) {
        repository.addItem(item, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { /* Firestore update */ }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    public void removeItem(ShopItem item) {
        repository.removeItem(item, new RepositoryCallback<Void>() {
            @Override
            public void onSuccess(Void result) { }
            @Override
            public void onFailure(Exception e) { }
        });
    }

    private void recalcTotal(List<ShopItem> items) {
        int total = 0;
        for (ShopItem i : items) total += i.getPrice();
        totalPrice.setValue(total);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (cartListener != null) cartListener.remove();
    }
}