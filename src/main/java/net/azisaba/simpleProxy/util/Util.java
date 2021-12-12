package net.azisaba.simpleProxy.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class Util {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    @Contract(pure = true)
    @NotNull
    public static String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            int i = System.in.read();
            if (i == -1 || i == 10) return sb.toString();
            char c = (char) i;
            sb.append(c);
        }
    }

    @NotNull
    public static String repeat(@NotNull String s, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) result.append(s);
        return result.toString();
    }
}
