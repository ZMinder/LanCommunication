package com.zminder.lancommunication.service;

import com.zminder.lancommunication.dao.FriendshipDao;
import com.zminder.lancommunication.pojo.User;

import java.sql.SQLException;
import java.util.List;

public class FriendshipService {
    private FriendshipDao friendshipDao = new FriendshipDao();

    public List<User> getFriendships(int userId) throws SQLException {
        return friendshipDao.getFriendsByUserId(userId);
    }

    public boolean sendFriendRequest(int userId, int friendId) throws SQLException {
        // 检查是否已存在好友请求或关系
        if (friendshipDao.existsFriendship(userId, friendId)) {
            return false;
        }
        return friendshipDao.addFriend(userId, friendId);
    }

    public boolean acceptFriendRequest(int userId, int friendId) throws SQLException {
        return friendshipDao.updateFriendshipStatus(friendId, userId, "accepted");
    }

    public boolean removeFriend(int userId, int friendId) throws SQLException {
        return friendshipDao.removeFriend(userId, friendId);
    }
}
