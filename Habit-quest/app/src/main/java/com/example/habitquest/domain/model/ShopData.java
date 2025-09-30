package com.example.habitquest.domain.model;

import com.example.habitquest.R;

import java.util.Arrays;
import java.util.List;

public class ShopData {

    public static final List<ShopItem> ITEMS = Arrays.asList(
            new ShopItem("SWORD", EquipmentType.WEAPON, 200, 0.05, R.drawable.ic_sword),
            new ShopItem("BOW AND ARROW", EquipmentType.WEAPON, 300, 0.05, R.drawable.ic_bow_arrow),

            new ShopItem("GLOVES", EquipmentType.CLOTHING, 120, 0.10, R.drawable.ic_gloves),
            new ShopItem("SHIELD", EquipmentType.CLOTHING, 120, 0.10, R.drawable.ic_shield),
            new ShopItem("BOOTS", EquipmentType.CLOTHING, 150, 0.40, R.drawable.ic_boots),

            new ShopItem("TEMPORARY BOOST: +20% PP", EquipmentType.POTION, 100, 0.20, R.drawable.ic_temp_cheaper),
            new ShopItem("TEMPORARY BOOST: +40% PP", EquipmentType.POTION, 150, 0.40, R.drawable.ic_temp_exp),
            new ShopItem("PERMANENT BOOST: +5% PP", EquipmentType.POTION, 400, 0.05, R.drawable.ic_perm_cheap),
            new ShopItem("PERMANENT BOOST: +10% PP", EquipmentType.POTION, 2000, 0.10, R.drawable.ic_perm_exp)
    );
}

