package MultiThreadReactor;

import java.io.IOException;

public class EchoServer {
    public static void main(String[] args) throws IOException {
        new Thread(new MultiReactor(2333)).start();
    }
}
