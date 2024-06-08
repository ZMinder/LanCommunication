package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class FriendRequestPanel extends JPanel {
    private MainFrame mainFrame;
    private Socket socket;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
    private DefaultListModel<JPanel> requestListModel = new DefaultListModel<>();
    private JList<JPanel> requestList = new JList<>(requestListModel);
    private volatile boolean isActive = false; // 使用 volatile 确保线程安全

    public FriendRequestPanel(MainFrame mainFrame, Socket socket, String username) {
        this.mainFrame = mainFrame;
        setConnection(socket, username);
        setupUI();
    }

    public void setConnection(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
        stopReading();  // 停止现有的数据读取
        initializeStreams();  // 重新初始化流
        startReading();  // 重新开始读取数据
        loadFriendRequests();  // 重新加载好友请求
    }

    private void initializeStreams() {
        try {
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error initializing network streams: " + e.getMessage());
        }
    }

    public void startReading() {
        isActive = true; // 设置活跃状态为true
        new Thread(() -> {
            try {
                String line;
                while (isActive && (line = in.readLine()) != null) { // 只有在活跃状态下才读取数据
                    handleReceivedData(line);
                }
            } catch (IOException e) {
                if (!Thread.currentThread().isInterrupted()) {
                    System.err.println("Error reading from socket: " + e.getMessage());
                }
            }
        }).start();
    }

    public void stopReading() {
        isActive = false; // 设置活跃状态为false，这将自然结束数据读取循环
    }

    private void handleReceivedData(String data) {
        if (data.startsWith("requestList:")) {
            String userList = data.substring(12);
            updateRequestList(userList);
        }
    }

    private void updateRequestList(String userData) {
        SwingUtilities.invokeLater(() -> {
            requestListModel.clear();
            if (!userData.isEmpty()) {
                String[] users = userData.split(",");
                for (String username : users) {
                    JPanel requestPanel = new JPanel(new BorderLayout());
                    JLabel nameLabel = new JLabel(username);
                    JButton acceptButton = new JButton("接受");
                    JButton rejectButton = new JButton("拒绝");
                    acceptButton.addActionListener(e -> sendFriendRequestResponse(username, true));
                    rejectButton.addActionListener(e -> sendFriendRequestResponse(username, false));
                    JPanel buttonPanel = new JPanel(new FlowLayout());
                    buttonPanel.add(acceptButton);
                    buttonPanel.add(rejectButton);
                    requestPanel.add(nameLabel, BorderLayout.CENTER);
                    requestPanel.add(buttonPanel, BorderLayout.EAST);
                    requestListModel.addElement(requestPanel);
                }
            } else {
                JOptionPane.showMessageDialog(this, "没有新的好友请求");
            }
        });
    }

    private void sendFriendRequestResponse(String username, boolean accepted) {
        String response = "friendResponse:" + username + ":" + (accepted ? "accept" : "reject");
        out.println(response);
    }

    private void setupUI() {
        setLayout(new BorderLayout());
        requestList.setCellRenderer(new RequestListCellRenderer());
        add(new JScrollPane(requestList), BorderLayout.CENTER);

        JButton backButton = new JButton("返回聊天");
        backButton.addActionListener(e -> mainFrame.showChatPanel(this.username, this.socket));
        add(backButton, BorderLayout.SOUTH);
    }

    private void loadFriendRequests() {
        out.println("loadFriendRequests");
    }

    class RequestListCellRenderer implements ListCellRenderer<JPanel> {
        @Override
        public Component getListCellRendererComponent(JList<? extends JPanel> list, JPanel value, int index, boolean isSelected, boolean cellHasFocus) {
            return value;
        }
    }
}
