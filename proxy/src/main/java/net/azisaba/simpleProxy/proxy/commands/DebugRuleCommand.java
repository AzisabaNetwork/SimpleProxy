package net.azisaba.simpleProxy.proxy.commands;

import net.azisaba.simpleProxy.proxy.config.ProxyConfig;
import net.azisaba.simpleProxy.proxy.config.RuleCheckResult;
import net.azisaba.simpleProxy.proxy.config.RuleType;
import net.azisaba.simpleProxy.api.command.CommandHandler;
import net.azisaba.simpleProxy.proxy.util.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DebugRuleCommand implements CommandHandler {
    private static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void execute(@NotNull List<String> args) {
        if (args.isEmpty()) {
            LOGGER.info(Util.ANSI_RED + "Usage: debugrule <ip address or subnet>" + Util.ANSI_RESET);
            return;
        }
        String address = args.get(0);
        RuleCheckResult result = ProxyConfig.rules.getEffectiveRuleResult(address);
        LOGGER.info(Util.ANSI_CYAN + "Check result for {}:" + Util.ANSI_RESET, address);
        String resultColor;
        if (result.getRuleType() == RuleType.ALLOW) {
            resultColor = Util.ANSI_GREEN;
        } else {
            resultColor = Util.ANSI_RED;
        }
        LOGGER.info(Util.ANSI_YELLOW + "  Result: {}{}" + Util.ANSI_RESET, resultColor, result.getRuleType().getName());
        String cause;
        if (result.getCause() == null) {
            cause = "null";
        } else {
            cause = result.getCause().getRawString();
        }
        LOGGER.info(Util.ANSI_YELLOW + "  Cause: {}{}" + Util.ANSI_RESET, Util.ANSI_CYAN, cause);
        LOGGER.info(Util.ANSI_YELLOW + "  Reason: {}{}" + Util.ANSI_RESET, Util.ANSI_CYAN, result.getReason());
    }

    @Override
    public @Nullable String getDescription() {
        return "Checks what rule is applied for specific IP address.";
    }
}
