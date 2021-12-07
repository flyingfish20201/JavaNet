package CS_Model.server;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
public class server {
    public static void main(String[] args) {
        try {
            int port = 6666;

            ServerSocket serverSocket = new ServerSocket(port);
            System.out.println("����������..." + serverSocket.getLocalSocketAddress());  //����������,��ӡ���ص�ַ

            //�̳߳�
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

            while (true) {  //��ѭ���������߳�
                Socket client = serverSocket.accept();
                System.out.println("�пͻ������ӵ�������:" + client.getRemoteSocketAddress());
                executorService.execute(new HandlerClient(client));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
