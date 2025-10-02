package com.example.habitquest.domain.model;

public class ShopItem {
    private String name;
    private EquipmentType type;
    private double priceFactor;
    private int calculatedPrice; // ovo punimo kad znamo previousBossReward
    private double bonus;
    private boolean active;
    private boolean permanent;
    private int imageResId;

    public ShopItem() {}

    //za shop data treba bez izracunate cene
    public ShopItem(String name, EquipmentType type, double priceFactor,
                    double bonus, boolean permanent, int imageResId) {
        this.name = name;
        this.type = type;
        this.priceFactor = priceFactor;
        this.bonus = bonus;
        this.permanent = permanent;
        this.active = false;
        this.imageResId = imageResId;
    }

    //sa izracunatom cenom za prikaz
    public ShopItem(ShopItem base, int previousBossReward) {
        this.name = base.name;
        this.type = base.type;
        this.priceFactor = base.priceFactor;
        this.bonus = base.bonus;
        this.permanent = base.permanent;
        this.active = base.active;
        this.imageResId = base.imageResId;
        this.calculatedPrice = (int) (previousBossReward * base.priceFactor);
    }


    public String getName() { return name; }
    public EquipmentType getType() { return type; }
    public double getBonus() { return bonus; }
    public boolean isActive() { return active; }
    public boolean isPermanent() { return permanent; }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(EquipmentType type) {
        this.type = type;
    }

    public double getPriceFactor() {
        return priceFactor;
    }

    public void setPriceFactor(double priceFactor) {
        this.priceFactor = priceFactor;
    }

    public void setBonus(double bonus) {
        this.bonus = bonus;
    }

    public void setPermanent(boolean permanent) {
        this.permanent = permanent;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public int getImageResId() { return imageResId; }

    public void setActive(boolean active) { this.active = active; }

    public int getCalculatedPrice() {
        return calculatedPrice;
    }
    public void setCalculatedPrice(int calculatedPrice) {
        this.calculatedPrice = calculatedPrice;
    }
}
