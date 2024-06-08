package com.zminder.lancommunication.frame;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.util.List;

public class ChatPanel extends JPanel {
    private MainFrame mainFrame;
    private String username;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson = new Gson();
    private JTextArea chatArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(40);
    private JButton sendButton = new JButton("发送");

    private JList<String> friendList = new JList<>();
    private DefaultListModel<String> friendListModel = new DefaultListModel<>();
    private JList<String> groupList = new JList<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();

    public ChatPanel(MainFrame mainFrame, Socket socket, String username) {
        this.mainFrame = mainFrame;
        setConnection(socket, username);//设置username和socket
        setupUI();//设置UI界面
        loadInitialData();//加载好友列表和群组列表
    }

    public void setConnection(Socket socket, String username) {
        this.username = username;
        this.socket = socket;
        try {
            if (socket != null) {//创建输入输出流对象
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //开启接受服务器端信息的线程
                new Thread(this::readMessages).start();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "连接错误: " + e.getMessage());
        }
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

    private void loadInitialData() {//从服务器端加载好友列表和群组列表
        out.println("load:friends");
        out.println("load:groups");
    }

    private void readMessages() {//处理从服务器端接收的信息
        try {
            String line;
            while ((line = in.readLine()) != null) {
                final String msg = line;
                handleMessages(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleMessages(String msg) {//将处理信息的代码封装起来
        if (msg.startsWith("friends:")) {
            handleFriendsList(msg.substring(8));
        } else if (msg.startsWith("groups:")) {
            handleGroupsList(msg.substring(7));
        } else {
            SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
        }
    }

    private void handleFriendsList(String json) {//将从服务器端加载的好友列表显示到本地
        Type listType = new TypeToken<List<User>>() {
        }.getType();
        List<User> friends = gson.fromJson(json, listType);
        SwingUtilities.invokeLater(() -> {
            friendListModel.clear();
            friends.forEach(friend -> friendListModel.addElement(friend.getUsername()));
        });
    }

    private void handleGroupsList(String json) {//将从服务器端加载的群组列表显示到本地
        Type listType = new TypeToken<List<ChatGroup>>() {
        }.getType();
        List<ChatGroup> groups = gson.fromJson(json, listType);
        SwingUtilities.invokeLater(() -> {
            groupListModel.clear();
            groups.forEach(group -> groupListModel.addElement(group.getGroupName()));
        });
    }

    private void handleSendMessage(ActionEvent e) {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            if (!friendList.isSelectionEmpty()) {
                String friendName = friendList.getSelectedValue();
                out.println("private:" + friendName + ":" + message);
            } else if (!groupList.isSelectionEmpty()) {
                String groupName = groupList.getSelectedValue();
                out.println("group:" + groupName + ":" + message);
            }
            chatArea.append("我: " + message + "\n");
            inputField.setText("");
        }
    }

    private void loadGroupChatHistory() {
        // 这里可以添加从服务器加载群组聊天历史的逻辑
    }

    private void loadFriendChatHistory() {
        // 这里可以添加从服务器加载好友聊天历史的逻辑
    }
}
