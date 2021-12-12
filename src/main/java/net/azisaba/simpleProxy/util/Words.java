package net.azisaba.simpleProxy.util;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public enum Words {
    $INVALID,
    $IP_ADDRESS,
    $NULL($IP_ADDRESS),
    IP($IP_ADDRESS),
    FROM(IP, $IP_ADDRESS),
    CONNECTION(FROM),
    ;

    private final List<Words> nextAllowedWords;

    Words(Words... nextAllowedWords) {
        this.nextAllowedWords = Arrays.asList(nextAllowedWords);
    }

    public boolean isAllowedForNextWord(@NotNull Words word) {
        return nextAllowedWords.contains(word);
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
