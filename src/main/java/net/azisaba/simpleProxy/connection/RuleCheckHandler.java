package net.azisaba.simpleProxy.connection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleProxy.config.ProxyConfig;
import net.azisaba.simpleProxy.config.RuleCheckResult;
import net.azisaba.simpleProxy.config.RuleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@ChannelHandler.Sharable
public class RuleCheckHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();
    private boolean denied = false;

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        if (!(socketAddress instanceof InetSocketAddress)) return;
        String hostAddress = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
        RuleType ruleType = ProxyConfig.rules.getEffectiveRuleType(hostAddress);
        if (ruleType == RuleType.DENY) {
            ctx.channel().close();
            denied = true;
            return;
        }
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(@NotNull ChannelHandlerContext ctx) throws Exception {
        if (denied) {
            if (ProxyConfig.debug) {
                SocketAddress socketAddress = ctx.channel().remoteAddress();
                if (!(socketAddress instanceof InetSocketAddress)) return;
                String hostAddress = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
                RuleCheckResult result = ProxyConfig.rules.getEffectiveRuleResult(hostAddress);
                LOGGER.info("Denied connection from {} because {}", ctx.channel().remoteAddress(), result.getReason());
            } else if (ProxyConfig.verbose) {
                LOGGER.info("Denied connection from {}", ctx.channel().remoteAddress());
            }
            return;
        }
        super.channelInactive(ctx);
    }
}
