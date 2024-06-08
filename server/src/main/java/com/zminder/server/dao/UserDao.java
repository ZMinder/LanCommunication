package com.zminder.server.dao;

import com.zminder.server.pojo.User;
import com.zminder.server.utils.DbHelper;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;

public class UserDao {
    // 注册用户
    public boolean registerUser(String username, String passwordHash) {
        String sql = "INSERT INTO user (username, password_hash) VALUES (?, ?)";
        try {
            int rows = DbHelper.update(sql, username, passwordHash);
            return rows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 用户登录时更新在线状态
    public User loginUser(String username, String passwordHash) {
        String sql = "SELECT user_id AS userId, username, password_hash AS passwordHash, created_at AS createdAt, is_online AS isOnline " +
                "FROM user WHERE username = ? AND password_hash = ?";
        try {
            User user = DbHelper.query(sql, User.class, username, passwordHash);
            if (user != null) {
                updateOnlineStatus(user.getUserId(), true);  // 设置用户在线
            }
            return user;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查询用户
    public User getUserByUsername(String username) {
        String sql = "SELECT user_id AS userId, username, password_hash AS passwordHash, created_at AS createdAt, is_online AS isOnline " +
                "FROM user WHERE username = ?";
        try {
            return DbHelper.query(sql, User.class, username);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据用户ID获取用户信息
    public User getUserById(int userId) {
        String sql = "SELECT user_id AS userId, username, password_hash AS passwordHash, created_at AS createdAt, is_online AS isOnline " +
                "FROM user WHERE user_id = ?";
        try {
            return DbHelper.query(sql, User.class, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查询用户是否存在
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username = ?";
        try {
            Long count = DbHelper.query(sql, new ScalarHandler<>(), username);
            return count != null && count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 更新用户在线状态
    public boolean updateOnlineStatus(int userId, boolean isOnline) {
        String sql = "UPDATE user SET is_online = ? WHERE user_id = ?";
        try {
            int rows = DbHelper.update(sql, isOnline, userId);
            return rows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 用户登出或断线时调用
    public void logoutUser(int userId) {
        updateOnlineStatus(userId, false);
    }
}
