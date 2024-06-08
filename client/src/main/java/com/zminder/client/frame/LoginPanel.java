package com.zminder.client.frame;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutionException;

public class LoginPanel extends JPanel {

    private MainFrame mainFrame;
    private JTextField usernameField = new JTextField(20);
    private JPasswordField passwordField = new JPasswordField(20);
    private JButton loginButton = new JButton("登录");
    private JButton goToRegisterButton = new JButton("注册");

    public LoginPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setupUI();
        setupListeners();
    }

    private void setupUI() {
        setLayout(new GridBagLayout());
        setBackground(new Color(235, 245, 251));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.EAST;
        add(new JLabel("用户名:"), gbc);

        gbc.gridx++;
        usernameField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        add(new JLabel("密码:"), gbc);

        gbc.gridx++;
        passwordField.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy++;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        loginButton.setBackground(new Color(66, 139, 202));
        loginButton.setForeground(Color.WHITE);
        add(loginButton, gbc);

        gbc.gridy++;
        goToRegisterButton.setBackground(new Color(92, 184, 92));
        goToRegisterButton.setForeground(Color.WHITE);
        add(goToRegisterButton, gbc);
    }

    private void setupListeners() {
        loginButton.addActionListener(e -> attemptLogin());
        goToRegisterButton.addActionListener(e -> mainFrame.showRegisterPanel());
    }

    private void attemptLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            private Socket socket;

            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    socket = new Socket("localhost", 12345);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    out.println("login:" + username + ":" + password);
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
                    boolean success = get();  // 可能会报类型匹配异常
                    if (success) {
                        JOptionPane.showMessageDialog(mainFrame, "登录成功!");
                        mainFrame.showChatPanel(username, socket); // Pass username and socket to ChatPanel
                    } else {
                        JOptionPane.showMessageDialog(mainFrame, "登录失败!");
                        if (socket != null) {
                            socket.close();
                        }
                    }
                } catch (InterruptedException | ExecutionException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "登录错误: " + ex.getMessage());
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(mainFrame, "网络错误: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }
}
