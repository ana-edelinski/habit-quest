package com.example.habitquest.utils;

public class ProgressPointsUtils {

    /**
     * Returns PP reward for reaching a specific level.
     * @param level target level (must be >= 1)
     */
    public static int getRewardForLevel(int level) {
        if (level == 1) return 40; // first level gives 40 PP

        int pp = 40;
        for (int i = 2; i <= level; i++) {
            pp = (int) Math.round(pp + (pp * 0.75)); // 1.75x previous
        }
        return pp;
    }

    /**
     * Returns the total PP accumulated up to a given level.
     * Example: if user is level 3, returns sum of rewards from level 1 + 2 + 3.
     */
    public static int getTotalPPForLevel(int level) {
        int total = 0;
        for (int i = 1; i <= level; i++) {
            total += getRewardForLevel(i);
        }
        return total;
    }
}
