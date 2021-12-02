import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultithreadEchoServer {
    private final int POOL_SIZE = 4;
    private int port = 2333;
    private ServerSocket serverSocket;
    private ExecutorService executorService;

    public MultithreadEchoServer() throws IOException {
        serverSocket = new ServerSocket(port);
        // 根据CPU的数目来创建线程
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * POOL_SIZE);

        System.out.println("Server is running");
    }

    public static void main(String[] args) throws IOException {
        new MultithreadEchoServer().service();
    }

    public void service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept();
                executorService.execute(new handler(socket));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

class handler implements Runnable {
    private Socket socket;

    public handler(Socket socket) {
        this.socket = socket;
    }

    private PrintWriter getWriter(Socket socket) throws IOException {
        OutputStream socketOut = socket.getOutputStream();
        return new PrintWriter(socketOut, true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(new InputStreamReader(socketIn));
    }

    public String echo(String msg) {
        return "echo:" + msg;
    }

    @Override
    public void run() {
        try {
            System.out.println("New connection accepted" + socket.getInetAddress() + ":" + socket.getPort());
            BufferedReader br = getReader(socket);
            PrintWriter pw = getWriter(socket);

            String msg = null;
            while ((msg = br.readLine()) != null) {
                System.out.println("from" + socket.getInetAddress() + ":" + socket.getPort() + ">" + msg);
                pw.println(echo(msg));
                if (msg.equals("bye")) {
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

    }
}