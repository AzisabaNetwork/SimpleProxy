package net.azisaba.simpleProxy.proxy.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@SuppressWarnings("unused")
public enum Words {
    $INVALID,
    $IP_ADDRESS,
    $NULL($IP_ADDRESS),
    IP($IP_ADDRESS),
    FROM(IP, $IP_ADDRESS),
    CONNECTION(FROM),
    ;

    @SuppressWarnings("ImmutableEnumChecker")
    private final List<Words> acceptableWords;

    Words(Words... acceptableWords) {
        this.acceptableWords = Arrays.asList(acceptableWords);
    }

    public boolean isAllowedForNextWord(@NotNull Words word) {
        return acceptableWords.contains(word);
    }

    @NotNull
    public static Words parse(@NotNull String input) {
        if (input.length() == 0) return $INVALID;
        if (input.startsWith("$")) return $INVALID;
        try {
            return Words.valueOf(input.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignore) {}
        try {
            return Words.valueOf(input.toUpperCase(Locale.ROOT).substring(0, input.length() - 1));
        } catch (IllegalArgumentException ignore) {}
        return $INVALID;
    }
}
