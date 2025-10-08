package com.example.habitquest.domain.model;

import com.google.firebase.Timestamp;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AllianceMission {

    private String id;
    private String allianceId;
    private long bossHP;
    private long remainingHP;
    private Timestamp startDate;
    private Timestamp endDate;
    private boolean active;
    private Map<String, MemberMissionProgress> memberProgress; // uid -> progress
    private boolean finished;
    private boolean victory;


    public AllianceMission() {
    }

    public AllianceMission(String id, String allianceId, int memberCount) {
        this.id = id;
        this.allianceId = allianceId;
        this.bossHP = 100L * memberCount;
        this.remainingHP = this.bossHP;
        this.startDate = Timestamp.now();
        // kraj misije za 14 dana — skrati na nekoliko minuta za demonstraciju
        this.endDate = new Timestamp(Instant.ofEpochMilli(startDate.toDate().getTime() + 14L * 24 * 60 * 60 * 1000));
        this.active = true;
        this.memberProgress = new HashMap<>();
    }

    /** Dodaje novog člana u mapu napretka ako nije već tu */
    public void addMember(String userId) {
        if (!memberProgress.containsKey(userId)) {
            memberProgress.put(userId, new MemberMissionProgress(userId));
        }
    }

    /** Primena smanjenja HP-a bosa */
    public void applyDamage(String userId, int damage) {
        if (!active) return;
        MemberMissionProgress progress = memberProgress.get(userId);
        if (progress != null) {
            remainingHP = Math.max(0, remainingHP - damage);
            progress.addDamage(damage);
        }
    }

    /** Provera da li je misija završena */
    public boolean isFinished() {
        return remainingHP <= 0 || Timestamp.now().compareTo(endDate) > 0;
    }

    /** Završava misiju */
    public void finish(boolean victory) {
        this.active = false;
        this.finished = true;
        this.victory = victory;
        this.remainingHP = Math.max(0, remainingHP);
    }

    // --- Getteri i setteri ---
    public String getId() { return id; }
    public String getAllianceId() { return allianceId; }
    public long getBossHP() { return bossHP; }
    public long getRemainingHP() { return remainingHP; }
    public Timestamp getStartDate() { return startDate; }
    public Timestamp getEndDate() { return endDate; }
    public boolean isActive() { return active; }
    public Map<String, MemberMissionProgress> getMemberProgress() { return memberProgress; }

    public void setMemberProgress(Map<String, MemberMissionProgress> memberProgress) {
        this.memberProgress = memberProgress;
    }

    public void setBossHP(long bossHP) {
        this.bossHP = bossHP;
    }

    public void setRemainingHP(long remainingHP) {
        this.remainingHP = remainingHP;
    }

    public boolean isVictory() {
        return victory;
    }
}
