package com.example.habitquest.utils;

public class XpCalculator {

    // skalira XP po formuli: XP = XP + XP/2, ponavljano po levelu
    public static int scaleXpByLevel(int baseXp, int level) {
        int xp = baseXp;
        for (int i = 0; i < level; i++) {
            xp = (int) Math.round(xp + xp / 2.0);
        }
        return xp;
    }

    public static int calculateTaskXp(int baseDifficultyXp, int baseImportanceXp, int userLevel) {
        int difficultyXp = scaleXpByLevel(baseDifficultyXp, userLevel);
        int importanceXp = scaleXpByLevel(baseImportanceXp, userLevel);
        return difficultyXp + importanceXp;
    }
}
