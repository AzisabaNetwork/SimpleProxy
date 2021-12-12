package net.azisaba.simpleProxy.config;

import net.azisaba.simpleProxy.util.InvalidArgumentException;
import net.azisaba.simpleProxy.yaml.YamlArray;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RuleSet extends ArrayList<Rule> {
    public static final RuleType DEFAULT_RULE_TYPE = RuleType.ALLOW;
    private final Map<String, RuleType> cachedTypes = new HashMap<>();

    public void read(@NotNull YamlArray arr) throws InvalidArgumentException {
        for (Object o : arr) {
            String s = String.valueOf(o);
            add(Rule.parse(s));
        }
    }

    @NotNull
    public RuleType getEffectiveRuleType(@NotNull String address) {
        RuleType ruleType = cachedTypes.get(address);
        if (ruleType != null) return ruleType;
        ruleType = DEFAULT_RULE_TYPE;
        for (Rule rule : this) {
            if (rule.isNetworkContains(address)) {
                ruleType = rule.getRuleType();
                break;
            }
        }
        cachedTypes.put(address, ruleType);
        return ruleType;
    }

    @NotNull
    public RuleCheckResult getEffectiveRuleResult(@NotNull String address) {
        for (Rule rule : this) {
            if (rule.isNetworkContains(address)) {
                return new RuleCheckResult(rule.getRuleType(), rule, rule.getRawNetwork() + " contains " + address);
            }
        }
        return RuleCheckResult.DEFAULT;
    }

    @Override
    public void clear() {
        cachedTypes.clear();
        super.clear();
    }
}
