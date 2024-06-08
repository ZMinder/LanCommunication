package com.zminder.lancommunication.server;

import com.zminder.lancommunication.pojo.Message;
import com.zminder.lancommunication.pojo.User;
import com.zminder.lancommunication.service.GroupMemberService;
import com.zminder.lancommunication.service.MessageService;
import com.zminder.lancommunication.service.UserService;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private int port;
    private ServerSocket serverSocket;
    private ExecutorService threadPool; // 线程池处理客户端连接
    private ConcurrentHashMap<String, ClientHandler> clientHandlers; // 维护所有客户端的映射

    private MessageService messageService;
    private GroupMemberService groupMemberService;
    private UserService userService = new UserService();

    public ChatServer(int port) {
        this.port = port;
        this.clientHandlers = new ConcurrentHashMap<>();
        this.threadPool = Executors.newCachedThreadPool();
    }

    public void start() {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server is running on port " + port);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(clientSocket, this);
                threadPool.submit(clientHandler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void registerClient(String username, ClientHandler handler) {
        clientHandlers.put(username, handler);
    }

    public void unregisterClient(String username) {
        clientHandlers.remove(username);
    }

    public void handleMessage(String fromUsername, String toUser, String message, boolean isGroupMessage) {
        if (isGroupMessage) {
            sendGroupMessage(fromUsername, toUser, message);
        } else {
            sendPrivateMessage(fromUsername, toUser, message);
        }
    }

    private void sendPrivateMessage(String fromUser, String toUserId, String message) {
        int toUserIntId = Integer.parseInt(toUserId); // 将字符串ID转换为整数
        User toUser = userService.getUserById(toUserIntId); // 从数据库获取用户信息
        if (toUser != null) {
            String toUsername = toUser.getUsername();
            if (clientHandlers.containsKey(toUsername)) {
                // 如果用户在线，则直接发送消息
                clientHandlers.get(toUsername).sendMessage("From " + fromUser + ": " + message);
            }
            // 记录消息到数据库，无论用户是否在线
            messageService.sendMessage(new Message(clientHandlers.get(fromUser).getUserId(), toUserIntId, null, message));
        } else {
            System.out.println("用户ID：" + toUserId + " 未找到或不在线");
        }
    }


    private void sendGroupMessage(String fromUser, String groupId, String message) {
        int groupIntId = Integer.parseInt(groupId); // 将字符串ID转换为整数
        //记录到数据库
        messageService.sendMessage(new Message(clientHandlers.get(fromUser).getUserId(), null, groupIntId, message));
        List<User> members = groupMemberService.getGroupMembers(groupIntId);
        //给在线的用户发送
        for (User member : members) {
            if (!member.getUsername().equals(fromUser) && clientHandlers.containsKey(member.getUsername())) {
                clientHandlers.get(member.getUsername()).sendMessage("Group " + groupId + " From " + fromUser + ": " + message);
            }
        }
    }

    public static void main(String[] args) {
        int port = 12345; // Example port number
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
