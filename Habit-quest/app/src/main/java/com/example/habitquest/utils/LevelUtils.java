package com.example.habitquest.utils;

public class LevelUtils {

    // Vrati XP prag za zadati nivo
    public static int getXpThresholdForLevel(int level) {
        if (level == 0) {
            return 0; // start
        } else if (level == 1) {
            return 200; // prvi prag fiksno
        }

        int prevThreshold = getXpThresholdForLevel(level - 1);
        double raw = prevThreshold * 2 + prevThreshold / 2.0;

        // zaokruži na prvu narednu stotinu
        int rounded = ((int) Math.ceil(raw / 100.0)) * 100;
        return rounded;
    }

    // Izračunaj level korisnika na osnovu totalXp
    public static int calculateLevelFromXp(int totalXp) {
        int level = 0;
        while (true) {
            int nextThreshold = getXpThresholdForLevel(level + 1);
            if (totalXp >= nextThreshold) {
                level++;
            } else {
                break;
            }
        }
        return level;
    }
}
