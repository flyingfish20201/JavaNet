package Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoServer implements Runnable {

    // final变量一定要在构造器中初始化
    // 若为static final则一定要直接初始化或者在static代码块中初始化
    final Selector selector;
    final ServerSocketChannel serverSocket;

    public EchoServer(int port) throws IOException {
        selector = Selector.open();
        serverSocket = ServerSocketChannel.open();
        serverSocket.socket().bind(new InetSocketAddress(port));
        serverSocket.configureBlocking(false);
        SelectionKey sKey = serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        sKey.attach(new Acceptor());
    }


    /**
     * DispatchLoop
     * 派发循环，循环调用dispatch()方法
     */
    @Override
    public void run() {
        try {
            while (!Thread.interrupted()) {
                selector.select();
                Set<SelectionKey> selected = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selected.iterator();
                while (iterator.hasNext()) {
                    dispatch(iterator.next());
                }
                // 清空selector的兴趣集合，和使用迭代器的remove()方法一样
                selected.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 派发任务，相当于判断条件以后再调用指定方法
     * 使用dispatch()可以无差别的直接派发任务到指定对象并且调用指定方法
     * 例如：Accept的接收方法，Handler的处理报文的方法
     *
     * @param key
     */
    private void dispatch(SelectionKey key) {
        System.out.println("发布了一个新任务");
        Runnable r = (Runnable) (key.attachment());
        if (r != null) {
            r.run();
        }
    }

    class Acceptor implements Runnable {
        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocket.accept();
                if (socketChannel != null) {
                    /**
                     * 每次new一个Handler相当于先注册了一个key到selector
                     * 而真正进行读写操作发送操作还是依靠DispatchLoop实现
                     */
                    new Handler(socketChannel, selector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}