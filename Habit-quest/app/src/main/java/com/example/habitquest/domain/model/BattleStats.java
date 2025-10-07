package com.example.habitquest.domain.model;

public class BattleStats {

    private String firebaseUid;
    private String bossId;
    private long createdAt;
    private boolean rewardGranted;
    private int totalAttempts;        // ukupno pokušaja (uvek 5)
    private int remainingAttempts;    // preostali napadi
    private double successRate;       // uspešnost (npr. 67.5%)
    private int userPP;               // snaga korisnika
    private int bossHP;               // trenutni HP bosa
    private int bossMaxHP;            // max HP bosa
    private int hitsLanded;           // broj uspešnih udaraca
    private boolean battleOver;       // da li je borba završena
    private boolean victory;          // da li je korisnik pobedio

    public BattleStats() {}

    public BattleStats(String firebaseUid, String bossId, long createdAt, boolean rewardGranted, int totalAttempts, double successRate, int userPP, int bossMaxHP, int bossHP) {
        this.firebaseUid = firebaseUid;
        this.bossId = bossId;
        this.createdAt = createdAt;
        this.rewardGranted = rewardGranted;
        this.totalAttempts = totalAttempts;
        this.remainingAttempts = totalAttempts;
        this.successRate = successRate;
        this.userPP = userPP;
        this.bossMaxHP = bossMaxHP;
        this.bossHP = bossHP;
        this.hitsLanded = 0;
        this.battleOver = false;
        this.victory = false;
    }


    public int getTotalAttempts() { return totalAttempts; }
    public void setTotalAttempts(int totalAttempts) { this.totalAttempts = totalAttempts; }

    public int getRemainingAttempts() { return remainingAttempts; }
    public void setRemainingAttempts(int remainingAttempts) { this.remainingAttempts = remainingAttempts; }

    public double getSuccessRate() { return successRate; }
    public void setSuccessRate(double successRate) { this.successRate = successRate; }

    public int getUserPP() { return userPP; }
    public void setUserPP(int userPP) { this.userPP = userPP; }

    public int getBossHP() { return bossHP; }
    public void setBossHP(int bossHP) { this.bossHP = bossHP; }

    public int getBossMaxHP() { return bossMaxHP; }
    public void setBossMaxHP(int bossMaxHP) { this.bossMaxHP = bossMaxHP; }

    public int getHitsLanded() { return hitsLanded; }
    public void setHitsLanded(int hitsLanded) { this.hitsLanded = hitsLanded; }

    public boolean isBattleOver() { return battleOver; }
    public void setBattleOver(boolean battleOver) { this.battleOver = battleOver; }

    public boolean isVictory() { return victory; }
    public void setVictory(boolean victory) { this.victory = victory; }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRewardGranted() {
        return rewardGranted;
    }

    public void setRewardGranted(boolean rewardGranted) {
        this.rewardGranted = rewardGranted;
    }
}
