package CS_model.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class server {
    public static void main(String[] args) {
        try {
            int port = 2333;

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("服务器启动..." + serverSocket.getLocalSocketAddress());  //服务器启动,打印本地地址

            //线程池
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
            //开启多线程
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("有客户端连接到服务器:" + client.getRemoteSocketAddress());
                executorService.execute(new HandlerClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
