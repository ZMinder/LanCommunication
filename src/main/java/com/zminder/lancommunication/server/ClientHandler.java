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

            // 第一条信息是用户名
            String username = in.readLine();
            // 查询数据库获取用户对象
            this.user = userService.getUserByUsername(username);

            if (this.user != null) {
                //注册到server
                server.registerClient(user.getUsername(), this);

                String messageLine;
                while ((messageLine = in.readLine()) != null) {
                    handleMessage(messageLine);
                }
            } else {
                System.out.println("用户验证失败，无法找到用户名：" + username);
                return; // 如果用户验证失败，则不进行注册和消息处理
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
