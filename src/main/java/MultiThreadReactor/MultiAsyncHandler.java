package MultiThreadReactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class MultiAsyncHandler implements Runnable {

    private final static int READ = 0;
    private final static int SEND = 1;
    private final static int PROCESSING = 2;
    private static final ExecutorService workers = Executors.newFixedThreadPool(5);
    private final Selector selector;
    private final SelectionKey selectionKey;
    private final SocketChannel socketChannel;
    private ByteBuffer readBuffer = ByteBuffer.allocate(1024);
    private ByteBuffer sendBuffer = ByteBuffer.allocate(2048);
    private int status = READ;
    private int num;

    public MultiAsyncHandler(SocketChannel socketChannel, Selector selector, int num) throws IOException {
        this.num = num;
        this.socketChannel = socketChannel;
        this.socketChannel.configureBlocking(false);
        selectionKey = socketChannel.register(selector, 0);
        selectionKey.attach(this);
        selectionKey.interestOps(SelectionKey.OP_READ);
        this.selector = selector;
        this.selector.wakeup();
    }

    @Override
    public void run() {

    }
}