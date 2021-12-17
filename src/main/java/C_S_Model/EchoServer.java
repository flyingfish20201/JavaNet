package C_S_Model;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用非阻塞模式的SocketChannel,ServerSocketChannel
 */
public class EchoServer {
    boolean login_status = false;
    private Selector selector = null;
    private ServerSocketChannel serverSocketChannel = null;
    private int port = 2333;
    private Charset charset = Charset.forName("GBK");
    private Map<String, SelectionKey> ALL_USER_LIST = new ConcurrentHashMap<String, SelectionKey>();
    private Map<String, SelectionKey> ONLINE_USER_LIST = new ConcurrentHashMap<String, SelectionKey>();

    public EchoServer() throws IOException {
        //创建一个selector对象
        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.socket().setReuseAddress(true);
        //使serverSocketChannel工作于非阻塞模式
        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(port));
        System.out.println("服务器启动...");
    }

    public static void main(String[] args) throws IOException {
        new EchoServer().service();
    }

    public void service() throws IOException {
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        String msg = "";
        while (selector.select() > 0) {
            Set readyKeys = selector.selectedKeys();
            Iterator it = readyKeys.iterator();
            while (it.hasNext()) {
                SelectionKey key = null;
                try {
                    key = (SelectionKey) it.next();
                    it.remove();
                    if (key.isAcceptable()) {
                        ServerSocketChannel ssc = (ServerSocketChannel) key.channel();
                        SocketChannel socketChannel = (SocketChannel) ssc.accept();
                        System.out.println("接收到客户连接，来自：" + socketChannel.socket().getInetAddress() + ":"
                                + socketChannel.socket().getPort());
                        socketChannel.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE, buffer);
                        ALL_USER_LIST.put("william", key);
                        ALL_USER_LIST.put("flyingfish", key);
                        ALL_USER_LIST.put("wyq", key);
                    }
                    if (key.isReadable()) {
                        receive(key);
                    }
                    if (key.isWritable()) {
                        send(key, msg);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        if (key != null) {
                            key.cancel();
                            key.channel().close();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }

    public void send(SelectionKey key, String msg) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        buffer.flip();
        ByteBuffer outputBuffer = encode(msg);
        while (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer);
        }
        ByteBuffer temp = encode(msg);
        buffer.flip();
        buffer.compact();//删除已经处理的字符串
    }

    public void receive(SelectionKey key) throws IOException {
        ByteBuffer buffer = (ByteBuffer) key.attachment();
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer readBuff = ByteBuffer.allocate(32);
        socketChannel.read(readBuff);
        readBuff.flip();
        String msg = decode(readBuff);
        msg = msg.replaceAll("[\\r\\n]", "");
        System.out.println("msg:" + msg);
        Handler(key, msg);
    }

    private void Handler(SelectionKey key, String msg) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();

        msg = msg.replaceAll("[\\r\\n]", "");
        if (msg.startsWith("LOGIN:") || msg.startsWith("login:")) {
            String username = msg.split(":")[1];
            if (ALL_USER_LIST.containsKey(username)) {
                System.out.println(username + "登录成功\r\n");
                ONLINE_USER_LIST.put(username, key);
                login_status = true;
                send(key, "登录成功\r\n");
            } else {
                System.out.println(username + "登录失败\r\n");
                send(key, "登录失败，请重新登录\r\n");
            }
        } else if (msg.startsWith("QUERY") || msg.startsWith("query")) {
            if (login_status) {
                String str = "";
                for (String username : ONLINE_USER_LIST.keySet()) {
                    str = str + username + " | ";
                }
                str = str.replaceAll("[\\r\\n]", "");
                System.out.println("当前在线用户：" + str);
                send(key, "当前在线用户：" + str + "\r\n");
            } else {
                send(key, "请先登录\r\n");
            }
        } else if (msg.startsWith("SENDTO:") || msg.startsWith("sendto:")) {
            if (login_status) {
                String[] segments = msg.split(":");
                String targetUserName = segments[1];
                String message = segments[2];
                SelectionKey target = ONLINE_USER_LIST.get(targetUserName);
                sendto(target.channel(), message);
            } else {
                send(key, "请先登录\r\n");
            }

        } else if (msg.startsWith("EXIT") || msg.startsWith("exit")) {
            String username = "";
            for (Map.Entry<String, SelectionKey> entry : ONLINE_USER_LIST.entrySet()) {
                if (Objects.equals(entry.getValue(), socketChannel)) {
                    username = entry.getKey();
                }
            }
            key.cancel();
            socketChannel.close();
            ONLINE_USER_LIST.remove(username);
            System.out.println("关闭与客户端的连接");
        } else {
            String data = "输入有误，请重新输入\r\n";
            send(key, data);
        }
    }

    private void sendto(SelectableChannel target, String message) throws IOException {
        SocketChannel socketChannel = (SocketChannel) target;
        String outputData = message;
        ByteBuffer outputBuffer = encode(outputData);
        while (outputBuffer.hasRemaining()) {
            socketChannel.write(outputBuffer);
        }
    }

    public String decode(ByteBuffer buffer) {
        CharBuffer charBuffer = charset.decode(buffer);

        return charBuffer.toString();
    }

    public ByteBuffer encode(String str) {
        return charset.encode(str);
    }
}