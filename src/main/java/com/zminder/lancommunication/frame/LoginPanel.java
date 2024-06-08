package com.zminder.lancommunication.frame;

import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.UserService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("登录");
    private JButton goToRegisterButton = new JButton("注册");

    private UserService userService = new UserService();

    public LoginPanel(MainFrame mainFrame) {
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

        loginButton.setFont(new Font("Serif", Font.BOLD, 16));
        loginButton.setBackground(new Color(66, 139, 202));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        add(loginButton, gbc);

        gbc.gridy++;
        goToRegisterButton.setFont(new Font("Serif", Font.BOLD, 16));
        goToRegisterButton.setBackground(new Color(92, 184, 92));
        goToRegisterButton.setForeground(Color.WHITE);//设置按钮文字颜色
        goToRegisterButton.setFocusPainted(false);//是指获取焦点时是否绘制焦点状态
        add(goToRegisterButton, gbc);

        // 登录按钮事件处理
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                // 使用SwingWorker进行网络操作，防止UI线程阻塞
                SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        try (Socket socket = new Socket("localhost", 12345);
                             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                            // 发送登录请求
                            out.println("login:" + username + ":" + password);
                            // 接收服务器响应
                            String response = in.readLine();
                            return "success".equals(response);
                        } catch (IOException ex) {
                            ex.printStackTrace();
                            return false;
                        }
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean success = get();
                            if (success) {
                                JOptionPane.showMessageDialog(mainFrame, "登录成功!");
                                User user = new User(); // 假设你有一个适当的User对象或方法来获取用户信息
                                user.setUsername(username);
                                mainFrame.showChatPanel(user);
                            } else {
                                JOptionPane.showMessageDialog(mainFrame, "登录失败!");
                            }
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(mainFrame, "登录错误：" + ex.getMessage());
                        }
                    }
                };
                worker.execute();
            }
        });


        // 切换到注册面板按钮事件处理
        goToRegisterButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mainFrame.showRegisterPanel();
            }
        });
    }
}
