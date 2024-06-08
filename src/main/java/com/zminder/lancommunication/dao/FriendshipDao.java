package com.zminder.lancommunication.dao;

import com.zminder.lancommunication.pojo.Friendship;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.utils.DbHelper;
import org.apache.commons.dbutils.handlers.ScalarHandler;

import java.sql.SQLException;
import java.util.List;

public class FriendshipDao {

    public List<User> getFriendsByUserId(int userId) throws SQLException {
        String sql = "SELECT u.* FROM user u JOIN friendships f ON " +
                "(u.user_id = f.friend_id OR u.user_id = f.user_id) AND u.user_id != ? " +
                "WHERE (f.user_id = ? OR f.friend_id = ?) AND f.status = 'accepted'";
        return DbHelper.queryList(sql, User.class, userId, userId, userId);
    }

    public boolean addFriend(int userId, int friendId) throws SQLException {
        String sql = "INSERT INTO friendships (user_id, friend_id, status) VALUES (?, ?, 'pending')";
        int affectedRows = DbHelper.update(sql, userId, friendId);
        return affectedRows > 0;
    }

    public boolean updateFriendshipStatus(int userId, int friendId, String status) throws SQLException {
        String sql = "UPDATE friendships SET status = ? WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        int affectedRows = DbHelper.update(sql, status, userId, friendId, friendId, userId);
        return affectedRows > 0;
    }

    public boolean removeFriend(int userId, int friendId) throws SQLException {
        String sql = "DELETE FROM friendships WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        int affectedRows = DbHelper.update(sql, userId, friendId, friendId, userId);
        return affectedRows > 0;
    }

    public boolean existsFriendship(int userId, int friendId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM friendships " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";
        long count = DbHelper.query(sql, new ScalarHandler<Long>(), userId, friendId, friendId, userId);
        return count > 0;
    }
}
