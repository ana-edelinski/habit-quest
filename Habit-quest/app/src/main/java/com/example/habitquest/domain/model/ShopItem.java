package com.example.habitquest.domain.model;

import java.util.Objects;

public class ShopItem {
    private String name;
    private EquipmentType type;
    private int price;
    private double bonus;
    private boolean active;
    private int imageResId;

    public ShopItem() {}

    public ShopItem(String name, EquipmentType type, int price, double bonus, int imageResId) {
        this.name = name;
        this.type = type;
        this.price = price;
        this.bonus = bonus;
        this.active = false;
        this.imageResId = imageResId;
    }

    public String getName() { return name; }
    public EquipmentType getType() { return type; }
    public int getPrice() { return price; }
    public double getBonus() { return bonus; }
    public boolean isActive() { return active; }
    public int getImageResId() { return imageResId; }

    public void setActive(boolean active) { this.active = active; }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ShopItem)) return false;
        ShopItem item = (ShopItem) o;
        return price == item.price &&
                Objects.equals(name, item.name) &&
                type == item.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, price);
    }
}

