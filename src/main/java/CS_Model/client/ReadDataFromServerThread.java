package CS_Model.client;

import java.io.InputStream;
import java.net.Socket;
import java.util.Scanner;

/**
 * description:�ͻ��˴ӷ���˶�ȡ���ݵ��߳�
 **/
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
                String data = scanner.nextLine();//���ж�����
                System.out.println("���Է������Ϣ:" + data);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}