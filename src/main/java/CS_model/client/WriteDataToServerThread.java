package CS_model.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;


public class WriteDataToServerThread extends Thread {
    private final Socket client;

    public WriteDataToServerThread(Socket client) {
        this.client = client;
    }

    @Override
    public void run() {
        try {
            OutputStream clientOutput = this.client.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            Scanner scanner = new Scanner(System.in);  //有客户端输入数据
            while (true) {
                System.out.print("请输入>>");
                String data = scanner.nextLine(); //读数据
                writer.write(data + "\n");
                writer.flush();
                if (data.equals("bye")) {
                    System.out.println("系统关闭");
                    break;
                }
            }
            this.client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}