package com.zminder.client.pojo;

import java.sql.Timestamp;

public class ChatGroup {
    private int groupId;
    private String groupName;
    private int ownerId;
    private Timestamp createdAt;

    public ChatGroup() {
    }

    public ChatGroup(int groupId, String groupName, int ownerId, Timestamp createdAt) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.ownerId = ownerId;
        this.createdAt = createdAt;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(int ownerId) {
        this.ownerId = ownerId;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
