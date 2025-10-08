package com.example.habitquest.domain.model;

public class AllianceMessage {
    private String id;
    private String senderId;
    private String senderName;
    private String text;
    private long timestamp;
    private int senderAvatar;


    public AllianceMessage() {}

    public AllianceMessage(String id, String senderId, String senderName, String text, long timestamp, int senderAvatar) {
        this.id = id;
        this.senderId = senderId;
        this.senderName = senderName;
        this.text = text;
        this.timestamp = timestamp;
        this.senderAvatar = senderAvatar;
    }

    public String getId() { return id; }
    public String getSenderId() { return senderId; }
    public String getSenderName() { return senderName; }
    public String getText() { return text; }
    public long getTimestamp() { return timestamp; }

    public void setId(String id) { this.id = id; }
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public int getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(int senderAvatar) { this.senderAvatar = senderAvatar; }
}
