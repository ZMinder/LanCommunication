package com.zminder.lancommunication.frame;

import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.ChatGroupService;
import com.zminder.lancommunication.service.GroupMemberService;
import com.zminder.lancommunication.service.MessageService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

public class ChatPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextArea chatArea = new JTextArea(20, 50);
    private JTextField inputField = new JTextField(40);
    private JButton sendButton = new JButton("发送");
    private JList<String> groupList = new JList<>();
    private DefaultListModel<String> groupListModel = new DefaultListModel<>();

    private ChatGroupService chatGroupService = new ChatGroupService();
    private MessageService messageService = new MessageService();
    private GroupMemberService groupMemberService = new GroupMemberService();

    private User currentUser;
    private ChatGroup currentGroup;

    public ChatPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        chatArea.setEditable(false);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        JScrollPane chatScrollPane = new JScrollPane(chatArea);

        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        setupButtonIcon(); // 设置按钮图标

        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.add(new JLabel("群组列表"), BorderLayout.NORTH);
        groupList.setModel(groupListModel);
        leftPanel.add(new JScrollPane(groupList), BorderLayout.CENTER);

        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 0.25;
        gbc.weighty = 1.0;
        add(leftPanel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.75;
        gbc.weighty = 1.0;
        add(chatScrollPane, gbc);

        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.weightx = 0.75;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputPanel, gbc);

        sendButton.addActionListener(this::sendMessage);
    }

    private void setupButtonIcon() {
        ImageIcon originalIcon = new ImageIcon("E:\\Project\\LanCommunication\\src\\main\\resources\\images\\send_logo.png");
        Image image = originalIcon.getImage();
        Image resizedImage = image.getScaledInstance(24, 24, Image.SCALE_SMOOTH);
        ImageIcon resizedIcon = new ImageIcon(resizedImage);
        sendButton.setIcon(resizedIcon);
    }

    private void sendMessage(ActionEvent e) {
        String message = inputField.getText();
        if (!message.isEmpty() && currentGroup != null) {
            boolean success = messageService.sendMessage(currentUser.getUserId(), null, currentGroup.getGroupId(), message);
            if (success) {
                chatArea.append("我: " + message + "\n");
                inputField.setText("");
            } else {
                JOptionPane.showMessageDialog(mainFrame, "消息发送失败.");
            }
        }
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        System.out.println(this.currentUser);
        loadUserGroups();
    }

    private void loadUserGroups() {
        List<ChatGroup> groups = groupMemberService.getGroupsByUserId(currentUser.getUserId());
        groupListModel.clear();
        for (ChatGroup group : groups) {
            groupListModel.addElement(group.getGroupName());
        }
    }
}
