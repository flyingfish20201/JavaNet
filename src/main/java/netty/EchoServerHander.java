package netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

public class EchoServerHander extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        //输出从客户端发送来的数据
        System.out.println(ctx.channel().remoteAddress() + "," + buf.toString(CharsetUtil.UTF_8));
        //给客户端回送数据
        ctx.channel().writeAndFlush("server send：" + buf.toString(CharsetUtil.UTF_8));

        TimeUnit.MICROSECONDS.sleep(500);
    }

    //异常处理
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        //关闭channel
        ctx.close();

    }
}
