package com.zminder.server.dao;

import com.zminder.server.pojo.ChatGroup;
import com.zminder.server.pojo.User;
import com.zminder.server.utils.DbHelper;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class GroupMemberDao {

    // 添加成员到群组
    public boolean addMemberToGroup(int groupId, int userId, String role) {
        String sql = "INSERT INTO group_members (group_id, user_id, role) VALUES (?, ?, ?)";
        try {
            return DbHelper.update(sql, groupId, userId, role) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 从群组中移除成员
    public boolean removeMemberFromGroup(int groupId, int userId) {
        String sql = "DELETE FROM group_members WHERE group_id = ? AND user_id = ?";
        try {
            return DbHelper.update(sql, groupId, userId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 查询用户所属群组
    public List<ChatGroup> getGroupsByUserId(int userId) {
        String sql = "SELECT g.group_id AS groupId, g.group_name AS groupName, g.owner_id AS ownerId, g.created_at AS createdAt " +
                "FROM chat_group g " +
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
        String sql = "SELECT u.user_id AS userId, u.username, u.password_hash AS passwordHash, u.created_at AS createdAt, u.is_online AS isOnline " +
                "FROM user u " +
                "JOIN group_members gm ON u.user_id = gm.user_id " +
                "WHERE gm.group_id = ?";
        try {
            return DbHelper.queryList(sql, User.class, groupId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 检查用户是否在某个群组中
    public boolean isUserInGroup(int groupId, int userId) {
        String sql = "SELECT COUNT(*) FROM group_members WHERE group_id = ? AND user_id = ?";
        try {
            long count = DbHelper.query(sql, new ScalarHandler<Long>(), groupId, userId);
            return count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}

