package com.zminder.lancommunication.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class Server {

    private ServerSocket serverSocket; // 服务器socket
    private final List<ClientHandler> clientHandlers = new ArrayList<>(); // 客户端处理器列表

    // 启动服务器的构造函数
    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    // 接受连接请求并创建新的客户端处理器
    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept(); // 接受客户端连接
                System.out.println("新客户端已连接");
                ClientHandler clientHandler = new ClientHandler(clientSocket); // 创建新的客户端处理器
                clientHandlers.add(clientHandler); // 添加到列表
                new Thread(clientHandler).start(); // 启动线程处理客户端
            }
        } catch (IOException e) {
            closeServerSocket();
        }
    }

    // 关闭服务器socket
    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close(); // 关闭服务器socket
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 客户端处理器内部类
    private class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedReader bufferedReader;
        private BufferedWriter bufferedWriter;

        public ClientHandler(Socket socket) {
            try {
                this.clientSocket = socket;
                this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }
        }

        @Override
        public void run() {
            String messageFromClient;

            try {
                while (clientSocket.isConnected()) {
                    messageFromClient = bufferedReader.readLine(); // 读取客户端消息
                    if (messageFromClient != null) {
                        System.out.println("Received: " + messageFromClient);
                        broadcastMessage(messageFromClient); // 广播消息给其他客户端
                    }
                }
            } catch (IOException e) {
                closeEverything(clientSocket, bufferedReader, bufferedWriter);
            }
        }

        // 广播消息给所有客户端
        private void broadcastMessage(String messageToSend) {
            for (ClientHandler clientHandler : clientHandlers) {
                try {
                    if (!clientHandler.clientSocket.equals(this.clientSocket)) {
                        clientHandler.bufferedWriter.write(messageToSend);
                        clientHandler.bufferedWriter.newLine();
                        clientHandler.bufferedWriter.flush();
                    }
                } catch (IOException e) {
                    closeEverything(clientHandler.clientSocket, clientHandler.bufferedReader, clientHandler.bufferedWriter);
                }
            }
        }

        // 关闭所有资源
        private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
            try {
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(12345); // 在12345端口启动服务器
        Server server = new Server(serverSocket);
        server.startServer(); // 启动服务器
    }
}
