import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Scanner;

public class HttpClient {
    public static void main(String[] args) throws IOException {

/*
        Scanner sc = new Scanner(System.in);
        StringBuilder builder = new StringBuilder("https://cn.bing.com/search?q=");
        String keyWord = sc.nextLine();
        builder.append(keyWord);
        URL url = new URL(builder.toString());
        URLConnection connection = url.openConnection();

        InputStream is = connection.getInputStream();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int len = -1;
        while ((len = is.read(buff)) != -1) {
            buffer.write(buff, 0, len);
        }
*/

        Scanner sc = new Scanner(System.in);
        StringBuilder builder = new StringBuilder("https://cn.bing.com/search?q=");
        String keyWord = sc.nextLine();
        builder.append(keyWord);
        Document document = Jsoup.connect(builder.toString()).get();
        Elements links = document.select("a[href]");
        for (Element link : links) {
            System.out.println("link:" + link.attr("href"));
            System.out.println("text:" + link.text());
            System.out.println("-------------------------------");
        }
    }
}
