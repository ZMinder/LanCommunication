package com.zminder.server.server;

import com.zminder.server.pojo.Message;
import com.zminder.server.pojo.User;
import com.zminder.server.service.GroupMemberService;
import com.zminder.server.service.MessageService;
import com.zminder.server.service.UserService;

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

    public ClientHandler getClientHandler(String username) {
        return clientHandlers.get(username);
    }

    public static void main(String[] args) {
        int port = 12345; // Example port number
        ChatServer server = new ChatServer(port);
        server.start();
    }
}
