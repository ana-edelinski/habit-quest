package com.example.habitquest.domain.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TaskOccurrence  implements Parcelable {
    private String id;           // Firestore ID za occurrence dokument
    private String taskId;       // parent task ID
    private TaskStatus status;   // koristi isti enum kao parent Task
    private Long date;  // datum (i vreme ako treba) kada se occurrence dešava
    private String firebaseUid;

    public TaskOccurrence() {}

    public TaskOccurrence(TaskStatus status, Long date, String taskId, String id, String firebaseUid) {
        this.status = status;
        this.date = date;
        this.taskId = taskId;
        this.id = id;
        this.firebaseUid = firebaseUid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    protected TaskOccurrence(Parcel in) {
        id = in.readString();
        taskId = in.readString();
        status = TaskStatus.valueOf(in.readString());
        if (in.readByte() == 0) {
            date = null;
        } else {
            date = in.readLong();
        }
    }

    public static final Creator<TaskOccurrence> CREATOR = new Creator<TaskOccurrence>() {
        @Override
        public TaskOccurrence createFromParcel(Parcel in) {
            return new TaskOccurrence(in);
        }

        @Override
        public TaskOccurrence[] newArray(int size) {
            return new TaskOccurrence[size];
        }
    };

    @Override
    public int describeContents() { return 0; }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(taskId);
        dest.writeString(status.name());
        if (date == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(date);
        }
    }
}

