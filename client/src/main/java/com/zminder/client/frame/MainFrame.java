package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.net.Socket;

public class MainFrame extends JFrame {

    private CardLayout cardLayout = new CardLayout();//用于在同一个容器切换多个面板
    private JPanel mainPanel = new JPanel(cardLayout);//主面板

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ChatPanel chatPanel;
    private SearchPanel searchPanel;
    private FriendRequestPanel friendRequestPanel;

    public MainFrame() {
        setTitle("局域网通信");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭窗口即退出程序
        setLocationRelativeTo(null);//居中显示

        // 初始化各个面板
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);

        // 添加面板到主面板
        mainPanel.add(loginPanel, "登录");
        mainPanel.add(registerPanel, "注册");

        add(mainPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "登录");
    }

    public void showRegisterPanel() {
        cardLayout.show(mainPanel, "注册");
    }

    public void showChatPanel(String username, Socket socket) {
        if (chatPanel == null) {
            chatPanel = new ChatPanel(this, socket, username);
            mainPanel.add(chatPanel, "聊天");
        } else {
            chatPanel.setConnection(socket, username);
        }
        cardLayout.show(mainPanel, "聊天");
        if (searchPanel != null) {
            searchPanel.stopReading();
        }
        if (friendRequestPanel != null) {
            friendRequestPanel.stopReading();
        }
        chatPanel.startReading();
    }

    public void showSearchPanel(String username, Socket socket) {
        if (searchPanel == null) {
            searchPanel = new SearchPanel(this, socket, username);
            mainPanel.add(searchPanel, "查找用户");
        } else {
            searchPanel.setConnection(socket, username);
        }
        cardLayout.show(mainPanel, "查找用户");
        if (chatPanel != null) {
            chatPanel.stopReading();
        }
        if (friendRequestPanel != null) {
            friendRequestPanel.stopReading();
        }
        searchPanel.startReading();
    }

    public void showFriendRequestPanel(String username, Socket socket) {
        if (friendRequestPanel == null) {
            friendRequestPanel = new FriendRequestPanel(this, socket, username);
            mainPanel.add(friendRequestPanel, "好友申请");
        } else {
            friendRequestPanel.setConnection(socket, username);
        }
        cardLayout.show(mainPanel, "好友申请");
        if (chatPanel != null) {
            chatPanel.stopReading();
        }
        if (searchPanel != null) {
            searchPanel.stopReading();
        }
        friendRequestPanel.startReading();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //确保在事件调度线程中创建和显示 MainFrame，以保证线程安全。
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
