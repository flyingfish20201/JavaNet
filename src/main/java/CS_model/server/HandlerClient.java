package CS_model.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class HandlerClient implements Runnable {

    // 维护所有的连接到服务端的客户端对象
    private static final Map<String, Socket> ONLINE_CLIENT_MAP = new ConcurrentHashMap<String, Socket>();

    private static final Map<String, Socket> ALL_CLIENT_MAP = new ConcurrentHashMap<String, Socket>();

    private final Socket client;
    public boolean su = false;

    public HandlerClient(Socket client) {
        this.client = client;
    }

    //@Override
    public void run() {
        try {
            //获取客户端的数据流
            InputStream clientInput = client.getInputStream();
            //字节流转字符流
            Scanner scanner = new Scanner(clientInput);
            /**
             *消息是按行读取
             * 1.register:<username> 例如: register:张三
             * 2.群聊: groupChat:<message> 例如:groupChat:大家好
             * 3.私聊: privateChat:张三:你好
             * 4.退出:bye
             */
            ALL_CLIENT_MAP.put("小丽", this.client);
            ALL_CLIENT_MAP.put("小天", this.client);
            ALL_CLIENT_MAP.put("小飞", this.client);


            // printOnlineClient();
            while (true) {

                // 读数据,按行读
                String data = scanner.nextLine();
                if (data.startsWith("LOGIN:")) {
                    // 登录
                    // 冒号分隔,取第一个
                    String userName = data.split(":")[1];

                    login(userName);
                    continue;
                }

                if (data.startsWith("QUERY")) {
                    // 注册
                    // String userName = data.split(":")[1];
                    // 冒号分隔,取第一个
                    // register(userName);
                    String msg = printOnlineClient2();
                    this.sendMessage(this.client, msg, false);

                    continue;
                }


                if (data.startsWith("register:")) {
                    //注册
                    String userName = data.split(":")[1];
                    register(userName);
                    continue;
                }

//                if (data.startsWith("groupChat:")) {
//                    String message = data.split(":")[1];
//                    groupChat(message);
//                    continue;
//                }

                if (data.startsWith("SENDTO:")) {
                    String[] segments = data.split(":");
                    // String targetUserName = segments[1].split("\\-")[0];
                    // 取目标用户名
                    // String message = segments[1].split("\\-")[1];
                    // 因为要取两次,所以用数组
                    // 取发送的消息内容
                    String targetUserName = segments[1];
                    String message = segments[3];
                    privateChat(targetUserName, message);
                    continue;
                }

                if (data.equals("EXIT")) {
                    //表示退出
                    bye();
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 当前客户端退出
    private void bye() {
        if (su == true) {
            for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
                Socket target = entry.getValue();
                if (target.equals(this.client)) {
                    // 在在线用户中找到自己并且移除
                    ONLINE_CLIENT_MAP.remove(entry.getKey());
                    break;
                }
                System.out.println(getCurrentUserName() + "退出聊天室");
            }
            // 打印当前用户
            printOnlineClient();
            this.sendMessage(this.client, "您已退出", false);
            su = false;
        } else {
            this.sendMessage(this.client, "请先登录", false);
        }

    }

    private String getCurrentUserName() {
        for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
            // getvalue得到Socket对象
            Socket target = entry.getValue();
            // 排除群聊的时候自己给自己发消息的情况
            if (target.equals(this.client)) {
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     * 私聊,给targetUserName发送message消息
     *
     * @param targetUserName
     * @param message
     */
    private void privateChat(String targetUserName, String message) {
        // 获取目标用户名
        Socket target = ONLINE_CLIENT_MAP.get(targetUserName);
        if (su == false) {
            this.sendMessage(this.client, "请先登录", false);

        } else if (target == null) {
            this.sendMessage(this.client, "没有这个用户" + targetUserName, false);
        } else {
            this.sendMessage(target, message, true);
        }
    }

    /**
     * 群聊,发送message
     *
     * @param message
     */
    private void groupChat(String message) {
        for (Map.Entry<String, Socket> entery : ONLINE_CLIENT_MAP.entrySet()) {
            // getvalue得到Socket对象
            Socket target = entery.getValue();
            // 排除群聊的时候自己给自己发消息的情况
            if (target.equals(this.client)) {
                continue;
            }
            this.sendMessage(target, message, true);
        }
    }

    /**
     * 以userName为key注册当前用户(Socket client)
     *
     * @param userName
     */
    private void register(String userName) {
        if (ALL_CLIENT_MAP.containsKey(userName)) {
            this.sendMessage(this.client, "您已经注册过了,无需重复注册", false);
        } else {
            ALL_CLIENT_MAP.put(userName, this.client);
//            printOnlineClient();
            this.sendMessage(this.client, "恭喜" + userName + "注册成功\n", false);
        }
    }

    private void login(String userName) {

        if (ALL_CLIENT_MAP.containsKey(userName)) {
            this.sendMessage(this.client, "登录成功", false);
            ONLINE_CLIENT_MAP.put(userName, this.client);
            su = true;
        } else {
            this.sendMessage(this.client, "无此用户，请重新登录", false);

        }
    }

    private void sendMessage(Socket target, String message, boolean prefix) {
        // value是每一个客户端
        OutputStream clientOutput = null;
        try {
            clientOutput = target.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            if (prefix) {
                String currentUserName = this.getCurrentUserName();

                writer.write("<" + currentUserName + "说:>" + message + "\n");
            } else {
                writer.write(message + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 服务器打印在线客户端
     */
    private void printOnlineClient() {
        System.out.println("当前在线人数:" + ONLINE_CLIENT_MAP.size() + "," + "用户名如下列表:");
        //Map的key为用户名
        for (String userName : ONLINE_CLIENT_MAP.keySet()) {
            System.out.println(userName);
        }
    }

    /**
     *
     */
    private String printOnlineClient2() {
        if (su == true) {
            System.out.println("当前在线人数:" + ONLINE_CLIENT_MAP.size() + "," + "用户名如下列表:");
            String a = "";
            for (String userName : ONLINE_CLIENT_MAP.keySet()) {
                System.out.println(userName);
                a = a + userName + "。";
            }
            return '\n' + "当前在线的人有：" + a;
        } else {
            return "请先登录";
        }

    }
}

