package net.azisaba.simpleProxy.proxy.connection;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.NotNull;

public class ChannelInboundDiscardHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
        if (msg instanceof ReferenceCounted) {
            ((ReferenceCounted) msg).release();
        }
    }
}
