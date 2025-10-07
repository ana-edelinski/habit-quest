package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    private Long id;
    private String uid;
    private String email;
    private String username;
    //private String password;
    private int avatar; // samo indeks (1–5 za predefinisane slike)
    //private boolean isVerified;
    private int totalXp;
    private int level;
    private String title;
    private int pp;
    private int coins;
    private int bossesDefeated;
    private List<ShopItem> cart = new ArrayList<>();
    private List<ShopItem> equipment = new ArrayList<>();
    private List<String> friends = new ArrayList<>();
    private List<String> friendRequestsSent = new ArrayList<>();
    private List<String> friendRequestsReceived = new ArrayList<>();
    private String allianceId;


    public User(Long id, String email, String username, int avatar, int totalXp, int level, String title, int pp, int coins, int bossesDefeated) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.totalXp = totalXp;
        this.level = level;
        this.title = title;
        this.pp = pp;
        this.coins = coins;
        this.bossesDefeated = bossesDefeated;
        this.cart = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.friendRequestsSent = new ArrayList<>();
        this.friendRequestsReceived = new ArrayList<>();
    }

    public User() {
        this.cart = new ArrayList<>();
        this.equipment = new ArrayList<>();
        this.friends = new ArrayList<>();
        this.friendRequestsSent = new ArrayList<>();
        this.friendRequestsReceived = new ArrayList<>();
        this.coins = 0;
        this.pp = 0;
        this.bossesDefeated = 0;
        this.allianceId = null;
    }

    // Konstruktor za citanje iz Parcel objekta
    protected User(Parcel in) {
        id = in.readLong();
        email = in.readString();
        username = in.readString();
        avatar = in.readInt();
        totalXp = in.readInt();
        level = in.readInt();
        title = in.readString();
        pp = in.readInt();
        coins = in.readInt();
        bossesDefeated = in.readInt();
        cart = new ArrayList<>();
        in.readList(cart, ShopItem.class.getClassLoader());
        equipment = new ArrayList<>();
        in.readList(equipment, ShopItem.class.getClassLoader());
        allianceId = in.readString();
    }

    // GET / SET
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getAvatar() { return avatar; }
    public void setAvatar(int avatar) { this.avatar = avatar; }

    public int getTotalXp() { return totalXp; }
    public void setTotalXp(int totalXp) { this.totalXp = totalXp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public int getPp() { return pp; }
    public void setPp(int pp) { this.pp = pp; }

    public int getCoins() { return coins; }
    public void setCoins(int coins) { this.coins = coins; }

    public List<ShopItem> getCart() { return cart; }
    public void setCart(List<ShopItem> cart) { this.cart = cart; }

    public List<ShopItem> getEquipment() { return equipment; }
    public void setEquipment(List<ShopItem> equipment) { this.equipment = equipment; }

    public int getBossesDefeated() { return bossesDefeated; }
    public void setBossesDefeated(int bossesDefeated) { this.bossesDefeated = bossesDefeated; }

    public List<String> getFriends() {
        return friends;
    }
    public void setFriends(List<String> friends) {
        this.friends = friends;
    }

    public List<String> getFriendRequestsSent() { return friendRequestsSent; }
    public void setFriendRequestsSent(List<String> friendRequestsSent) { this.friendRequestsSent = friendRequestsSent; }

    public List<String> getFriendRequestsReceived() { return friendRequestsReceived; }
    public void setFriendRequestsReceived(List<String> friendRequestsReceived) { this.friendRequestsReceived = friendRequestsReceived; }

    public String getAllianceId() {
        return allianceId;
    }

    public void setAllianceId(String allianceId) {
        this.allianceId = allianceId;
    }


    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar=" + avatar +
                ", totalXp=" + totalXp +
                ", level=" + level +
                ", pp=" + pp +
                '}';
    }

    // Parcelable implementacija
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id != null ? id : -1);
        dest.writeString(email);
        dest.writeString(username);
        dest.writeInt(avatar);
        dest.writeInt(totalXp);
        dest.writeInt(level);
        dest.writeString(title);
        dest.writeInt(pp);
        dest.writeInt(coins);
        dest.writeList(cart);
        dest.writeList(equipment);
        dest.writeInt(bossesDefeated);
        dest.writeString(allianceId);
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) {
            return new User(in);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };

    public int getPreviousBossReward() {
        return calculateBossReward(bossesDefeated);
    }

    private int calculateBossReward(int defeatedCount) {
        if (defeatedCount <= 0) return 0;
        int reward = 200; // reward prvog bossa
        for (int i = 1; i < defeatedCount; i++) {
            reward = (int) Math.round(reward * 1.2);
        }
        return reward;
    }
}
