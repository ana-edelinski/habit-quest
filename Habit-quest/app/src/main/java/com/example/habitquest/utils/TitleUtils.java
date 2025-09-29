package com.example.habitquest.utils;

public class TitleUtils {
    public static String getTitleForLevel(int level) {
        switch (level) {
            case 0: return "Beginner";
            case 1: return "Adventurer";
            case 2: return "Champion";
            case 3: return "Master";
            default: return "Legend";
        }
    }
}
