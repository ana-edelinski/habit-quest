package com.example.habitquest.domain.model;

import com.example.habitquest.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ShopData {

    public static final List<ShopItem> ITEMS = Arrays.asList(
            new ShopItem("SWORD", EquipmentType.WEAPON, 1.0, 0.05, true, R.drawable.ic_sword),
            new ShopItem("BOW AND ARROW", EquipmentType.WEAPON, 1.5, 0.05, true, R.drawable.ic_bow_arrow),

            new ShopItem("TEMPORARY BOOST: +20% PP", EquipmentType.POTION, 0.5, 0.20, false, R.drawable.ic_temp_cheaper),
            new ShopItem("TEMPORARY BOOST: +40% PP", EquipmentType.POTION, 0.7, 0.40, false, R.drawable.ic_temp_exp),
            new ShopItem("PERMANENT BOOST: +5% PP", EquipmentType.POTION, 2.0, 0.05, true, R.drawable.ic_perm_cheap),
            new ShopItem("PERMANENT BOOST: +10% PP", EquipmentType.POTION, 10.0, 0.10, true, R.drawable.ic_perm_exp),

            new ShopItem("GLOVES", EquipmentType.CLOTHING, 0.6, 0.10, false, R.drawable.ic_gloves),
            new ShopItem("SHIELD", EquipmentType.CLOTHING, 0.6, 0.10, false, R.drawable.ic_shield),
            new ShopItem("BOOTS", EquipmentType.CLOTHING, 0.8, 0.40, false, R.drawable.ic_boots)
    );

    public static List<ShopItem> getByType(EquipmentType type) {
        List<ShopItem> filtered = new ArrayList<>();
        for (ShopItem item : ITEMS) {
            if (item.getType() == type) {
                filtered.add(item);
            }
        }
        return filtered;
    }
}
