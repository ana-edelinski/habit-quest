package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {
    private String id;
    private Long userId;        // lokalni user ID (SQLite)
    private String firebaseUid; // za Firestore
    private String categoryId;

    private String name;
    private String description;

    // Za jednokratne zadatke
    private Long date;

    // Za ponavljajuće
    private Long startDate;
    private Long endDate;
    private Integer interval;
    private String unit; // "day" / "week"

    // XP vrednosti
    private int difficultyXp;   // umesto weight
    private int importanceXp;
    private int totalXp;

    private TaskStatus status;

    public Task() { }

    public Task(String id, Long userId, String firebaseUid, String categoryId,
                String name, String description, Long date,
                Long startDate, Long endDate, Integer interval, String unit,
                int difficultyXp, int importanceXp, int totalXp, TaskStatus status) {
        this.id = id;
        this.userId = userId;
        this.firebaseUid = firebaseUid;
        this.categoryId = categoryId;
        this.name = name;
        this.description = description;
        this.date = date;
        this.startDate = startDate;
        this.endDate = endDate;
        this.interval = interval;
        this.unit = unit;
        this.difficultyXp = difficultyXp;
        this.importanceXp = importanceXp;
        this.totalXp = totalXp;
        this.status = status;
    }


    protected Task(Parcel in) {
        id = in.readByte() == 0 ? null : in.readString();
        userId = in.readByte() == 0 ? null : in.readLong();
        firebaseUid = in.readString();
        categoryId = in.readByte() == 0 ? null : in.readString();
        name = in.readString();
        description = in.readString();
        date = in.readByte() == 0 ? null : in.readLong();
        startDate = in.readByte() == 0 ? null : in.readLong();
        endDate = in.readByte() == 0 ? null : in.readLong();
        interval = in.readByte() == 0 ? null : in.readInt();
        unit = in.readString();
        difficultyXp = in.readInt();
        importanceXp = in.readInt();
        totalXp = in.readInt();
        String statusName = in.readString();
        status = statusName != null ? TaskStatus.valueOf(statusName) : null;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(id);
        }

        if (userId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(userId);
        }

        dest.writeString(firebaseUid);

        if (categoryId == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeString(categoryId);
        }

        dest.writeString(name);
        dest.writeString(description);

        if (date == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(date);
        }

        if (startDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(startDate);
        }

        if (endDate == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(endDate);
        }

        if (interval == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(interval);
        }

        dest.writeString(unit);
        dest.writeInt(difficultyXp);
        dest.writeInt(importanceXp);
        dest.writeInt(totalXp);
        dest.writeString(status != null ? status.name() : null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };


    public boolean isRecurring() {
        boolean hasRecurringFields = startDate != null && endDate != null && interval != null && unit != null;
        boolean hasOneTimeField = date != null;

        if (hasRecurringFields && !hasOneTimeField) {
            return true;
        } else if (!hasRecurringFields && hasOneTimeField) {
            return false;
        } else {
            throw new IllegalStateException("Task is not well-defined: either set one-time date OR recurring fields, not both.");
        }
    }


    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Integer getInterval() {
        return interval;
    }

    public void setInterval(Integer interval) {
        this.interval = interval;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public int getDifficultyXp() {
        return difficultyXp;
    }

    public void setDifficultyXp(int difficultyXp) {
        this.difficultyXp = difficultyXp;
    }

    public int getImportanceXp() {
        return importanceXp;
    }

    public void setImportanceXp(int importanceXp) {
        this.importanceXp = importanceXp;
    }

    public int getTotalXp() {
        return totalXp;
    }

    public void setTotalXp(int totalXp) {
        this.totalXp = totalXp;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}

