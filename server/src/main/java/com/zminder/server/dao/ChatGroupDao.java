package com.zminder.server.dao;

import com.zminder.server.pojo.ChatGroup;
import com.zminder.server.utils.DbHelper;

import java.sql.SQLException;
import java.util.List;

public class ChatGroupDao {

    // 添加新的群组
    public boolean addChatGroup(ChatGroup group) {
        String sql = "INSERT INTO chat_group (group_name, owner_id) VALUES (?, ?)";
        try {
            return DbHelper.update(sql, group.getGroupName(), group.getOwnerId()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 根据群组ID获取群组信息
    public ChatGroup getChatGroupById(int groupId) {
        String sql = "SELECT group_id AS groupId, group_name AS groupName, owner_id AS ownerId, created_at AS createdAt " +
                "FROM chat_group WHERE group_id = ?";
        try {
            return DbHelper.query(sql, ChatGroup.class, groupId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据群组名称获取群组信息（模糊查询）
    public List<ChatGroup> getChatGroupByName(String groupName) {
        String sql = "SELECT group_id AS groupId, group_name AS groupName, owner_id AS ownerId, created_at AS createdAt " +
                "FROM chat_group WHERE group_name LIKE ?";
        try {
            return DbHelper.queryList(sql, ChatGroup.class, "%" + groupName + "%");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取所有群组
    public List<ChatGroup> getAllChatGroups() {
        String sql = "SELECT group_id AS groupId, group_name AS groupName, owner_id AS ownerId, created_at AS createdAt " +
                "FROM chat_group";
        try {
            return DbHelper.queryList(sql, ChatGroup.class);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 更新群组信息
    public boolean updateChatGroup(ChatGroup group) {
        String sql = "UPDATE chat_group SET group_name = ?, owner_id = ? WHERE group_id = ?";
        try {
            return DbHelper.update(sql, group.getGroupName(), group.getOwnerId(), group.getGroupId()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除群组
    public boolean deleteChatGroup(int groupId) {
        String sql = "DELETE FROM chat_group WHERE group_id = ?";
        try {
            return DbHelper.update(sql, groupId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
