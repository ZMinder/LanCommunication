package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class SearchPanel extends JPanel {
    private MainFrame mainFrame;
    private Socket socket;
    private String username;
    private JTextField searchField; // 搜索框
    private JButton searchButton, backButton; // 搜索和返回按钮
    private JList<String> resultList; // 显示搜索结果的列表
    private DefaultListModel<String> listModel; // 列表模型

    public SearchPanel(MainFrame mainFrame, Socket socket, String username) {
        this.mainFrame = mainFrame;
        this.socket = socket;
        this.username = username;
        initializeUI(); // 初始化用户界面
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 搜索面板设置
        JPanel searchPanel = new JPanel(new BorderLayout());
        searchField = new JTextField(20); // 设置搜索输入框
        searchButton = new JButton("查找"); // 设置查找按钮
        searchPanel.add(searchField, BorderLayout.CENTER);
        searchPanel.add(searchButton, BorderLayout.EAST);

        // 结果列表
        listModel = new DefaultListModel<>();
        resultList = new JList<>(listModel);
        resultList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 返回按钮
        backButton = new JButton("返回");
        backButton.addActionListener(e -> mainFrame.showChatPanel(username, socket)); // 添加返回按钮的事件处理

        // 将组件添加到面板
        add(searchPanel, BorderLayout.NORTH);
        add(new JScrollPane(resultList), BorderLayout.CENTER);
        add(backButton, BorderLayout.SOUTH);

        // 查找按钮的事件处理
        searchButton.addActionListener(this::performSearch);
    }

    private void performSearch(ActionEvent e) {
        String searchTerm = searchField.getText().trim(); // 获取搜索词
        if (!searchTerm.isEmpty()) {
            sendSearchRequest(searchTerm); // 发送搜索请求
        }
    }

    private void sendSearchRequest(String searchTerm) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("userSearch:" + searchTerm); // 向服务器发送搜索请求
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String response = in.readLine(); // 读取服务器响应
            updateSearchResults(response); // 更新搜索结果
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateSearchResults(String data) {
        // 假设数据是以逗号分隔的用户名列表
        SwingUtilities.invokeLater(() -> {
            listModel.clear(); // 清空现有列表
            if (data != null && !data.isEmpty()) {
                String[] users = data.split(","); // 分割字符串
                for (String user : users) {
                    listModel.addElement(user); // 添加用户到列表模型
                }
            }
        });
    }
}
