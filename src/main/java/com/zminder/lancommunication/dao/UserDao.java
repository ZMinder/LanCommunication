package com.zminder.lancommunication.dao;

import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.utils.DbHelper;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;

public class UserDao {
    // 注册用户
    public boolean registerUser(String username, String passwordHash) {
        String sql = "INSERT INTO users (username, password_hash) VALUES (?, ?)";
        try {
            int rows = DbHelper.update(sql, username, passwordHash);
            return rows == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 用户登录
    public User loginUser(String username, String passwordHash) {
        String sql = "SELECT * FROM users WHERE username = ? AND password_hash = ?";
        try {
            return DbHelper.query(sql, User.class, username, passwordHash);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查询用户
    public User getUserByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";
        try {
            return DbHelper.query(sql, User.class, username);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 查询用户是否存在
    public boolean isUsernameTaken(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try {
            Long count = DbHelper.query(sql, new ScalarHandler<>(), username);
            return count != null && count > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
