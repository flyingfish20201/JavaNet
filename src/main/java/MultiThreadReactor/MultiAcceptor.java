package MultiThreadReactor;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import static java.lang.String.format;

class MultiAcceptor implements Runnable {
    private final ServerSocketChannel serverSocketChannel;
    // 获得CPU核心数
    private final int coreNum = Runtime.getRuntime().availableProcessors();
    private final Selector[] selectors = new Selector[coreNum];
    private int next = 0;
    private SubReactor[] reactors = new SubReactor[coreNum];
    private Thread[] threads = new Thread[coreNum];

    public MultiAcceptor(ServerSocketChannel serverSocketChannel) throws IOException {
        this.serverSocketChannel = serverSocketChannel;

        for (int i = 0; i < coreNum; i++) {
            selectors[i] = Selector.open();
            reactors[i] = new SubReactor(selectors[i], i);
            threads[i] = new Thread(reactors[i]);
            threads[i].start();
        }
    }

    @Override
    public void run() {
        SocketChannel socketChannel;
        try {
            socketChannel = serverSocketChannel.accept();
            if (socketChannel != null) {
                System.out.println(format("收到来自%s的连接", socketChannel.getRemoteAddress()));
                socketChannel.configureBlocking(false);
                reactors[next].registering(true);
                selectors[next].wakeup();
                SelectionKey selectionKey = socketChannel.register(selectors[next], SelectionKey.OP_READ);
                selectors[next].wakeup();
                reactors[next].registering(false);
                selectionKey.attach(new MultiAsyncHandler(socketChannel, selectors[next], next));
                if (++next == selectors.length) {
                    next = 0;
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}