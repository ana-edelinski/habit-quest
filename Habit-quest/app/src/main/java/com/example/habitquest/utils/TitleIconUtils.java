package com.example.habitquest.utils;

import com.example.habitquest.R;

public class TitleIconUtils {
    public static int getIconForLevel(int level) {
        switch (level) {
            case 0: return R.drawable.ic_beginner;
            case 1: return R.drawable.ic_adventurer;
            case 2: return R.drawable.ic_champion;
            case 3: return R.drawable.ic_master;
            default: return R.drawable.ic_legend;
        }
    }
}
