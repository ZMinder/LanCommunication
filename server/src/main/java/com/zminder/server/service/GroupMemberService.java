package com.zminder.server.service;
import com.zminder.server.dao.GroupMemberDao;
import com.zminder.server.pojo.ChatGroup;
import com.zminder.server.pojo.User;

import java.util.List;

public class GroupMemberService {

    private GroupMemberDao groupMemberDao = new GroupMemberDao();

    // 添加成员到群组
    public boolean addMemberToGroup(int groupId, int userId, String role) {
        // 首先检查用户是否已经在群组中
        if (!groupMemberDao.isUserInGroup(groupId, userId)) {
            return groupMemberDao.addMemberToGroup(groupId, userId, role);
        }
        System.out.println("该成员已在本群内");
        return false;
    }

    // 从群组中移除成员
    public boolean removeMemberFromGroup(int groupId, int userId) {
        // 检查用户是否在群组中
        if (groupMemberDao.isUserInGroup(groupId, userId)) {
            return groupMemberDao.removeMemberFromGroup(groupId, userId);
        }
        System.out.println("该成员不在本群内");
        return false;
    }

    // 获取某用户所属的群组
    public List<ChatGroup> getGroupsByUserId(int userId) {
        return groupMemberDao.getGroupsByUserId(userId);
    }

    // 获取群组的所有成员
    public List<User> getGroupMembers(int groupId) {
        return groupMemberDao.getGroupMembers(groupId);
    }

    // 检查用户是否在某个群组中
    public boolean isUserInGroup(int groupId, int userId) {
        return groupMemberDao.isUserInGroup(groupId, userId);
    }
}
