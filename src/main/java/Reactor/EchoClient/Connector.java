package Reactor.EchoClient;

import java.io.IOException;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class Connector implements Runnable {
    private final Selector selector;

    private final SocketChannel socketChannel;

    public Connector(SocketChannel socketChannel, Selector selector) {
        this.socketChannel = socketChannel;
        this.selector = selector;
    }

    @Override
    public void run() {
        try {
            if (socketChannel.finishConnect()) {
                System.out.println(String.format("已完成 %s 的连接",
                        socketChannel.getRemoteAddress()));
                new Handler(socketChannel, selector);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
