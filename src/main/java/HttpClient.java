import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class HttpClient {
    public static void main(String[] args) throws IOException {
        URL url = new URL("https://www.baidu.com/");
        URLConnection connection = url.openConnection();

        InputStream is = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = -1;
        while ((len = is.read(buff)) != -1) {
            buffer.write(buff, 0, len);
        }
        System.out.println(new String(buffer.toString()));
    }
}
