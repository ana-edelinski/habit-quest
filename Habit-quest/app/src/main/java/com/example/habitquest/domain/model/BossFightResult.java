package com.example.habitquest.domain.model;

public class BossFightResult {
    private String bossId;      // ID bosa iz Firestore
    private String firestoreUid;      // ID korisnika iz firestora
    private boolean victory;    // true ako je korisnik pobedio
    private int earnedCoins;    // Broj osvojenih novčića
    private String equipmentId; // ID dobijene opreme (ili null)
    private long timestamp;     // Vreme završetka borbe

    public BossFightResult() {}

    public BossFightResult(String bossId, String firestoreUid, boolean victory, int earnedCoins, String equipmentId) {
        this.bossId = bossId;
        this.firestoreUid = firestoreUid;
        this.victory = victory;
        this.earnedCoins = earnedCoins;
        this.equipmentId = equipmentId;
        this.timestamp = System.currentTimeMillis();
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(String equipmentId) {
        this.equipmentId = equipmentId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getEarnedCoins() {
        return earnedCoins;
    }

    public void setEarnedCoins(int earnedCoins) {
        this.earnedCoins = earnedCoins;
    }

    public String getFirestoreUid() {
        return firestoreUid;
    }

    public void setFirestoreUid(String firestoreUid) {
        this.firestoreUid = firestoreUid;
    }

    public boolean isVictory() {
        return victory;
    }

    public void setVictory(boolean victory) {
        this.victory = victory;
    }

    public String getBossId() {
        return bossId;
    }

    public void setBossId(String bossId) {
        this.bossId = bossId;
    }
}
