package com.zminder.client.frame;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.zminder.client.pojo.ChatGroup;
import com.zminder.client.pojo.ChatMessage;
import com.zminder.client.pojo.User;

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
    private CustomListRenderer friendListRenderer = new CustomListRenderer();
    private CustomListRenderer groupListRenderer = new CustomListRenderer();
    private JButton friendRequestButton = new JButton("查看好友申请");
    private volatile boolean isActive = false;  // 控制读取线程
    private Thread readingThread;  // 保存数据读取线程的引用

    public ChatPanel(MainFrame mainFrame, Socket socket, String username) {
        this.mainFrame = mainFrame;
        setConnection(socket, username);//设置username和socket
        setupUI();//设置UI界面
        loadInitialData();//加载好友列表和群组列表
        startReading();  // 默认开始读取
    }

    public void setConnection(Socket socket, String username) {
        chatArea.setText("");//清空聊天记录区域
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

        // 使用JTabbedPane配置好友列表和群组列表
        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.addTab("好友列表", new JScrollPane(friendList));
        tabbedPane.addTab("群组列表", new JScrollPane(groupList));
        friendList.setModel(friendListModel);
        groupList.setModel(groupListModel);
        friendList.setCellRenderer(friendListRenderer);
        groupList.setCellRenderer(groupListRenderer);

        // 创建一个包含JTabbedPane的面板，使其高度自适应
        JPanel tabPanel = new JPanel(new BorderLayout());
        tabPanel.add(tabbedPane, BorderLayout.CENTER);

        // 使用JSplitPane水平分隔聊天区域和选项卡
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, tabPanel, chatArea);
        mainSplitPane.setDividerLocation(180);
        mainSplitPane.setResizeWeight(0.2);
        add(mainSplitPane, BorderLayout.CENTER);

        JButton searchButton = new JButton("查找用户");
        searchButton.addActionListener(e -> mainFrame.showSearchPanel(username, socket));
        add(searchButton, BorderLayout.NORTH);

        // 添加查看好友申请按钮
        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(searchButton, BorderLayout.WEST);
        northPanel.add(friendRequestButton, BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        friendRequestButton.addActionListener(e -> mainFrame.showFriendRequestPanel(username, socket));

        //设置监听器
        sendButton.addActionListener(this::handleSendMessage);

        // 添加选项卡切换监听器
        tabbedPane.addChangeListener(e -> {
            chatArea.setText(""); // 清空聊天区域
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 0) { // 选择好友列表选项卡
                groupList.clearSelection(); // 清除群组列表的选中状态
                System.out.println("group clear");
            } else if (selectedIndex == 1) { // 选择群组列表选项卡
                friendList.clearSelection(); // 清除好友列表的选中状态
                System.out.println("friend clear");
            }
        });

        // 添加好友列表选择监听器
        friendList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !friendList.isSelectionEmpty()) {
                String selectedFriend = friendList.getSelectedValue();
                ((CustomListRenderer) friendList.getCellRenderer()).clearNewMessage(selectedFriend);
                friendList.repaint();
                loadFriendChatHistory();
            }
        });

        // 添加群组列表选择监听器
        groupList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && !groupList.isSelectionEmpty()) {
                String selectedGroup = groupList.getSelectedValue();
                ((CustomListRenderer) groupList.getCellRenderer()).clearNewMessage(selectedGroup);
                groupList.repaint();
                loadGroupChatHistory();
            }
        });
    }

    private void loadInitialData() {//从服务器端加载好友列表和群组列表
        out.println("load:friends");
        out.println("load:groups");
    }

    // 开始读取数据
    public synchronized void startReading() {
        if (readingThread == null || !readingThread.isAlive()) {  // 确保不重复启动线程
            isActive = true;
            readingThread = new Thread(this::readMessages);
            readingThread.start();
        }
    }

    // 停止读取数据
    public synchronized void stopReading() {
        isActive = false;
    }

    private void readMessages() {
        try {
            String line;
            while (isActive && in != null && (line = in.readLine()) != null) {
                System.out.println("chatPanel");
                final String msg = line;
                handleMessages(msg);
            }
        } catch (IOException e) {
            if (isActive) {
                System.err.println("读取数据时出错: " + e.getMessage());
            }
        }
    }

    // 处理服务器发送的消息
    private void handleMessages(String msg) {
        if (msg.startsWith("friends:")) {
            handleFriendsList(msg.substring(8));
        } else if (msg.startsWith("groups:")) {
            handleGroupsList(msg.substring(7));
        } else if (msg.startsWith("groupHistory:") || msg.startsWith("friendHistory:")) {
            handleHistoryMessages(msg);
        } else if (msg.startsWith("private:") || msg.startsWith("group:")) {
            handleNewMessage(msg);
        } else {
            SwingUtilities.invokeLater(() -> chatArea.append(msg + "\n"));
        }
    }

    private void handleNewMessage(String msg) {
        // private:fromUser:message
        // group:groupId:fromUser:message
        // 根据冒号来分割消息
        String[] parts = msg.split(":", 4);
        String type = parts[0];
        String senderOrGroupId = parts[1]; // 第二部分为发送者用户名或群组ID
        String fromUser; // 初始化发送者变量
        String content; // 初始化内容变量

        // 根据消息类型分别处理
        if (type.equals("private")) {
            if (parts.length < 3) {
                System.out.println("Received malformed private message: " + msg);
                return; // 如果消息格式不正确，则不处理此消息
            }
            fromUser = senderOrGroupId; // 私聊中的第二部分为发送者用户名
            content = parts[2]; // 私聊中的第三部分为消息内容
        } else if (type.equals("group")) {
            if (parts.length < 4) {
                System.out.println("Received malformed group message: " + msg);
                return; // 如果消息格式不正确，则不处理此消息
            }
            fromUser = parts[2]; // 群聊中的第三部分为发送者用户名
            content = parts[3]; // 群聊中的第四部分为消息内容
        } else {
            System.out.println("Unknown message type: " + msg);
            return;
        }

        SwingUtilities.invokeLater(() -> {
            if (type.equals("private")) {
                if (isActiveChat("private", senderOrGroupId)) {
                    chatArea.append(fromUser + ": " + content + "\n");
                } else {
                    ((CustomListRenderer) friendList.getCellRenderer()).setNewMessage(senderOrGroupId);
                    friendList.repaint();
                }
            } else if (type.equals("group")) {
                if (isActiveChat("group", senderOrGroupId)) {
                    chatArea.append(fromUser + ": " + content + "\n");
                } else {
                    System.out.println(senderOrGroupId);
                    ((CustomListRenderer) groupList.getCellRenderer()).setNewMessage(senderOrGroupId);
                    groupList.repaint();
                }
            }
        });
    }

    private boolean isActiveChat(String type, String id) {
        if (type.equals("private") && !friendList.isSelectionEmpty() && friendList.getSelectedValue().equals(id)) {
            return true;
        } else if (type.equals("group") && !groupList.isSelectionEmpty() && groupList.getSelectedValue().equals(id)) {
            return true;
        }
        return false;
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

    // 这个方法被调用当用户点击发送消息按钮
    private void handleSendMessage(ActionEvent e) {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            // 判断是发送给好友还是群组
            if (!friendList.isSelectionEmpty()) {
                System.out.println("private");
                // 发送私聊消息
                String friendName = friendList.getSelectedValue();
                out.println("private:" + friendName + ":" + message);
            } else if (!groupList.isSelectionEmpty()) {
                System.out.println("group");
                // 发送群聊消息
                String groupName = groupList.getSelectedValue();
                out.println("group:" + groupName + ":" + message);
            }
            chatArea.append(username + ":" + message + "\n"); // 在本地聊天区域显示发送的消息
            inputField.setText(""); // 清空输入框
        }
    }


    // 加载群组聊天历史
    private void loadGroupChatHistory() {
        if (!groupList.isSelectionEmpty()) {
            String groupName = groupList.getSelectedValue();
            out.println("load:groupHistory:" + groupName);
        }
    }

    // 加载好友聊天历史
    private void loadFriendChatHistory() {
        if (!friendList.isSelectionEmpty()) {
            String friendName = friendList.getSelectedValue();
            out.println("load:friendHistory:" + friendName);
        }
    }

    // 处理和显示历史消息
    private void handleHistoryMessages(String msg) {
        try {
            // 提取JSON部分
            System.out.println(msg);
            String json = msg.substring(msg.indexOf('['));
            Type type = new TypeToken<List<ChatMessage>>() {
            }.getType();
            List<ChatMessage> messages = gson.fromJson(json, type);

            SwingUtilities.invokeLater(() -> {
                chatArea.setText(""); // 清空聊天区域
                for (ChatMessage message : messages) {
                    chatArea.append(message.getUsername() + ": " + message.getContent() + "\n");
                }
            });
        } catch (JsonSyntaxException e) {
            System.err.println("JSON parsing error: " + e.getMessage());
        }
    }

}
