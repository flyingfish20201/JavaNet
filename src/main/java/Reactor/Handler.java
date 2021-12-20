package Reactor;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class Handler implements Runnable {

    /**
     * ����ʹ����״̬������ֹ���̳߳������ݲ�һ�µ�����
     **/
    static final int PROCESSING = 1;
    static final int PROCESSED = 2;
    private static final ExecutorService pool = Executors.newFixedThreadPool(4);
    private SocketChannel socketChannel;
    private SelectionKey selectionKey;
    private ByteBuffer oldBuffer;
    private volatile int state = PROCESSED;

    public Handler(SocketChannel socketChannel, Selector selector) throws IOException {

        // ��ʼ����oldBufferΪnull
        oldBuffer = null;
        this.socketChannel = socketChannel;
        socketChannel.configureBlocking(false);

        // �ڹ��캯�����ע��ͨ����Selector
        this.selectionKey = socketChannel.register(selector, SelectionKey.OP_READ);
        // attach(this)���������󶨵�key�ϣ�������ʹdispatch()������ȷʹ��
        selectionKey.attach(this);
        // Selector.wakeup()������ʹ�����е�Selector.select()�������̷���
        selector.wakeup();
    }

    /**
     * ��ȡByteBufferֱ��һ�е�ĩβ ������һ�е����ݣ��������з�
     *
     * @param buffer
     * @return String ��ȡ����ĩ�����ݣ��������з� ; null ���û�л��з�
     * @throws UnsupportedEncodingException
     */
    private static String readLine(ByteBuffer buffer) throws UnsupportedEncodingException {
        // windows�еĻ��з���ʾ�ֶ� "\r\n"
        // ����windows��������͵Ļ��з��ǻ���CR��LF
        char CR = '\r';
        char LF = '\n';

        boolean crFound = false;
        int index = 0;
        int len = buffer.limit();
        buffer.rewind();
        while (index < len) {
            byte temp = buffer.get();
            if (temp == CR) {
                crFound = true;
            }
            if (crFound && temp == LF) {
                // Arrays.copyOf(srcArr,length)�����᷵��һ�� Դ�����еĳ��ȵ�lengthλ ��������
                return new String(Arrays.copyOf(buffer.array(), index + 1), "utf-8");
            }
            index++;
        }
        return null;
    }

    /**
     * �Դ����Buffer����ƴ��
     *
     * @param oldBuffer
     * @param newBuffer
     * @return ByteBuffer ƴ�Ӻ��Buffer
     */
    public static ByteBuffer mergeBuffer(ByteBuffer oldBuffer, ByteBuffer newBuffer) {
        // ���ԭ����Buffer��null��ֱ�ӷ���
        if (oldBuffer == null) {
            return newBuffer;
        }
        // ���ԭ����Buffer��ʣ�೤�ȿ������µ�buffer��ֱ��ƴ��
        newBuffer.rewind();
        if (oldBuffer.remaining() > (newBuffer.limit() - newBuffer.position())) {
            return oldBuffer.put(newBuffer);
        }

        // �������������������͹����µ�Buffer����ƴ��
        int oldSize = oldBuffer != null ? oldBuffer.limit() : 0;
        int newSize = newBuffer != null ? newBuffer.limit() : 0;
        ByteBuffer result = ByteBuffer.allocate(oldSize + newSize);

        result.put(Arrays.copyOfRange(oldBuffer.array(), 0, oldSize));
        result.put(Arrays.copyOfRange(newBuffer.array(), 0, newSize));

        return result;
    }

    // ʹ���̳߳�ִ��
    @Override
    public void run() {
        if (state == PROCESSED) {
            // �����ʱû���߳��ڴ����ͨ���ı��ζ�ȡ�����ύ���뵽�̳߳ؽ��ж�д����
            pool.execute(new process(selectionKey));
        } else {
            // �����ʱ���߳����ڽ��ж�д��������ֱ��return��ѡ�����������һ��ѡ����������
            return;
        }
    }

    /**
     * ��ȡһ�е����ݣ����������з�
     *
     * @param line
     * @return String �е�����
     * @throws UnsupportedEncodingException
     */
    private String readLineContent(String line) throws UnsupportedEncodingException {
        System.out.print(line);
        System.out.print(line.length());
        return line.substring(0, line.length() - 2);
    }

    /**
     * �ڲ���ʵ�ֶ�ͨ�����ݵĶ�ȡ����ͷ���
     *
     * @author CringKong
     */
    private class process implements Runnable {

        private SelectionKey selectionKey;

        public process(SelectionKey selectionKey) {
            this.selectionKey = selectionKey;
            state = PROCESSING;
        }

        /**
         * ����һ��ͬ����������Ϊ��reactor�е�ѡ�����п��ܻ����һ��״����
         * ��process�߳��Ѿ�Ҫ��ĳͨ�����ж�д��ʱ���п���Selector���ٴ�ѡ���ͨ��
         * ��Ϊ��ʱ��process�̻߳���û�������Ľ��ж�д���ᵼ����һ�߳����´���һ��process
         * ����ͼ���ж�д��������ʱ�ͻ����cpu��Դ�˷ѵ���������߳����쳣����Ϊ�߳�1�ڶ�ȡͨ�����ݵ�ʱ��
         * �߳�2�ͻᱻ���������ȵ��߳�2ִ�в�����ʱ���߳�1�Ѿ���ͨ������˶�д����
         * ��˿���ͨ�����ö���״̬������ֹ������Щ����
         *
         * @param selectionKey
         * @throws IOException
         * @throws InterruptedException
         */
        private synchronized void readDate(SelectionKey selectionKey) throws IOException, InterruptedException {

            ByteBuffer newBuffer = ByteBuffer.allocate(64);

            int read;
            while ((read = socketChannel.read(newBuffer)) <= 0) {
                state = PROCESSED;
                return;
            }

            newBuffer.flip();
            String line = readLine(newBuffer);
            if (line != null) {

                // �����ζ������н��������ͽ�ԭ���������н�������buffer�ϲ�λһ��
                String sendData = readLine(mergeBuffer(oldBuffer, newBuffer));
                if (readLineContent(sendData).equalsIgnoreCase("exit")) { // �����һ�е�������exit�ͶϿ�����
                    socketChannel.close();
                    state = PROCESSED;
                    return;
                }
                // Ȼ��ֱ�ӷ��ͻص��ͻ���
                ByteBuffer sendBuffer = ByteBuffer.wrap(sendData.getBytes("utf-8"));
                while (sendBuffer.hasRemaining()) {
                    socketChannel.write(sendBuffer);
                }
                oldBuffer = null;
            } else {
                // ������û�����н��������ͽ���ζ������ݺ�ԭ�������ݺϲ�
                oldBuffer = mergeBuffer(oldBuffer, newBuffer);
            }
            state = PROCESSED;
        }

        @Override
        public void run() {
            try {
                readDate(selectionKey);
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}