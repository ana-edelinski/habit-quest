package com.example.habitquest.domain.model;

public class MemberMissionProgress {

    private String userId;
    private String username;
    private int hpReduced;
    private int shopActions;
    private int bossHits;
    private int easyTasks;
    private int hardTasks;
    private boolean noFailedTasks;
    private int messageDays;

    public MemberMissionProgress() {}

    public MemberMissionProgress(String userId) {

        this.userId = userId;
    }

    // --- Granice iz specifikacije ---
    private static final int MAX_SHOP_ACTIONS = 1;
    private static final int MAX_BOSS_HITS = 10;
    private static final int MAX_EASY_TASKS = 10;
    private static final int MAX_HARD_TASKS = 6;
    private static final int MAX_MESSAGE_DAYS = 14;

    // --- HP vrednosti po akciji ---
    private static final int SHOP_DAMAGE = 2;
    private static final int BOSS_HIT_DAMAGE = 2;
    private static final int EASY_TASK_DAMAGE = 1;
    private static final int HARD_TASK_DAMAGE = 4;
    private static final int NO_FAILED_TASKS_DAMAGE = 10;
    private static final int MESSAGE_DAY_DAMAGE = 4;

    // --- Pamet: metode koje proveravaju limite i vraćaju stvarni damage ---

    public int onShopPurchase() {
        if (shopActions >= MAX_SHOP_ACTIONS) return 0;
        shopActions++;
        hpReduced += SHOP_DAMAGE;
        return SHOP_DAMAGE;
    }

    public int onBossHit() {
        if (bossHits >= MAX_BOSS_HITS) return 0;
        bossHits++;
        hpReduced += BOSS_HIT_DAMAGE;
        return BOSS_HIT_DAMAGE;
    }

    public int onEasyTaskSolved(boolean isNormalOrImportant) {
        if (easyTasks >= MAX_EASY_TASKS) return 0;
        easyTasks++;
        int damage = EASY_TASK_DAMAGE;
        if (isNormalOrImportant) damage *= 2; // normalan ili važan zadatak
        hpReduced += damage;
        return damage;
    }

    public int onHardTaskSolved() {
        if (hardTasks >= MAX_HARD_TASKS) return 0;
        hardTasks++;
        hpReduced += HARD_TASK_DAMAGE;
        return HARD_TASK_DAMAGE;
    }

    public int onNoFailedTasks() {
        if (noFailedTasks) return 0;
        noFailedTasks = true;
        hpReduced += NO_FAILED_TASKS_DAMAGE;
        return NO_FAILED_TASKS_DAMAGE;
    }

    public int onMessageSent() {
        if (messageDays >= MAX_MESSAGE_DAYS) return 0;
        messageDays++;
        hpReduced += MESSAGE_DAY_DAMAGE;
        return MESSAGE_DAY_DAMAGE;
    }

    /** Dodaje ukupni damage (ako dolazi spolja iz AllianceMission.applyDamage) */
    public void addDamage(int damage) {
        this.hpReduced += damage;
    }

    // --- Getteri ---
    public String getUserId() { return userId; }
    public int getHpReduced() { return hpReduced; }
}
