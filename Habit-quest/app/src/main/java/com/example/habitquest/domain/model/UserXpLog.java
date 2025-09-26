package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;
public class UserXpLog implements Parcelable {
    private Long id;            // primarni ključ u lokalnoj bazi
    private Long userId;        // FK → User (lokalni ID korisnika)
    private Long taskId;        // FK → Task (uvek postoji, čak i za occurrence)
    private Long occurrenceId;  // FK → TaskOccurrence (može biti null za one-time zadatke)

    private int xpGained;       // XP dobijen završavanjem zadatka
    private long completedAt;   // epoch millis kada je korisnik označio zadatak kao urađen
    private String firebaseUid;

    public UserXpLog() {}

    public UserXpLog(Long id, Long userId,String firebaseUid, Long taskId, Long occurrenceId,
                     int xpGained, long completedAt) {
        this.id = id;
        this.userId = userId;
        this.taskId = taskId;
        this.occurrenceId = occurrenceId;
        this.xpGained = xpGained;
        this.completedAt = completedAt;
        this.firebaseUid = firebaseUid;
    }

    // --- GETTERI & SETTERI ---
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getFirebaseUid() { return firebaseUid; }
    public void setFirebaseUid(String firebaseUid) { this.firebaseUid = firebaseUid; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getOccurrenceId() { return occurrenceId; }
    public void setOccurrenceId(Long occurrenceId) { this.occurrenceId = occurrenceId; }

    public int getXpGained() { return xpGained; }
    public void setXpGained(int xpGained) { this.xpGained = xpGained; }

    public long getCompletedAt() { return completedAt; }
    public void setCompletedAt(long completedAt) { this.completedAt = completedAt; }

    // --- Parcelable implementacija ---
    protected UserXpLog(Parcel in) {
        if (in.readByte() == 0) { id = null; } else { id = in.readLong(); }
        if (in.readByte() == 0) { userId = null; } else { userId = in.readLong(); }
        if (in.readByte() == 0) { taskId = null; } else { taskId = in.readLong(); }
        if (in.readByte() == 0) { occurrenceId = null; } else { occurrenceId = in.readLong(); }
        xpGained = in.readInt();
        completedAt = in.readLong();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeLong(id); }
        if (userId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeLong(userId); }
        if (taskId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeLong(taskId); }
        if (occurrenceId == null) { dest.writeByte((byte) 0); } else { dest.writeByte((byte) 1); dest.writeLong(occurrenceId); }
        dest.writeInt(xpGained);
        dest.writeLong(completedAt);
    }

    @Override
    public int describeContents() { return 0; }

    public static final Creator<UserXpLog> CREATOR = new Creator<UserXpLog>() {
        @Override
        public UserXpLog createFromParcel(Parcel in) { return new UserXpLog(in); }
        @Override
        public UserXpLog[] newArray(int size) { return new UserXpLog[size]; }
    };
}
