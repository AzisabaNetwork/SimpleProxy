package net.azisaba.simpleProxy.proxy.util;

import io.netty.util.ReferenceCounted;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.function.Supplier;

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
        while (!Thread.currentThread().isInterrupted()) {
            int i = System.in.read();
            if (i == -1 || i == 10) return sb.toString();
            char c = (char) i;
            sb.append(c);
        }
        return sb.toString();
    }

    @NotNull
    public static String repeat(@NotNull String s, int times) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < times; i++) result.append(s);
        return result.toString();
    }

    public static <T> T getOrGet(Supplier<T> valueSupplier, Supplier<T> defSupplier) {
        T value = valueSupplier.get();
        if (value == null) return defSupplier.get();
        return value;
    }

    /**
     * Try to release the object and returns the number of freed objects.
     * @param o the object, this can be an instance of ReferenceCounted or Iterable to free all elements.
     * @return the number of freed objects.
     */
    public static int release(@Nullable Object o) {
        if (o == null) {
            return 0;
        }
        if (o instanceof ReferenceCounted) {
            if (((ReferenceCounted) o).release()) {
                return 1; // return 1 only if the object has been deallocated
            }
        } else if (o instanceof Iterable<?>) {
            Iterable<?> itr = (Iterable<?>) o;
            int freed = 0;
            for (Object o1 : itr) {
                freed += release(o1);
            }
            return freed;
        }
        return 0;
    }
}
