package com.zminder.lancommunication.frame;

import com.zminder.lancommunication.pojo.User;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private CardLayout cardLayout = new CardLayout();//用于在同一个容器切换多个面板
    private JPanel mainPanel = new JPanel(cardLayout);//主面板

    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;
    private ChatPanel chatPanel;

    public MainFrame() {
        setTitle("局域网通信");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);//关闭窗口即退出程序
        setLocationRelativeTo(null);//居中显示

        // 初始化各个面板
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel(this);
        chatPanel = new ChatPanel(this);

        // 添加面板到主面板
        mainPanel.add(loginPanel, "登录");
        mainPanel.add(registerPanel, "注册");
        mainPanel.add(chatPanel, "聊天");

        add(mainPanel);
        showLoginPanel();
    }

    public void showLoginPanel() {
        cardLayout.show(mainPanel, "登录");
    }

    public void showRegisterPanel() {
        cardLayout.show(mainPanel, "注册");
    }

    public void showChatPanel(User user) {
        chatPanel.setCurrentUser(user);
        cardLayout.show(mainPanel, "聊天");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            //确保在事件调度线程中创建和显示 MainFrame，以保证线程安全。
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);
        });
    }
}
