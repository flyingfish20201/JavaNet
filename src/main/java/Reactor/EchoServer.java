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

    // final����һ��Ҫ�ڹ������г�ʼ��
    // ��Ϊstatic final��һ��Ҫֱ�ӳ�ʼ��������static������г�ʼ��
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
     * �ɷ�ѭ����ѭ������dispatch()����
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
                // ���selector����Ȥ���ϣ���ʹ�õ�������remove()����һ��
                selected.clear();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * �ɷ������൱���ж������Ժ��ٵ���ָ������
     * ʹ��dispatch()�����޲���ֱ���ɷ�����ָ�������ҵ���ָ������
     * ���磺Accept�Ľ��շ�����Handler�Ĵ����ĵķ���
     *
     * @param key
     */
    private void dispatch(SelectionKey key) {
        System.out.println("������һ��������");
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
                     * ÿ��newһ��Handler�൱����ע����һ��key��selector
                     * ���������ж�д�������Ͳ�����������DispatchLoopʵ��
                     */
                    new Handler(socketChannel, selector);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}