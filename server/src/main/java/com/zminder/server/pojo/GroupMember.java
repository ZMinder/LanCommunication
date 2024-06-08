package com.zminder.server.pojo;

public class GroupMember {
    private int groupId;
    private int userId;
    private String role;

    // 无参构造函数
    public GroupMember() {
    }

    // 带参数的构造函数
    public GroupMember(int groupId, int userId, String role) {
        this.groupId = groupId;
        this.userId = userId;
        this.role = role;
    }

    // Getter和Setter方法
    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
