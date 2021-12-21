package Reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class EchoServer {

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;

    public EchoServer(int port) throws IOException {
        // ��ʼ��Selector��Channel�������ע��
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);
        SelectionKey selectionKey = serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        selectionKey.attach(new Acceptor());
        serverSocketChannel.bind(new InetSocketAddress(port));
    }

    public static void main(String[] args) {
        EchoServer reactor;
        try {
            reactor = new EchoServer(2333);
            reactor.dispatchLoop();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ѯ�ַ�����
     *
     * @throws IOException
     */
    private void dispatchLoop() throws IOException {
        while (true) {
            selector.select();
            Set<SelectionKey> selectionKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectionKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey selectionKey = iterator.next();
                dispatchTask(selectionKey);
            }
            selectionKeys.clear();
        }
    }

    /**
     * ����������Ľ��װ棬����Խ��ͣ���չ����ǿ
     * ����ֻ��Ҫʵ��Runnable�ӿڣ�����дrun()�������Ϳ���ʵ�ֶ���������޲�����
     *
     * @param taskSelectionKey
     */
    private void dispatchTask(SelectionKey taskSelectionKey) {
        Runnable runnable = (Runnable) taskSelectionKey.attachment();
        if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * Accept�࣬ʵ��TCP���ӵĽ�����SocketChannel�Ļ�ȡ���������ʵ��
     * �������ʵ�֣����Է���һ��Accept���Ӧһ��ServerSocketChannel
     *
     * @author CringKong
     */
    private class Acceptor implements Runnable {

        @Override
        public void run() {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                if (socketChannel != null) {
                    // ����һ���µĴ�����
                    new Handler(socketChannel, selector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}