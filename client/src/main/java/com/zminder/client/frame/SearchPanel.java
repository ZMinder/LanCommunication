package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SearchPanel extends JPanel {
    private MainFrame mainFrame;
    private Socket socket;
    private String username;
    private PrintWriter out;
    private BufferedReader in;
    private JTextField searchField = new JTextField(20);
    private JButton searchButton = new JButton("搜索");
    private JButton backButton = new JButton("返回");
    private JList<String> resultList = new JList<>();
    private DefaultListModel<String> listModel = new DefaultListModel<>();

    private volatile boolean isActive = false;
    private Thread readingThread;

    public SearchPanel(MainFrame mainFrame, Socket socket, String username) {
        this.mainFrame = mainFrame;
        setConnection(socket, username);
        setupUI();
        startReading();
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // 上方搜索栏
        JPanel northPanel = new JPanel();
        northPanel.setLayout(new BorderLayout());
        northPanel.add(searchField, BorderLayout.CENTER);
        northPanel.add(searchButton, BorderLayout.EAST);
        add(northPanel, BorderLayout.NORTH);

        // 结果列表
        add(new JScrollPane(resultList), BorderLayout.CENTER);
        resultList.setModel(listModel);
        resultList.setCellRenderer(new UserCellRenderer(socket, username)); // 使用自定义的列表项渲染器

        // 下方包含返回按钮
        JPanel southPanel = new JPanel();
        southPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(backButton);
        add(southPanel, BorderLayout.SOUTH);

        searchButton.addActionListener(e -> sendSearchRequest(searchField.getText()));
        backButton.addActionListener(e -> mainFrame.showChatPanel(username, socket));

        // 添加鼠标监听器到结果列表
        resultList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                int index = resultList.locationToIndex(e.getPoint());
                if (index >= 0) {
                    Rectangle rect = resultList.getCellBounds(index, index);
                    Rectangle buttonBounds = new Rectangle(rect.x + rect.width - 80, rect.y, 80, rect.height);
                    if (buttonBounds.contains(e.getPoint())) {
                        String selectedUser = listModel.getElementAt(index);
                        sendFriendRequest(selectedUser);
                    }
                }
            }
        });

        // 开启一个线程来监听服务器的响应
        new Thread(this::readSearchResults).start();
    }

    // 设置或更新网络连接
    public void setConnection(Socket socket, String username) {
        this.socket = socket;
        this.username = username;
        try {
            if (this.socket != null && !this.socket.isClosed()) {
                out = new PrintWriter(socket.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "设置连接时出错: " + e.getMessage());
        }
    }

    private void sendSearchRequest(String searchTerm) {
        if (out != null) {
            out.println("userSearch:" + searchTerm);
        }
    }

    private void sendFriendRequest(String toUser) {
        System.out.println(toUser);
        if (!toUser.equals(username) && out != null) { // 确保不向自己发送好友请求
            out.println("friendRequest:" + toUser);
        }
    }

    public synchronized void startReading() {
        if (readingThread == null || !readingThread.isAlive()) {
            isActive = true;
            readingThread = new Thread(this::readSearchResults);
            readingThread.start();
        }
    }

    public synchronized void stopReading() {
        isActive = false;
    }

    private void readSearchResults() {
        try {
            String line;
            while (isActive && in != null && (line = in.readLine()) != null) {
                handleSearchResults(line);
            }
        } catch (IOException e) {
            if (isActive) {
                System.err.println("读取搜索结果时出错: " + e.getMessage());
            }
        }
    }

    private void handleSearchResults(String msg) {
        SwingUtilities.invokeLater(() -> {
            if (msg.startsWith("userSearch:")) {
                String userData = msg.substring(11);
                updateSearchResults(userData);
            } else if (msg.equals("search:no users found")) {
                JOptionPane.showMessageDialog(this, "没有找到用户");
                listModel.clear();
            }
        });
    }

    private void updateSearchResults(String userData) {
        if (userData.isEmpty()) {
            listModel.clear();
            JOptionPane.showMessageDialog(this, "没有找到用户");
        } else {
            String[] users = userData.split(",");
            listModel.clear();
            for (String user : users) {
                listModel.addElement(user);
            }
        }
    }
}
