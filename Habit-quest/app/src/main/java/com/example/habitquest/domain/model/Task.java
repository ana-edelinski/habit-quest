package com.example.habitquest.domain.model;

public class Task {
    private Long id;
    private Long userId;        // lokalni user ID (SQLite)
    private String firebaseUid; // za Firestore
    private Long categoryId;

    private String name;
    private String description;

    // Za jednokratne zadatke
    private Long date;

    // Za ponavljajuće
    private Long startDate;
    private Long endDate;
    private Integer interval;
    private String unit; // "day" / "week"

    // XP vrednosti
    private int difficultyXp;   // umesto weight
    private int importanceXp;
    private int totalXp;

    private boolean completed;

    public Task() { }

    public Task(Long id, Long userId, String firebaseUid, Long categoryId,
                String name, String description, Long date,
                Long startDate, Long endDate, Integer interval, String unit,
                int difficultyXp, int importanceXp, int totalXp, boolean completed) {
        this.id = id;
        this.userId = userId;
        this.firebaseUid = firebaseUid;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interval = interval;
        this.unit = unit;
        this.difficultyXp = difficultyXp;
        this.importanceXp = importanceXp;
        this.totalXp = totalXp;
        this.completed = completed;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDifficultyXp() {
        return difficultyXp;
    }

    public void setDifficultyXp(int difficultyXp) {
        this.difficultyXp = difficultyXp;
    }

    public int getImportanceXp() {
        return importanceXp;
    }

    public void setImportanceXp(int importanceXp) {
        this.importanceXp = importanceXp;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}

