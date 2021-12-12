package net.azisaba.simpleProxy.config;

import inet.ipaddr.IPAddressString;
import net.azisaba.simpleProxy.util.InvalidArgumentException;
import net.azisaba.simpleProxy.util.StringReader;
import net.azisaba.simpleProxy.util.Words;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Objects;

public class Rule {
    private final RuleType ruleType;
    private final String rawNetwork;
    private final IPAddressString network;
    private final String rawString;

    // Example of acceptable syntax:
    // - deny connections from 1.1.1.1/32
    // - deny from ip 1.1.1.1/32
    // - deny 1.1.1.1
    // - allow 8.8.8.8
    // - deny 10.0.0.0/24
    // - deny connection from ip 0.0.0.0/0
    // - allow ::1
    // Example of unacceptable (invalid) syntax:
    // - drop from 1.1.1.1 (rule type of "drop" isn't supported)
    // - accept from 1.1.1.1 (rule type of "accept" does not exist)
    // - pls accept from all (first token must be rule type)
    // - deny from all (please specify 0.0.0.0/0 (v4) and/or ::/0 (v6) manually)
    @Contract(pure = true)
    @NotNull
    public static Rule parse(@NotNull String input) throws InvalidArgumentException {
        StringReader reader = new StringReader(input);
        int tokens = 0;
        Words currentWord = Words.$NULL;
        RuleType ruleType = null;
        String rawNetwork = null;
        IPAddressString network = null;
        while (!reader.isEOF()) {
            int index = reader.getIndex();
            String token = reader.readToken();
            if (token == null) break;
            tokens++;
            int offset = index - reader.getIndex();
            if (tokens == 1) {
                try {
                    ruleType = RuleType.valueOf(token.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException e) {
                    throw new InvalidArgumentException("Invalid rule type").withContext(reader, offset, token.length());
                }
                continue;
            }
            if (tokens == 2 || currentWord.isAllowedForNextWord(Words.$IP_ADDRESS)) {
                rawNetwork = token;
                network = new IPAddressString(token);
                if (network.isValid()) {
                    if (!reader.isEOF()) {
                        throw new InvalidArgumentException("Expected EOF, but found " + reader.peekRemaining().length() + " unread characters").withContext(reader, 0, reader.peekRemaining().length());
                    }
                    break;
                }
                currentWord = Words.parse(token);
                if (currentWord == Words.$INVALID) {
                    throw new InvalidArgumentException("Invalid token").withContext(reader, offset, token.length());
                }
            }
        }
        if (ruleType == null) throw InvalidArgumentException.createUnexpectedEOF("ruleType").withContext(reader);
        if (rawNetwork == null) throw InvalidArgumentException.createUnexpectedEOF("IP address or CIDR notation").withContext(reader);
        if (!network.isValid()) throw new InvalidArgumentException("Invalid IP address or CIDR notation: " + rawNetwork).withContext(reader);
        return new Rule(ruleType, rawNetwork, input);
    }

    public Rule(@NotNull RuleType ruleType, @NotNull String network, @Nullable String rawString) {
        this.ruleType = ruleType;
        this.rawNetwork = network;
        this.network = new IPAddressString(network);
        this.rawString = rawString;
    }

    @NotNull
    public RuleType getRuleType() {
        return ruleType;
    }

    @NotNull
    public String getRawNetwork() {
        return rawNetwork;
    }

    @NotNull
    public IPAddressString getNetwork() {
        return network;
    }

    @Nullable
    public String getRawString() {
        return rawString;
    }

    public boolean isNetworkContains(@NotNull String another) {
        if (Objects.equals(rawNetwork, another)) return true;
        return isNetworkContains(new IPAddressString(another));
    }

    public boolean isNetworkContains(@NotNull IPAddressString another) {
        return network.contains(another);
    }
}
