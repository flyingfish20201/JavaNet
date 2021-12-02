import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class MixEchoServer {
    private Selector selector = null;
    private ServerSocketChannel serverSocketChannel = null;
    private int port = 2333;
    private Charset charset = Charset.forName("GBK");
    // 接受和发送数据
    private Object gate = new Object();

    public MixEchoServer() throws IOException {
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("Server is running");
    }

    public static void main(String[] args) throws Exception {
        final MixEchoServer server = new MixEchoServer();
        // 匿名线程负责接受客户端连接
        Thread accept = new Thread() {
            @Override
            public void run() {
                server.accept();
            }
        };
        accept.start();
        // 主线程负责接收和发送数据
        server.service();
    }

    // serverSocketChannel先采用默认的阻塞模式，直到收到客户连接之后，进入非阻塞模式
    public void accept() {
        for (; ; ) {
            try {
                SocketChannel socketChannel = serverSocketChannel.accept();
                System.out.println("接受到客户连接，来自" + socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort());
                socketChannel.configureBlocking(false); // 设置为非阻塞模式

                ByteBuffer buffer = ByteBuffer.allocate(1024);
                // accept线程执行这个同步代码块
                synchronized (gate) {
                    selector.wakeup();
                    // 注册事件
                    socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void service() throws IOException {
        // 无限for循环
        for (; ; ) {
            // 空的同步代码块，目的是让主线程等待accept线程执行完同步代码块
            synchronized (gate) {
            }
            int n = selector.select();

            if (n == 0) {
                continue;
            }
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();
            while (it.hasNext()) {
                try {
                    SelectionKey key = null;
                    key = (SelectionKey) it.next();
                    it.remove(); // 删去已使用的key
                    if (key.isReadable()) {
                        receive(key);
                    }
                    if (key.isWritable()) {
                        send(key);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void send(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.flip();
        String data = decode(buffer);
        if (data.indexOf("\r\n") == -1) {
            return;
        }
        String outputData = data.substring(0, data.indexOf("\n") + 1);
        System.out.println(outputData);
        ByteBuffer outputBuffer = encode("echo:" + outputData);
        while (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer);
        }
        ByteBuffer temp = encode(outputData);
        buffer.position(temp.limit());
        buffer.compact();
        if (outputData.equals("bye\r\n")) {
            key.cancel();
            System.out.print(socketChannel.socket().getInetAddress() + ":" + socketChannel.socket().getPort());
            socketChannel.close();
            System.out.println("Close connection with client");
        }
    }

    public void receive(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();

        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer readBuff = ByteBuffer.allocate(32);
        socketChannel.read(readBuff);
        readBuff.flip();

        buffer.limit(buffer.capacity());
        buffer.put(readBuff);
    }

    public String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);
        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {
        return charset.encode(str);
    }
}
