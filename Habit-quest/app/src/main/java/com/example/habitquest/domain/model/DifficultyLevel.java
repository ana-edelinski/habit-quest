package com.example.habitquest.domain.model;

public enum DifficultyLevel {
    VERY_EASY("Very easy", 1),
    EASY("Easy", 3),
    HARD("Hard", 7),
    EXTREME("Very hard", 20);

    public final String label;
    public final int xp;
    DifficultyLevel(String label, int xp) { this.label = label; this.xp = xp; }

    @Override public String toString() { return label; }
}
