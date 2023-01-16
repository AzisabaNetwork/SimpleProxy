package net.azisaba.simpleproxy.proxy.builtin.echo;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.DatagramPacket;
import org.jetbrains.annotations.NotNull;

public class EchoServer extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) throws Exception {
        if (msg instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) msg;
            DatagramPacket newPacket = new DatagramPacket(packet.content(), packet.sender(), packet.recipient());
            ctx.writeAndFlush(newPacket);
        } else if (msg instanceof ByteBuf) {
            ctx.writeAndFlush(((ByteBuf) msg).copy());
        } else {
            ctx.writeAndFlush(msg);
        }
        super.channelRead(ctx, msg);
    }
}
