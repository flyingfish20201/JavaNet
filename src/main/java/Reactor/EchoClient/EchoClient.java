package Reactor.EchoClient;

public class EchoClient {
    public static void main(String[] args) {
        new Thread(new NIOClient("127.0.0.1", 2333)).start();
    }
}
