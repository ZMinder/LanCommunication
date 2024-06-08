package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.Set;

public class CustomListRenderer extends DefaultListCellRenderer {
    private Set<String> newMessages;//存储有新消息的列表名称

    public CustomListRenderer() {
        this.newMessages = new HashSet<>();
    }

    // 设置新消息
    public void setNewMessage(String value) {
        newMessages.add(value);
    }

    // 清除新消息提示
    public void clearNewMessage(String value) {
        newMessages.remove(value);
    }

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                  boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (newMessages.contains(value.toString())) {
            ImageIcon icon = new ImageIcon("E:\\Project\\LanCommunication\\client\\src\\main\\resources\\images\\red_pot.jpg");
            Image img = icon.getImage();
            Image newImg = img.getScaledInstance(10, 10, java.awt.Image.SCALE_SMOOTH); // 设置小红点的大小
            icon = new ImageIcon(newImg);
            label.setIcon(icon); // 设置小红点图标
        } else {
            label.setIcon(null); // 清除小红点图标
        }
        return label;
    }
}