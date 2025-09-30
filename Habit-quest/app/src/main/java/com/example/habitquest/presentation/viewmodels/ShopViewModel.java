package com.example.habitquest.presentation.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.habitquest.domain.model.EquipmentType;
import com.example.habitquest.domain.model.ShopData;
import com.example.habitquest.domain.model.ShopItem;

import java.util.List;

public class ShopViewModel extends ViewModel {

    private final MutableLiveData<List<ShopItem>> potions = new MutableLiveData<>();
    private final MutableLiveData<List<ShopItem>> clothing = new MutableLiveData<>();

    public ShopViewModel() {
        // U startu puniš fiksne podatke iz ShopData
        potions.setValue(ShopData.getByType(EquipmentType.POTION));
        clothing.setValue(ShopData.getByType(EquipmentType.CLOTHING));
    }

    public LiveData<List<ShopItem>> getPotions() {
        return potions;
    }

    public LiveData<List<ShopItem>> getClothing() {
        return clothing;
    }

    // Opciono: kasnije možeš dodati metode tipa "buyItem"
    public void buyItem(ShopItem item) {
        // ovde ide logika skidanja coina, aktivacije itema itd.
        // za sada prazan
    }
}
