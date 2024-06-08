package com.zminder.server.server;

import com.google.gson.Gson;
import com.zminder.server.pojo.ChatGroup;
import com.zminder.server.pojo.ChatMessage;
import com.zminder.server.pojo.Message;
import com.zminder.server.pojo.User;
import com.zminder.server.service.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class ClientHandler implements Runnable {
    private Socket socket;
    private ChatServer server;
    private BufferedReader in;
    private PrintWriter out;
    private User user; // 存储关联的用户对象

    private UserService userService = new UserService(); // 用户服务用于查询用户信息
    private FriendshipService friendshipService = new FriendshipService();
    private GroupMemberService groupMemberService = new GroupMemberService();
    private MessageService messageService = new MessageService();
    private ChatGroupService chatGroupService = new ChatGroupService();
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
        } else if (messageLine.startsWith("load:groupHistory:")) {
            sendGroupHistory(messageLine.substring(18)); // 提取 groupName
        } else if (messageLine.startsWith("load:friendHistory:")) {
            sendFriendHistory(messageLine.substring(19)); // 提取 friendUsername
        } else if (messageLine.startsWith("private:")) {
            handlePrivateMessage(messageLine.substring(8)); // 提取后续消息部分
        } else if (messageLine.startsWith("group:")) {
            handleGroupMessage(messageLine.substring(6)); // 提取后续消息部分
        } else if (messageLine.startsWith("userSearch:")) {
            handleUserSearch(messageLine.substring(11)); // 提取搜索词
        } else if (messageLine.startsWith("friendRequest:")) {
            handleFriendRequest(messageLine.substring(14));
        } else {
            System.out.println("Received unknown command: " + messageLine);
        }
    }


    private void handleFriendRequest(String requestedUsername) {
        User requestedUser = userService.getUserByUsername(requestedUsername);
        if (requestedUser == null) {
            System.out.println("friendRequest:fail:User not found");
        } else {
            try {
                friendshipService.sendFriendRequest(user.getUserId(), requestedUser.getUserId());
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleUserSearch(String searchTerm) {
        List<User> users = userService.searchUsers(searchTerm); // 调用UserService中的搜索方法
        if (users.isEmpty()) {
            sendMessage("search:no users found"); // 没有找到用户时的响应
        } else {
            String response = users.stream()
                    .map(User::getUsername)
                    .collect(Collectors.joining(",")); // 将用户名列表转换成字符串
            System.out.println(response);
            sendMessage("userSearch:" + response); // 发送搜索结果
        }
    }

    private void handlePrivateMessage(String details) {
        String[] parts = details.split(":", 2);
        if (parts.length < 2) return;  // 格式错误，返回

        String toUser = parts[0];
        String message = parts[1];
        sendPrivateMessage(user.getUsername(), toUser, message);
    }

    private void handleGroupMessage(String details) {
        String[] parts = details.split(":", 2);
        if (parts.length < 2) return;  // 格式错误，返回

        String groupName = parts[0];
        String message = parts[1];

        // 查询群组ID
        ChatGroup group = chatGroupService.getGroupByName(groupName).get(0);
        if (group != null) {
            sendGroupMessage(user.getUsername(), String.valueOf(group.getGroupId()), message);
        } else {
            System.out.println("群组 '" + groupName + "' 未找到");
        }
    }


    private void sendPrivateMessage(String fromUser, String toUser, String message) {
        User sender = userService.getUserByUsername(fromUser);
        User receiver = userService.getUserByUsername(toUser);

        // 查找目标用户是否在线
        ClientHandler receiverHandler = server.getClientHandler(toUser);
        if (receiverHandler != null) {
            // 用户在线，直接发送消息
            receiverHandler.sendMessage("private:" + fromUser + ":" + message);
        }

        // 记录发送的消息到数据库，无论用户是否在线
        if (sender != null && receiver != null) {
            Message msg = new Message();
            msg.setSenderId(sender.getUserId());
            msg.setReceiverId(receiver.getUserId());
            msg.setGroupId(null);  // 因为这是私人消息
            msg.setMessage(message);
            messageService.sendMessage(msg);
        }
    }

    private void sendGroupMessage(String fromUser, String groupId, String message) {
        User sender = userService.getUserByUsername(fromUser);
        int groupIdInt = Integer.parseInt(groupId);
        ChatGroup group = chatGroupService.getGroupById(groupIdInt);
        List<User> members = groupMemberService.getGroupMembers(groupIdInt);

        for (User member : members) {
            ClientHandler memberHandler = server.getClientHandler(member.getUsername());
            if (memberHandler != null && !member.getUsername().equals(fromUser)) {
                memberHandler.sendMessage("group:" + group.getGroupName() + ":" + fromUser + ":" + message);
            }
        }

        // 记录群组消息到数据库
        if (sender != null) {
            Message msg = new Message();
            msg.setSenderId(sender.getUserId());
            msg.setReceiverId(null);  // 因为这是群组消息
            msg.setGroupId(groupIdInt);
            msg.setMessage(message);
            messageService.sendMessage(msg);
        }
    }

    private void sendGroupHistory(String groupName) {
        ChatGroup group = chatGroupService.getGroupByName(groupName).get(0); // 假设返回的是列表，取第一个
        List<Message> messages = messageService.getGroupMessages(group.getGroupId());
        List<ChatMessage> chatMessages = messages.stream().map(msg -> {
            User sender = userService.getUserById(msg.getSenderId());
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setUsername(sender.getUsername());
            chatMsg.setContent(msg.getMessage());
            return chatMsg;
        }).collect(Collectors.toList());
        String json = gson.toJson(chatMessages);
        System.out.println(json);
        sendMessage("groupHistory:" + json);
    }

    private void sendFriendHistory(String friendUsername) {
        User friend = userService.getUserByUsername(friendUsername);
        List<Message> messages = messageService.getMessagesBetweenUsers(user.getUserId(), friend.getUserId());
        List<ChatMessage> chatMessages = messages.stream().map(msg -> {
            User sender = userService.getUserById(msg.getSenderId());
            ChatMessage chatMsg = new ChatMessage();
            chatMsg.setUsername(sender.getUsername());
            chatMsg.setContent(msg.getMessage());
            return chatMsg;
        }).collect(Collectors.toList());
        String json = gson.toJson(chatMessages);
        sendMessage("friendHistory:" + json);
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
