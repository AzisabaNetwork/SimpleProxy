package net.azisaba.simpleproxy.proxy.connection;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.azisaba.simpleproxy.proxy.config.ProxyConfigInstance;
import net.azisaba.simpleproxy.proxy.config.RuleCheckResult;
import net.azisaba.simpleproxy.proxy.config.RuleType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

@ChannelHandler.Sharable
public class RuleCheckHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final RuleCheckHandler INSTANCE = new RuleCheckHandler();

    private RuleCheckHandler() {}

    @Override
    public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
        SocketAddress socketAddress = ctx.channel().remoteAddress();
        if (!(socketAddress instanceof InetSocketAddress)) return;
        String hostAddress = ((InetSocketAddress) socketAddress).getAddress().getHostAddress();
        RuleType ruleType = ProxyConfigInstance.rules.getEffectiveRuleType(hostAddress);
        if (ruleType == RuleType.DENY) {
            if (ProxyConfigInstance.debug) {
                RuleCheckResult result = ProxyConfigInstance.rules.getEffectiveRuleResult(hostAddress);
                LOGGER.info("Denied connection from {} because {}", ctx.channel().remoteAddress(), result.getReason());
            } else if (ProxyConfigInstance.verbose) {
                LOGGER.info("Denied connection from {}", ctx.channel().remoteAddress());
            }
            ctx.channel().close();
            return;
        } else if (ruleType == RuleType.ALLOW) {
            if (ProxyConfigInstance.debug) {
                RuleCheckResult result = ProxyConfigInstance.rules.getEffectiveRuleResult(hostAddress);
                LOGGER.info("Allowed connection from {} because {}", ctx.channel().remoteAddress(), result.getReason());
            }
        }
        ctx.channel().pipeline().remove(this);
        super.channelActive(ctx);
    }
}
