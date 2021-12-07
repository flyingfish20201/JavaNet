package CS_Model.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


public class HandlerClient implements Runnable {

    /**
     * ά�����е����ӵ�����˵Ŀͻ��˶���
     */
    private static final Map<String, Socket> ONLINE_CLIENT_MAP =
            new ConcurrentHashMap<String, Socket>();  //��̬��Ϊ�˲��ö���仯,final���ö����޸�,ConcurrentHashMap���̰߳�ȫ����
    //static final���κ������Ӧ���ó���--��д��ĸ���»��߷ָ�
    private static final Map<String, Socket> ALL_CLIENT_MAP =
            new ConcurrentHashMap<String, Socket>();
    private final Socket client;
    public boolean su = false;

    public HandlerClient(Socket client) {  //HandlerClient�ڶ��̻߳����µ���,���Ի������Դ����,��һ��������HashMap
        this.client = client;          //Ϊ�˷�ֹ�������޸�,��final����
    }

    //@Override
    public void run() {
        try {
            InputStream clientInput = client.getInputStream(); //��ȡ�ͻ��˵�������
            Scanner scanner = new Scanner(clientInput); //�ֽ���ת�ַ���

            /**
             *��Ϣ�ǰ��ж�ȡ
             * 1.register:<username> ����: register:����
             * 2.Ⱥ��: groupChat:<message> ����:groupChat:��Һ�
             * 3.˽��: privateChat:����:���,��Ǯ
             * 4.�˳�:bye
             */
            ALL_CLIENT_MAP.put("william", this.client);
            ALL_CLIENT_MAP.put("flyingfish", this.client);
            ALL_CLIENT_MAP.put("wyq", this.client);


//            printOnlineClient();
            while (true) {


                String data = scanner.nextLine();  //������,���ж�
                if (data.startsWith("LOGIN:") || data.startsWith("login:")) {
                    //��¼
                    String userName = data.split(":")[1];//ð�ŷָ�,ȡ��һ��
                    login(userName);
                    continue;
                }

                if (data.equals("QUERY") || data.equals("query")) {
                    //ע��
//                    String userName = data.split(":")[1];//ð�ŷָ�,ȡ��һ��
//                    register(userName);
                    String msg = printOnlineClient2();
                    this.sendMessage(this.client, msg, false);

                    continue;
                }


                if (data.startsWith("register:") || data.startsWith("register")) {
                    //ע��
                    String userName = data.split(":")[1];//ð�ŷָ�,ȡ��һ��
                    register(userName);
                    continue;
                }

//                if (data.startsWith("groupChat:")) {
//                    String message = data.split(":")[1];
//                    groupChat(message);
//                    continue;
//                }

                if (data.startsWith("SENDTO:") || data.startsWith("sendto:")) {
                    String[] segments = data.split(":");
//                    String targetUserName = segments[1].split("\\-")[0]; //ȡĿ���û���
//                    String message = segments[1].split("\\-")[1];   //��ΪҪȡ����,���������� //ȡ���͵���Ϣ����
                    String targetUserName = segments[1];
                    String message = segments[2];
                    privateChat(targetUserName, message);
                    continue;
                }

                if (data.equals("EXIT") || data.equals("exit")) {
                    //��ʾ�˳�
                    bye();
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��ǰ�ͻ����˳�
     */
    private void bye() {
        if (su == true) {
            for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
                Socket target = entry.getValue();
                if (target.equals(this.client)) {   //�������û����ҵ��Լ������Ƴ�
                    ONLINE_CLIENT_MAP.remove(entry.getKey());
                    break;
                }
                System.out.println(getCurrentUserName() + "�˳�������");
            }
            printOnlineClient();//��ӡ��ǰ�û�
            this.sendMessage(this.client, "�����˳�", false);
            su = false;
        } else {
            this.sendMessage(this.client, "���ȵ�¼", false);
        }

    }

    private String getCurrentUserName() {
        for (Map.Entry<String, Socket> entry : ONLINE_CLIENT_MAP.entrySet()) {
            Socket target = entry.getValue(); //getvalue�õ�Socket����
            if (target.equals(this.client)) { //�ų�Ⱥ�ĵ�ʱ���Լ����Լ�����Ϣ�����
                return entry.getKey();
            }
        }
        return "";
    }

    /**
     * ˽��,��targetUserName����message��Ϣ
     *
     * @param targetUserName
     * @param message
     */
    private void privateChat(String targetUserName, String message) {
        Socket target = ONLINE_CLIENT_MAP.get(targetUserName);//��ȡĿ���û���
        if (su == false) {
            this.sendMessage(this.client, "���ȵ�¼", false);

        } else if (target == null) {
            this.sendMessage(this.client, "û������û�" + targetUserName, false);
        } else {
            this.sendMessage(target, message, true);
        }
    }

    /**
     * Ⱥ��,����message
     *
     * @param message
     */
    private void groupChat(String message) {
        for (Map.Entry<String, Socket> entery : ONLINE_CLIENT_MAP.entrySet()) {
            Socket target = entery.getValue(); //getvalue�õ�Socket����
            if (target.equals(this.client)) {
                continue;            //�ų�Ⱥ�ĵ�ʱ���Լ����Լ�����Ϣ�����
            }
            this.sendMessage(target, message, true);
        }
    }

    /**
     * ��userNameΪkeyע�ᵱǰ�û�(Socket client)
     *
     * @param userName
     */
    private void register(String userName) {
        if (ALL_CLIENT_MAP.containsKey(userName)) {
            this.sendMessage(this.client, "���Ѿ�ע�����,�����ظ�ע��", false);
        } else {
            ALL_CLIENT_MAP.put(userName, this.client);
//            printOnlineClient();
            this.sendMessage(this.client, "��ϲ" + userName + "ע��ɹ�\n", false);
        }
    }
    private void login(String userName) {

        if (ALL_CLIENT_MAP.containsKey(userName)) {
            this.sendMessage(this.client, "��¼�ɹ�", false);
            ONLINE_CLIENT_MAP.put(userName, this.client);
            su = true;
        } else {
            this.sendMessage(this.client, "�޴��û��������µ�¼", false);

        }
    }

    private void sendMessage(Socket target, String message, boolean prefix) {
        OutputStream clientOutput = null;      //value��ÿһ���ͻ���
        try {
            clientOutput = target.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(clientOutput);
            if (prefix) {
                String currentUserName = this.getCurrentUserName();

                writer.write("<" + currentUserName + "˵:>" + message + "\n");
            } else {
                writer.write(message + "\n");
            }
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * ��������ӡ���߿ͻ���
     */
    private void printOnlineClient() {
        System.out.println("��ǰ��������:" + ONLINE_CLIENT_MAP.size() + "," + "�û��������б�:");
        for (String userName : ONLINE_CLIENT_MAP.keySet()) {  //Map��keyΪ�û���
            System.out.println(userName);
        }
    }

    /**
     *
     *
     */
    private String printOnlineClient2() {
        if (su) {
            System.out.println("��ǰ��������:" + ONLINE_CLIENT_MAP.size() + "," + "�û��������б�:");
            String a = "";
            for (String userName : ONLINE_CLIENT_MAP.keySet()) {  //Map��keyΪ�û���
                System.out.println(userName);
                a = a + userName + "��";
            }
            return '\n' + "��ǰ���ߵ����У�" + a;
        } else {
            return "���ȵ�¼";
        }

    }
}

