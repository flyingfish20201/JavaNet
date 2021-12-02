package Multithread;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EchoServer {
    private final int POOL_SIZE = 4;
    private int port = 2333;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public EchoServer() throws IOException {
        serverSocket = new ServerSocket(port);
        // 根据CPU的数目来创建线程
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);

        System.out.println("Server is running");
    }

    public static void main(String[] args) throws IOException {
        new EchoServer().service();
    }

    public void service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                executorService.execute(new handler(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

