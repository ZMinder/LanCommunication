package com.zminder.lancommunication.server;

import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private User user; // 存储关联的用户对象

    private UserService userService = new UserService(); // 用户服务用于查询用户信息

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;

    }

    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            String messageLine;
            while ((messageLine = in.readLine()) != null) {
                if (messageLine.startsWith("login:")) {
                    handleLogin(messageLine);
                } else if (messageLine.startsWith("register:")) {
                    handleRegistration(messageLine);
                } else {
                    handleMessage(messageLine);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (this.user != null) {
                server.unregisterClient(user.getUsername());
            }
            closeResources();
        }
    }

    private void handleRegistration(String messageLine) {
        //register:username:password
        String[] parts = messageLine.split(":", 3);
        if (parts.length < 3) return; // 格式不正确

        String username = parts[1];
        String password = parts[2];

        boolean success = userService.registerUser(username, password);
        if (success) {
            sendMessage("success");
        } else {
            sendMessage("fail");
        }
    }

    private void handleLogin(String messageLine) {
        //login:username:password
        String[] parts = messageLine.split(":", 3);
        if (parts.length < 3) return; // 格式不正确

        String username = parts[1];
        String password = parts[2];

        User user = userService.loginUser(username, password);
        if (user != null) {
            this.user = user;
            server.registerClient(username, this);
            sendMessage("success");
        } else {
            sendMessage("fail");
        }
    }

    private void handleMessage(String messageLine) {
        String[] parts = messageLine.split(":", 3);
        if (parts.length < 3) {
            return; // 不符合预期的消息格式，忽略处理
        }

        //private recipient content
        //group groupId content
        String messageType = parts[0];
        String recipient = parts[1];
        String messageContent = parts[2];

        if ("private".equals(messageType)) {
            server.handleMessage(user.getUsername(), recipient, messageContent, false);
        } else if ("group".equals(messageType)) {
            server.handleMessage(user.getUsername(), recipient, messageContent, true);
        }
    }

    public void sendMessage(String message) {
        out.println(message);
    }

    private void closeResources() {
        try {
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {
        return user;
    }

    public int getUserId() {
        return user != null ? user.getUserId() : -1;
    }
}
