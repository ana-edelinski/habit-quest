package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class User implements Parcelable {
    private Long id;
    private String email;
    private String username;
    private String password;
    private int avatar; // samo indeks (1–5 za predefinisane slike)
    private boolean isVerified;

    public User(Long id, String email, String username, String password, int avatar, boolean isVerified) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
        this.avatar = avatar;
        this.isVerified = isVerified;
    }

    public User() {
    }

    // Konstruktor za citanje iz Parcel objekta
    protected User(Parcel in) {
        id = in.readLong();
        email = in.readString();
        username = in.readString();
        password = in.readString();
        avatar = in.readInt();
        isVerified = in.readByte() != 0; //boolean se cita kao byte
    }

    // GET / SET
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getAvatar() { return avatar; }
    public void setAvatar(int avatar) { this.avatar = avatar; }
    public boolean isVerified() { return isVerified; }
    public void setVerified(boolean verified) { isVerified = verified; }

    @Override
    public String toString() {
        return "User{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                ", avatar=" + avatar +
                ", verified=" + isVerified +
                '}';
    }

    // Parcelable implementacija
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeLong(id != null ? id : -1); // ako je null, snimi -1
        dest.writeString(email);
        dest.writeString(username);
        dest.writeString(password);
        dest.writeInt(avatar);
        dest.writeByte((byte) (isVerified ? 1 : 0));
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
