package CS_Model.client;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * description:�ͻ��˸�����˷������ݵ��߳�
 **/
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
            Scanner scanner = new Scanner(System.in);  //�пͻ�����������
            while (true) {
                System.out.print("������>>");
                String data = scanner.nextLine(); //������
                writer.write(data + "\n");
                writer.flush();
                if (data.equals("bye")) {
                    System.out.println("ϵͳ�ر�");
                    break;
                }
            }
            this.client.close();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }
}