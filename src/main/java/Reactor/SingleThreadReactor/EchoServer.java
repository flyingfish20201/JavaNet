package Reactor.SingleThreadReactor;

import java.io.IOException;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        new Thread(new Reactor(2333)).start();
    }
}
