package com.example.habitquest.domain.model;

public enum ImportanceLevel {
    NORMAL("Normal", 1),
    IMPORTANT("Important", 3),
    EXTREME_IMPORTANT("Very important", 10);

    public final String label;
    public final int xp;
    ImportanceLevel(String label, int xp) { this.label = label; this.xp = xp; }
    @Override public String toString() { return label; }
}
