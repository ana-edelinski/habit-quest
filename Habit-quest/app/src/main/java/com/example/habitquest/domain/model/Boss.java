package com.example.habitquest.domain.model;

public class Boss {

    private int level;          // Redni broj bosa
    private int hp;             // Trenutni HP
    private int maxHp;          // Maksimalni HP
    private boolean defeated;   // Da li je poražen
    private int rewardCoins;    // Nagrada za pobedu
    private String id;          // Firestore ID dokumenta (opciono)

    public Boss() {} // Firestore zahteva prazan konstruktor

    public Boss(int level, int maxHp, int rewardCoins) {
        this.level = level;
        this.maxHp = maxHp;
        this.hp = maxHp;
        this.rewardCoins = rewardCoins;
        this.defeated = false;
    }

    public boolean isDefeated() {
        return defeated;
    }

    public void setDefeated(boolean defeated) {
        this.defeated = defeated;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getMaxHp() {
        return maxHp;
    }

    public void setMaxHp(int maxHp) {
        this.maxHp = maxHp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getRewardCoins() {
        return rewardCoins;
    }

    public void setRewardCoins(int rewardCoins) {
        this.rewardCoins = rewardCoins;
    }
}
