package CS_Model.client;

import java.io.IOException;
import java.net.Socket;


public class cilent {
    public static void main(String[] args) {
        try {
            //��ȡ��ַ
            String host = "127.0.0.1";
            //��ȡ�˿ں�
            int port = 6666;

            Socket client = new Socket(host, port); //��д�����ٶ�����,��д�̷߳���
            new ReadDataFromServerThread(client).start();//�������߳�
            new WriteDataToServerThread(client).start();//����д�߳�
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}