package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    private Long id;
    private String email;
    private String username;
    //private String password;
    private int avatar; // samo indeks (1–5 za predefinisane slike)
    //private boolean isVerified;
    private int totalXp;
    private int level;
    private String title;

    public User(Long id, String email, String username, int avatar, int totalXp, int level, String title) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.avatar = avatar;
        this.totalXp = totalXp;
        this.level = level;
        this.title = title;
    }

    public User() {
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
    }

    // GET / SET
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar=" + avatar +
                ", totalXp=" + totalXp +
                ", level=" + level +
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
}
