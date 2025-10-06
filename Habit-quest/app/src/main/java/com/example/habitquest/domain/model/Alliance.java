package com.example.habitquest.domain.model;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class Alliance {

    private String id;
    private String name;
    private String leaderId;
    private String leaderName;
    private List<String> members;
    private List<String> requests;
    private Timestamp createdAt;

    public Alliance() {}

    public Alliance(String name, String leaderId, String leaderName, List<String> requests) {
        this.name = name;
        this.leaderId = leaderId;
        this.leaderName = leaderName;
        this.members = new ArrayList<>();
        this.requests = new ArrayList<>(requests);
        this.members.add(leaderId);
        this.createdAt = Timestamp.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public String getLeaderName() {
        return leaderName;
    }

    public void setLeaderName(String leaderName) {
        this.leaderName = leaderName;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public List<String> getRequests() {
        return requests;
    }

    public void setRequests(List<String> requests) {
        this.requests = requests;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
