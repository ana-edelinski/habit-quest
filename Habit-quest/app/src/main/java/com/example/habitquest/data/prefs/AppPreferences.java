package com.example.habitquest.data.prefs;

import android.content.Context;
import android.content.SharedPreferences;

public class AppPreferences {
    private static final String PREF_NAME = "HabitQuestPrefs";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_FIREBASE_UID = "firebaseUid";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";

    private final SharedPreferences prefs;

    public AppPreferences(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // User session
    public void saveUserSession(String userId, String firebaseUid) {
        prefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_FIREBASE_UID, firebaseUid)
                .putBoolean(KEY_IS_LOGGED_IN, true)
                .apply();
    }

    public void clearSession() {
        prefs.edit()
                .remove(KEY_USER_ID)
                .remove(KEY_FIREBASE_UID)
                .putBoolean(KEY_IS_LOGGED_IN, false)
                .apply();
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }
    public String getFirebaseUid() {
        return prefs.getString(KEY_FIREBASE_UID, null);
    }

    public boolean isLoggedIn() {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }
    public void saveUsername(String username) { prefs.edit().putString("username", username).apply(); }
    public String getUsername() { return prefs.getString("username", ""); }

    public void saveAvatarIndex(int index) { prefs.edit().putInt("avatar", index).apply(); }
    public int getAvatarIndex() { return prefs.getInt("avatar", 0); }

    public void setCurrentAllianceId(String id) {
        prefs.edit().putString("current_alliance_id", id).apply();
    }

    public String getCurrentAllianceId() {
        return prefs.getString("current_alliance_id", null);
    }

}

