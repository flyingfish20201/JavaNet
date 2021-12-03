package Reactor.EchoClient;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.atomic.AtomicInteger;

public class Handler implements Runnable {
    private final static int READ = 0;
    private final static int SEND = 1;
    private final SelectionKey selectionKey;
    private final SocketChannel socketChannel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(2048);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
    private int status = SEND;

    private AtomicInteger counter = new AtomicInteger();

    Handler(SocketChannel socketChannel, Selector selector) throws IOException {
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, 0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_WRITE);
        selector.wakeup();
    }

    @Override
    public void run() {
        try {
            switch (status) {
                case SEND:
                    send();
                    break;
                case READ:
                    read();
                    break;
                default:
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
            selectionKey.cancel();
            try {
                socketChannel.close();
            } catch (IOException e1) {
                System.out.println(e1.getMessage());
                e1.printStackTrace();
            }
        }
    }

    void send() throws IOException {
        if (selectionKey.isValid()) {
            sendBuffer.clear();
            int count = counter.incrementAndGet();
            if (count <= 10) {
                sendBuffer.put(String.format("客户端发送的第%s条消息", count).getBytes());
                //切换到读模式，用于让通道读到buffer里的数据
                sendBuffer.flip();
                socketChannel.write(sendBuffer);

                //则再次切换到读，用以接收服务端的响应
                status = READ;
                selectionKey.interestOps(SelectionKey.OP_READ);
            } else {
                selectionKey.cancel();
                socketChannel.close();
            }
        }
    }

    private void read() throws IOException {
        if (selectionKey.isValid()) {
            //切换成buffer的写模式，用于让通道将自己的内容写入到buffer里
            readBuffer.clear();
            socketChannel.read(readBuffer);
            System.out.println(String.format("收到来自服务端的消息: %s", new String(readBuffer.array())));
            //收到服务端的响应后，再继续往服务端发送数据
            status = SEND;
            //注册写事件
            selectionKey.interestOps(SelectionKey.OP_WRITE);
        }
    }
}
