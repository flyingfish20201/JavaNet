package MultiThreadReactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

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