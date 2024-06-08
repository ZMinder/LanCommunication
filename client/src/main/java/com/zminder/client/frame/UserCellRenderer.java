package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

class UserCellRenderer extends JPanel implements ListCellRenderer<String> {
    private JLabel label;
    private JButton addButton;
    private PrintWriter out;

    private String currentUsername; // 当前用户的用户名

    public UserCellRenderer(Socket socket, String username) {
        this.currentUsername = username; // 存储当前用户的用户名
        setLayout(new BorderLayout());
        label = new JLabel();
        addButton = new JButton("申请");
        add(label, BorderLayout.CENTER);
        add(addButton, BorderLayout.EAST);

        try {
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error setting up streams in renderer: " + e.getMessage());
        }
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends String> list, String value, int index, boolean isSelected, boolean cellHasFocus) {
        label.setText(value);

        // 如果当前项是用户自己，则不显示添加按钮
        if (value.equals(currentUsername)) {
            addButton.setVisible(false);
        } else {
            addButton.setVisible(true);
        }

        setBackground(isSelected ? list.getSelectionBackground() : list.getBackground());
        label.setForeground(isSelected ? list.getSelectionForeground() : list.getForeground());
        return this;
    }
}
