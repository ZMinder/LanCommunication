package com.zminder.lancommunication.server;

import com.google.gson.Gson;
import com.zminder.lancommunication.pojo.ChatGroup;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.FriendshipService;
import com.zminder.lancommunication.service.GroupMemberService;
import com.zminder.lancommunication.service.UserService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private User user; // 存储关联的用户对象

    private UserService userService = new UserService(); // 用户服务用于查询用户信息
    private FriendshipService friendshipService = new FriendshipService();
    private GroupMemberService groupMemberService = new GroupMemberService();
    private Gson gson = new Gson();

    public ClientHandler(Socket socket, ChatServer server) {
        this.socket = socket;
        this.server = server;

    }

    @Override
    public void run() {
        try {
            setupStreams();
            processClientRequests();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cleanUp();
        }
    }

    private void setupStreams() throws IOException {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);
    }

    private void processClientRequests() {
        try {
            String messageLine;
            while ((messageLine = in.readLine()) != null) {
                handleClientMessage(messageLine);
            }
        } catch (IOException e) {
            System.out.println("Error handling client messages: " + e.getMessage());
        }
    }

    private void handleClientMessage(String messageLine) {
        if (messageLine.startsWith("login:")) {
            handleLogin(messageLine);
        } else if (messageLine.startsWith("register:")) {
            handleRegistration(messageLine);
        } else if (messageLine.startsWith("load:friends")) {
            sendFriendsList();
        } else if (messageLine.startsWith("load:groups")) {
            sendGroupsList();
        } else {
            handleMessage(messageLine);
        }
    }

    private void sendFriendsList() {
        if (user != null) {
            List<User> friends = null;
            try {
                friends = friendshipService.getFriendships(user.getUserId());
                String json = gson.toJson(friends);
                sendMessage("friends:" + json);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void sendGroupsList() {
        if (user != null) {
            List<ChatGroup> groups = groupMemberService.getGroupsByUserId(user.getUserId());
            String json = gson.toJson(groups);
            sendMessage("groups:" + json);
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

    private void cleanUp() {
        if (user != null) {
            server.unregisterClient(user.getUsername());
        }
        closeResources();
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
