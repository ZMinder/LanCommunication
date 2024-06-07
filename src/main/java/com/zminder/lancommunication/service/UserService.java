package com.zminder.lancommunication.service;

import com.zminder.lancommunication.dao.UserDao;
import com.zminder.lancommunication.pojo.User;

public class UserService {

    private UserDao userDao = new UserDao();

    // 注册用户
    public boolean registerUser(String username, String passwordHash) {
        // 先检查用户名是否已被占用
        if (userDao.isUsernameTaken(username)) {
            System.out.println("Username already taken.");
            return false;
        } else {
            // 添加新用户到数据库
            return userDao.registerUser(username, passwordHash);
        }
    }

    // 用户登录
    public User loginUser(String username, String passwordHash) {
        return userDao.loginUser(username, passwordHash);
    }

    // 用户登出
    public void logoutUser(int userId) {
        // 更新用户的在线状态为离线
        userDao.logoutUser(userId);
    }

    // 获取用户信息
    public User getUserByUsername(String username) {
        return userDao.getUserByUsername(username);
    }

    // 更新用户状态
    public boolean updateUserOnlineStatus(int userId, boolean isOnline) {
        return userDao.updateOnlineStatus(userId, isOnline);
    }
}
