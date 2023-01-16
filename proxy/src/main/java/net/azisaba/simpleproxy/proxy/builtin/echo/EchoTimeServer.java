package net.azisaba.simpleproxy.proxy.builtin.echo;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.jetbrains.annotations.NotNull;

public class EchoTimeServer extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) msg;
            ByteBuf buf = Unpooled.buffer();
            buf.writeLong(System.currentTimeMillis());
            DatagramPacket newPacket = new DatagramPacket(buf, packet.sender(), packet.recipient());
            ctx.writeAndFlush(newPacket);
        } else if (msg instanceof ByteBuf) {
            ByteBuf buf = Unpooled.buffer();
            buf.writeLong(System.currentTimeMillis());
            ctx.writeAndFlush(buf);
        }
        super.channelRead(ctx, msg);
    }
}
