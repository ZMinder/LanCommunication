package com.zminder.lancommunication.service;

import com.zminder.lancommunication.dao.ChatGroupDao;
import com.zminder.lancommunication.dao.GroupMembersDao;
import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.User;

import java.util.List;

public class ChatGroupService {

    private ChatGroupDao chatGroupDao = new ChatGroupDao();
    private GroupMembersDao groupMembersDao = new GroupMembersDao();

    // 创建群组
    public boolean createGroup(String groupName, int ownerId) {
        ChatGroup group = new ChatGroup();
        group.setGroupName(groupName);
        group.setOwnerId(ownerId);

        boolean groupCreated = chatGroupDao.addChatGroup(group);
        if (groupCreated) {
            return groupMembersDao.addMemberToGroup(group.getGroupId(), ownerId, "owner");
        }
        return false;
    }

    // 获取群组信息
    public ChatGroup getGroupById(int groupId) {
        return chatGroupDao.getChatGroupById(groupId);
    }

    // 根据群组名称查询（模糊查询）
    public List<ChatGroup> getGroupByName(String groupName) {
        return chatGroupDao.getChatGroupByName(groupName);
    }

    // 获取所有群组
    public List<ChatGroup> getAllGroups() {
        return chatGroupDao.getAllChatGroups();
    }

    // 添加成员到群组
    public boolean addMemberToGroup(int groupId, int userId) {
        return groupMembersDao.addMemberToGroup(groupId, userId, "member");
    }

    // 获取群组成员
    public List<User> getGroupMembers(int groupId) {
        return groupMembersDao.getGroupMembers(groupId);
    }
}
