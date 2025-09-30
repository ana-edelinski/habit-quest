package com.example.habitquest.domain.model;

public class ShopItem {
    private String name;
    private EquipmentType type;
    private int price;
    private double bonus;
    private boolean active;
    private int imageResId;

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
}

