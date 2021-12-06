package CS_model.client;

import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;


public class ReadDataFromServerThread extends Thread {
    private final Socket client;

    public ReadDataFromServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            InputStream clientInput = this.client.getInputStream();
            Scanner scanner = new Scanner(clientInput);
            while (true) {
                String data = scanner.nextLine();//按行读数据
                System.out.println("来自服务端消息:" + data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}