package com.example.habitquest.domain.model;

public class PotentialRewards {
    private int minCoins;
    private int maxCoins;
    private double equipmentChance;
    private String description;

    public PotentialRewards(int previousBossReward) {
        this.maxCoins = previousBossReward > 0
                ? (int) Math.round(previousBossReward * 1.2)
                : 200;
        this.minCoins = this.maxCoins/2;
        this.equipmentChance = 0.2;
        this.description = "Coins: " + this.minCoins + "–" + this.maxCoins + ", Equipment chance: " + this.equipmentChance*100;
    }

    public int getMinCoins() { return minCoins; }
    public int getMaxCoins() { return maxCoins; }
    public double getEquipmentChance() { return equipmentChance; }
    public String getDescription() { return description; }
}
