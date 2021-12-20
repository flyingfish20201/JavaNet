package netty;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;

public class EchoServer {
    public static void main(String[] args) {
        //创建一个group，用于处理客户端连接请求
        NioEventLoopGroup parentGroup = new NioEventLoopGroup();
        //创建一个group，用于服务器与客户端发送数据，并与通道一一绑定
        NioEventLoopGroup childGroup = new NioEventLoopGroup();

        try {
            //bootstrap初始化信道
            ServerBootstrap bootstrap = new ServerBootstrap();
            //指定要使用的两个group
            bootstrap.group(parentGroup, childGroup)
                    .channel(NioServerSocketChannel.class)//指定要创建的channel的类型，并创建类型的channel
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        //初始化channel的方法
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception { //指定要使用的处理器
                            //channel 一旦创建完毕，其就会同时绑定一个pipeline
                            ChannelPipeline pipeline = ch.pipeline();
                            //添加编码器
                            pipeline.addLast(new StringEncoder());
                            //添加解码器
                            pipeline.addLast(new StringEncoder());
                            //添加自定义的处理器
                            pipeline.addLast(new EchoServerHander());

                        }
                    });
            //创建channel，绑定到指定的主机
            //sync()将异步变为同步
            ChannelFuture future = null;
            future = bootstrap.bind(8888).sync();
            System.out.println("服务器已经启动...");
            //当channel被关闭后，会触发closeFuture()的执行，完成一些收尾工作
            future.channel().closeFuture().sync();//异步变成同步
        } catch (InterruptedException e) {
            e.printStackTrace();

        } finally {
            //将两个group进行关闭
            parentGroup.shutdownGracefully();
            childGroup.shutdownGracefully();

        }

    }
}

