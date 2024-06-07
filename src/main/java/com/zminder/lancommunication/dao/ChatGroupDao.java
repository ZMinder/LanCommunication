package com.zminder.lancommunication.dao;

import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.utils.DbHelper;

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
        String sql = "SELECT * FROM chat_group WHERE group_id = ?";
        try {
            return DbHelper.query(sql, ChatGroup.class, groupId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据群组名称获取群组信息（模糊查询）
    public List<ChatGroup> getChatGroupByName(String groupName) {
        String sql = "SELECT * FROM chat_group WHERE group_name LIKE ?";
        try {
            return DbHelper.queryList(sql, ChatGroup.class, "%" + groupName + "%");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取所有群组
    public List<ChatGroup> getAllChatGroups() {
        String sql = "SELECT * FROM chat_group";
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

    // 获取某用户所属的群组
    public List<ChatGroup> getGroupsByUserId(int userId) {
        String sql = "SELECT g.* FROM chat_group g " +
                "JOIN group_members gm ON g.group_id = gm.group_id " +
                "WHERE gm.user_id = ?";
        try {
            return DbHelper.queryList(sql, ChatGroup.class, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取群组的所有成员
    public List<User> getGroupMembers(int groupId) {
        String sql = "SELECT u.* FROM user u " +
                "JOIN group_members gm ON u.user_id = gm.user_id " +
                "WHERE gm.group_id = ?";
        try {
            return DbHelper.queryList(sql, User.class, groupId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }
}
