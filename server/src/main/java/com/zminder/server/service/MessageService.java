package com.zminder.server.service;

import com.zminder.server.dao.MessageDao;
import com.zminder.server.pojo.Message;

import java.sql.Timestamp;
import java.util.List;

public class MessageService {

    private MessageDao messageDao = new MessageDao();

    // 发送消息
    public boolean sendMessage(int senderId, Integer receiverId, Integer groupId, String messageContent) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setGroupId(groupId);
        message.setMessage(messageContent);
        return messageDao.sendMessage(message);
    }

    public boolean sendMessage(Message message) {
        return messageDao.sendMessage(message);
    }

    // 获取某个用户的所有私聊消息
    public List<Message> getPrivateMessagesForUser(int userId) {
        return messageDao.getPrivateMessagesForUser(userId);
    }

    // 获取某个群组的所有消息
    public List<Message> getGroupMessages(int groupId) {
        return messageDao.getGroupMessages(groupId);
    }

    // 获取两个用户之间的所有消息
    public List<Message> getMessagesBetweenUsers(int userId1, int userId2) {
        return messageDao.getMessagesBetweenUsers(userId1, userId2);
    }

    // 获取某个群组中从某个时间开始的所有消息
    public List<Message> getGroupMessagesFromTimestamp(int groupId, Timestamp timestamp) {
        return messageDao.getGroupMessagesFromTimestamp(groupId, timestamp);
    }

    // 根据消息ID删除消息
    public boolean deleteMessage(int messageId) {
        return messageDao.deleteMessage(messageId);
    }
}
