package com.zminder.lancommunication.frame;

import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.Message;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ChatPanel extends JPanel {
    private MainFrame mainFrame;
    private User currentUser;
    private JTextArea chatArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(40);
    private JButton sendButton = new JButton("发送");

    private JList<String> friendList = new JList<>();
    private DefaultListModel<String> friendListModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();

    private UserService userService = new UserService();
    private ChatGroupService chatGroupService = new ChatGroupService();
    private MessageService messageService = new MessageService();
    private FriendshipService friendshipService = new FriendshipService();
    private GroupMemberService groupMemberService = new GroupMemberService();

    public ChatPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setupUI();
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        loadInitialData();
    }

    private void setupUI() {
        //设置整体布局为BorderLayout
        setLayout(new BorderLayout());

        //聊天历史界面放置在CENTER
        add(new JScrollPane(chatArea), BorderLayout.CENTER);

        //处理输入区域的布局
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);
        add(inputPanel, BorderLayout.SOUTH);

        //使用JTabbedPane配置好友列表和群组列表
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("好友列表", new JScrollPane(friendList));
        tabbedPane.addTab("群组列表", new JScrollPane(groupList));
        add(tabbedPane, BorderLayout.WEST);

        //设置监听器
        sendButton.addActionListener(this::handleSendMessage);

        // 添加选项卡切换监听器
        tabbedPane.addChangeListener(e -> {
            chatArea.setText(""); // 清空聊天区域
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) { // 选择好友列表选项卡
                if (friendList.getModel().getSize() > 0) {
                    friendList.clearSelection();
                    friendList.setSelectedIndex(0); // 强制触发好友列表选择事件
                }
            } else if (selectedIndex == 1) { // 选择群组列表选项卡
                if (groupList.getModel().getSize() > 0) {
                    groupList.clearSelection();
                    groupList.setSelectedIndex(0); // 强制触发群组列表选择事件
                }
            }
        });

        // 好友列表选择监听器
        friendList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                groupList.clearSelection(); // 清空群组列表的选中状态
                loadFriendChatHistory(); // 加载好友聊天记录
            }
        });

        // 群组列表选择监听器
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                friendList.clearSelection(); // 清空好友列表的选中状态
                loadGroupChatHistory(); // 加载群组聊天记录
            }
        });
    }

    private void loadInitialData() {//加载好友列表和群组列表
        if (currentUser == null) return;

        System.out.println("load");
        System.out.println(currentUser.getUserId());
        //加载群组列表
        List<ChatGroup> groups = groupMemberService.getGroupsByUserId(currentUser.getUserId());
        System.out.println(groups);
        groups.forEach(group -> groupListModel.addElement(group.getGroupName()));

        //加载好友列表
        try {
            List<User> friends = friendshipService.getFriendships(currentUser.getUserId());
            System.out.println(friends);
            friends.forEach(friend -> friendListModel.addElement(friend.getUsername()));
        } catch (Exception e) {
            e.printStackTrace();
        }

        //放置在UI界面上 用于显示
        friendList.setModel(friendListModel);
        groupList.setModel(groupListModel);
    }

    private void handleSendMessage(ActionEvent e) {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            if (!friendList.isSelectionEmpty()) {//发送给好友
                //获取当前选中的好友
                String friendName = friendList.getSelectedValue();
                //查询到好友ID
                User friend = userService.getUserByUsername(friendName);
                //发送信息
                messageService.sendMessage(currentUser.getUserId(), friend.getUserId(), null, message);
            } else if (!groupList.isSelectionEmpty()) {
                //获取当前选中的群组名称
                String groupName = groupList.getSelectedValue();
                //模糊查询到对应的群组详细信息
                ChatGroup group = chatGroupService.getGroupByName(groupName).get(0);
                //发送信息
                messageService.sendMessage(currentUser.getUserId(), null, group.getGroupId(), message);
            }
            chatArea.append("我: " + message + "\n");
            inputField.setText("");
        }
    }

    private void loadGroupChatHistory() {//加载群组历史信息
        if (!groupList.isSelectionEmpty()) {
            //获取当前选中群组的详细信息
            String groupName = groupList.getSelectedValue();
            ChatGroup group = chatGroupService.getGroupByName(groupName).get(0);
            //加载历史信息
            List<Message> messages = messageService.getGroupMessages(group.getGroupId());
            chatArea.setText("");
            messages.forEach(msg -> {
                String senderUsername = userService.getUserById(msg.getSenderId()).getUsername();
                chatArea.append(senderUsername + ": " + msg.getMessage() + "\n");
            });
        }
    }

    private void loadFriendChatHistory() {//加载好友历史信息
        if (!friendList.isSelectionEmpty()) {
            //获取当前选中好友的详细信息
            String friendName = friendList.getSelectedValue();
            User friend = userService.getUserByUsername(friendName);
            //获取当前两个人的历史信息
            List<Message> messages = messageService.getMessagesBetweenUsers(currentUser.getUserId(), friend.getUserId());
            //放置在chatArea中
            chatArea.setText("");
            messages.forEach(msg -> {
                String senderUsername = userService.getUserById(msg.getSenderId()).getUsername();
                chatArea.append(senderUsername + ": " + msg.getMessage() + "\n");
            });
        }
    }
}
