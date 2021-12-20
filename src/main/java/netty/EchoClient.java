package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class EchoClient {
    public static void main(String[] args) {
        NioEventLoopGroup group = new NioEventLoopGroup();

        try {
            //bootstrap用于初始化channel
            //server端使用Serverbootstrap
            Bootstrap bootstrap = new Bootstrap();
            //指定一个group
            //服务端需要两个group
            bootstrap.group(group)
                    .localAddress(8012)//指定端口
                    //指定要创建的channel的信道
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //添加编码器
                            pipeline.addLast(new StringEncoder());
                            //添加解码器
                            pipeline.addLast(new StringDecoder());
                            //添加自定义处理器
                            pipeline.addLast(new EchoClientHander());
                        }
                    });
            //sync异步边同步
            ChannelFuture future = null;

            future = bootstrap.connect("localhost", 8888).sync();
            //执行收尾操作
            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            group.shutdownGracefully();//关闭group
        }

    }
}
