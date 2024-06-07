package com.zminder.lancommunication.frame;

import com.zminder.lancommunication.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class RegisterPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton registerButton = new JButton("注册");
    private JButton backButton = new JButton("返回");

    private UserService userService = new UserService();

    public RegisterPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());//使用GridBagLayout布局管理器
        setBackground(new Color(235, 245, 251));//设置背景颜色

        GridBagConstraints gbc = new GridBagConstraints();
        //指定组件的边框与其显示区域的边缘之间应有多少空间
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        //用于确定当组件小于其分配区域时，组件应该在其空间里放置的位置
        gbc.anchor = GridBagConstraints.EAST;

        JLabel usernameLabel = new JLabel("用户名:");
        usernameLabel.setFont(new Font("Serif", Font.BOLD, 16));
        add(usernameLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        usernameField.setFont(new Font("Serif", Font.PLAIN, 16));
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setFont(new Font("Serif", Font.BOLD, 16));
        add(passwordLabel, gbc);

        gbc.gridx++;
        gbc.anchor = GridBagConstraints.WEST;
        passwordField.setFont(new Font("Serif", Font.PLAIN, 16));
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;

        registerButton.setFont(new Font("Serif", Font.BOLD, 16));
        registerButton.setBackground(new Color(66, 139, 202));
        registerButton.setForeground(Color.WHITE);
        registerButton.setFocusPainted(false);
        add(registerButton, gbc);

        gbc.gridy++;
        backButton.setFont(new Font("Serif", Font.BOLD, 16));
        backButton.setBackground(new Color(92, 184, 92));
        backButton.setForeground(Color.WHITE);//设置按钮文字颜色
        backButton.setFocusPainted(false);//是指获取焦点时是否绘制焦点状态
        add(backButton, gbc);

        // 注册按钮事件处理
        registerButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                boolean success = userService.registerUser(username, password);
                if (success) {
                    JOptionPane.showMessageDialog(mainFrame, "注册成功!");
                    mainFrame.showLoginPanel();
                } else {
                    JOptionPane.showMessageDialog(mainFrame, "注册失败!");
                }
            }
        });

        // 返回按钮事件处理
        backButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showLoginPanel();
            }
        });
    }
}
