import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.String.format;

public class MultiThreadReactorEchoServer {
    public static void main(String[] args) throws IOException {
        new Thread(new MultiReactor(2333)).start();
    }
}

class MultiReactor implements Runnable {
    private final Selector selector;
    private final ServerSocketChannel serverSocketChannel;

    // 初始化Reactor，构造方法
    public MultiReactor(int port) throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        // 绑定服务端口
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        // 设置为非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 注册监听accept事件
        SelectionKey key = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 绑定multiacceptor类
        key.attach(new MultiAcceptor(serverSocketChannel));
    }

    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                // 连接到达之前，阻塞
                int count = selector.select();
                if (count == 0) {
                    continue;
                }
                // 拿到这次连接的socket
                Set selected = selector.selectedKeys();
                Iterator it = selected.iterator();
                while (it.hasNext()) {
                    // 进行任务的分发
                    dispatch((SelectionKey) it.next());
                }
                selected.clear();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void dispatch(SelectionKey key) {
        // 每次拿到事件，调用事件自身的run方法，在handler里面进行创建
        Runnable r = (Runnable) key.attachment();
        if (r != null) {
            r.run();
        }
    }

}

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

class SubReactor implements Runnable {

    private final Selector selector;
    private int num;
    private boolean register = false;

    public SubReactor(Selector selector, int num) {
        this.selector = selector;
        this.num = num;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            System.out.println(String.format("%d号SubReactor等待注册中...", num));
            while (!Thread.interrupted() && !register) {
                try {
                    if (selector.select() == 0) {
                        continue;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    dispatch(it.next());
                    it.remove();
                }
            }
        }
    }

    private void dispatch(SelectionKey key) {
        Runnable r = (Runnable) key.attachment();
        if (r != null) {
            r.run();
        }
    }

    public void registering(boolean register) {
        this.register = register;
    }
}
