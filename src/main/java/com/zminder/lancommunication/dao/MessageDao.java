package com.zminder.lancommunication.dao;

import com.zminder.lancommunication.pojo.Message;
import com.zminder.lancommunication.utils.DbHelper;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

public class MessageDao {

    // 发送消息
    public boolean sendMessage(Message message) {
        String sql = "INSERT INTO message (sender_id, receiver_id, group_id, message) VALUES (?, ?, ?, ?)";
        try {
            return DbHelper.update(sql, message.getSenderId(), message.getReceiverId(), message.getGroupId(), message.getMessage()) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 获取某个用户的所有私聊消息
    public List<Message> getPrivateMessagesForUser(int userId) {
        String sql = "SELECT message_id AS messageId, sender_id AS senderId, receiver_id AS receiverId, group_id AS groupId, message, timestamp " +
                "FROM message WHERE receiver_id = ? AND group_id IS NULL ORDER BY timestamp";
        try {
            return DbHelper.queryList(sql, Message.class, userId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取某个群组的所有消息
    public List<Message> getGroupMessages(int groupId) {
        String sql = "SELECT message_id AS messageId, sender_id AS senderId, receiver_id AS receiverId, group_id AS groupId, message, timestamp " +
                "FROM message WHERE group_id = ? ORDER BY timestamp";
        try {
            return DbHelper.queryList(sql, Message.class, groupId);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取两个用户之间的所有消息
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        String sql = "SELECT message_id AS messageId, sender_id AS senderId, receiver_id AS receiverId, group_id AS groupId, message, timestamp " +
                "FROM message WHERE " +
                "(sender_id = ? AND receiver_id = ?) OR " +
                "(sender_id = ? AND receiver_id = ?) AND group_id IS NULL ORDER BY timestamp";
        try {
            return DbHelper.queryList(sql, Message.class, userId1, userId2, userId2, userId1);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 获取某个群组中从某个时间开始的所有消息
    public List<Message> getGroupMessagesFromTimestamp(int groupId, Timestamp timestamp) {
        String sql = "SELECT message_id AS messageId, sender_id AS senderId, receiver_id AS receiverId, group_id AS groupId, message, timestamp " +
                "FROM message WHERE group_id = ? AND timestamp >= ? ORDER BY timestamp";
        try {
            return DbHelper.queryList(sql, Message.class, groupId, timestamp);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    // 根据消息ID删除消息
    public boolean deleteMessage(int messageId) {
        String sql = "DELETE FROM message WHERE message_id = ?";
        try {
            return DbHelper.update(sql, messageId) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
