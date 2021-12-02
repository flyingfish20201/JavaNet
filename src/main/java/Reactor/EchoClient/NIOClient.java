package Reactor.EchoClient;

import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class NIOClient implements Runnable {
    private Selector selector;
    private SocketChannel socketChannel;

    NIOClient(String ip, int port) {
        try {
            selector = Selector.open();

        }
    }

    @Override
    public void run() {

    }
}
