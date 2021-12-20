package netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.time.LocalDateTime;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class EchoClientHander extends ChannelInboundHandlerAdapter {
    Scanner s = new Scanner(System.in);

    //自定义客户端处理器
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //ByteBuf buf = (ByteBuf) msg;
        System.out.println(ctx.channel().remoteAddress() + ":" + msg);
        //System.out.println(ctx.channel().remoteAddress()+","+buf.toString(CharsetUtil.UTF_8));

        ctx.channel().writeAndFlush("from client：" + LocalDateTime.now());
        //ctx.write(buf);
        TimeUnit.MICROSECONDS.sleep(500);

    }

    //当channel被激活会触发该方法的执行，只能执行一次进行通信
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("请输入第一次进行连接的对话：");
        String str1 = s.next();
        ctx.channel().writeAndFlush(str1);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
