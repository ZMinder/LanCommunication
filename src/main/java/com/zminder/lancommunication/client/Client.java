package com.zminder.lancommunication.client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket; // 客户端socket，用于与服务器建立连接
    private BufferedReader bufferedReader; // 用于读取从服务器接收的数据
    private BufferedWriter bufferedWriter; // 用于向服务器发送数据

    // 构造函数，初始化socket和输入输出流
    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // 发送消息到服务器并接收服务器回复的方法
    public void sendMessage() {
        try {
            Scanner scanner = new Scanner(System.in); // 创建扫描器读取用户输入
            while (socket.isConnected()) { // 循环直到socket断开连接
                String messageToSend = scanner.nextLine(); // 读取用户输入
                bufferedWriter.write(messageToSend); // 写入缓冲区
                bufferedWriter.newLine(); // 添加换行，表示消息结束
                bufferedWriter.flush(); // 刷新缓冲区，发送数据

                // 读取并显示服务器的响应
                System.out.println("服务器回复：" + bufferedReader.readLine());
            }
        } catch (IOException e) {
            closeEverything(socket, bufferedReader, bufferedWriter);
        }
    }

    // 在新线程中监听从服务器接收的消息
    public void listenForMessage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                String msgFromGroupChat;
                while (socket.isConnected()) { // 循环直到socket断开连接
                    try {
                        msgFromGroupChat = bufferedReader.readLine(); // 读取一行消息
                        System.out.println("收到消息: " + msgFromGroupChat); // 打印收到的消息
                    } catch (IOException e) {
                        closeEverything(socket, bufferedReader, bufferedWriter);
                    }
                }
            }
        }).start(); // 启动线程
    }

    // 关闭所有资源的方法
    private void closeEverything(Socket socket, BufferedReader bufferedReader, BufferedWriter bufferedWriter) {
        try {
            if (bufferedReader != null) {
                bufferedReader.close(); // 关闭输入流
            }
            if (bufferedWriter != null) {
                bufferedWriter.close(); // 关闭输出流
            }
            if (socket != null) {
                socket.close(); // 关闭socket连接
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 主函数，程序入口
    public static void main(String[] args) throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("输入服务器IP地址:"); // 提示输入服务器IP
        String ip = scanner.nextLine();
        System.out.println("输入端口号:"); // 提示输入端口号
        int port = scanner.nextInt();
        Socket socket = new Socket(ip, port); // 创建与服务器的连接
        Client client = new Client(socket); // 创建客户端对象
        client.listenForMessage(); // 开始监听消息
        client.sendMessage(); // 开始发送消息
    }
}

