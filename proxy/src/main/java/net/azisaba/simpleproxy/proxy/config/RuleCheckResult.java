package net.azisaba.simpleproxy.proxy.config;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RuleCheckResult {
    public static final RuleCheckResult DEFAULT = new RuleCheckResult(RuleSet.DEFAULT_RULE_TYPE, null, "Default rule type: " + RuleSet.DEFAULT_RULE_TYPE.getName());
    private final RuleType ruleType;
    private final Rule cause;
    private final String reason;

    public RuleCheckResult(@NotNull RuleType ruleType, @Nullable Rule cause, @NotNull String reason) {
        this.ruleType = ruleType;
        this.cause = cause;
        this.reason = reason;
    }

    @NotNull
    public RuleType getRuleType() {
        return ruleType;
    }

    @Nullable
    public Rule getCause() {
        return cause;
    }

    @NotNull
    public String getReason() {
        return reason;
    }
}
