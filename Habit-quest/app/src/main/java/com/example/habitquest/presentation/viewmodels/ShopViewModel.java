package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.data.repositories.CartRepository;
import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.ShopItem;
import com.example.habitquest.domain.model.User;

import java.util.List;
import java.util.stream.Collectors;

public class ShopViewModel extends ViewModel {

    private final CartRepository repository = new CartRepository();

    private final MutableLiveData<List<ShopItem>> potions = new MutableLiveData<>();
    private final MutableLiveData<List<ShopItem>> clothing = new MutableLiveData<>();

    public void loadShopItems(User user) {
        List<ShopItem> allItems = repository.getShopItemsForUser(user);

        List<ShopItem> potionItems = allItems.stream()
                .filter(i -> i.getType() == EquipmentType.POTION)
                .collect(Collectors.toList());

        List<ShopItem> clothingItems = allItems.stream()
                .filter(i -> i.getType() == EquipmentType.CLOTHING)
                .collect(Collectors.toList());

        potions.setValue(potionItems);
        clothing.setValue(clothingItems);
    }

    public LiveData<List<ShopItem>> getPotions() {
        return potions;
    }

    public LiveData<List<ShopItem>> getClothing() {
        return clothing;
    }
}
