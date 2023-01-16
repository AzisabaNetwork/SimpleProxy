package net.azisaba.simpleproxy.proxy.builtin;

import io.netty.channel.Channel;
import net.azisaba.simpleproxy.proxy.builtin.echo.EchoServer;
import net.azisaba.simpleproxy.proxy.builtin.echo.EchoTimeServer;
import net.azisaba.simpleproxy.proxy.connection.ChannelInboundDiscardHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BuiltinTypeHandler {
    public static void onConnectionInit(@Nullable String type, @NotNull Channel ch) {
        if ("echo".equals(type)) {
            if (ch.pipeline().names().contains("message_forwarder")) ch.pipeline().remove("message_forwarder");
            ch.pipeline().addLast("echo_server", new EchoServer()).addLast("discard", new ChannelInboundDiscardHandler());
        } else if ("echo-time".equals(type)) {
            if (ch.pipeline().names().contains("message_forwarder")) ch.pipeline().remove("message_forwarder");
            ch.pipeline().addLast("echo_time_server", new EchoTimeServer()).addLast("discard", new ChannelInboundDiscardHandler());
        }
    }
}
