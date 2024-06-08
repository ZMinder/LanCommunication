package com.zminder.lancommunication.pojo;

import java.sql.Timestamp;

public class Message {
    private int messageId;
    private int senderId;
    private Integer receiverId; // Nullable
    private Integer groupId;    // Nullable
    private String message;
    private Timestamp timestamp;

    public Message() {
    }

    public Message( int senderId, Integer receiverId, Integer groupId, String message) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.groupId = groupId;
        this.message = message;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public Integer getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Integer receiverId) {
        this.receiverId = receiverId;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public java.sql.Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(java.sql.Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
