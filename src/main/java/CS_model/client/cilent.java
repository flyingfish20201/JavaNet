package CS_model.client;

import java.io.IOException;
import java.net.Socket;


public class cilent {
    public static void main(String[] args) {
        try {
            //读取地址
            String host = "127.0.0.1";
            //读取端口号
            int port = 2333;

            Socket client = new Socket(host, port); //先写数据再读数据,读写线程分离
            new ReadDataFromServerThread(client).start();//启动读线程
            new WriteDataToServerThread(client).start();//启动写线程
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}