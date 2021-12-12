package net.azisaba.simpleProxy.util;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class InvalidArgumentException extends Exception {
    private StringReader context;
    private int length = 1;

    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(@Nullable String message) {
        super(message);
    }

    public InvalidArgumentException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }

    public InvalidArgumentException(@Nullable Throwable cause) {
        super(cause);
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static InvalidArgumentException createStringDoesNotStartWith(char c) {
        return new InvalidArgumentException("String does not start with '" + c + "'");
    }

    @Contract(value = "-> new", pure = true)
    @NotNull
    public static InvalidArgumentException createUnexpectedEOF() {
        return new InvalidArgumentException("Encountered unexpected EOF");
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static InvalidArgumentException createUnexpectedEOF(@NotNull String what) {
        return new InvalidArgumentException("Encountered unexpected EOF while looking for " + what);
    }

    @Contract(value = "_ -> new", pure = true)
    @NotNull
    public static InvalidArgumentException createUnexpectedEOF(char c) {
        return new InvalidArgumentException("Encountered unexpected EOF while looking for '" + c + "'");
    }

    @NotNull
    public InvalidArgumentException withContext(@NotNull StringReader reader) {
        this.context = reader;
        return this;
    }

    @NotNull
    public InvalidArgumentException withContext(@NotNull StringReader reader, int offset, int length) {
        this.context = reader.copy();
        this.context.setIndex(Math.min(this.context.getText().length(), Math.max(0, this.context.getIndex() + offset)));
        this.length = Math.max(1, length);
        return this;
    }

    @Override
    public String getMessage() {
        try {
            StringBuilder sb = new StringBuilder(super.getMessage());
            if (context != null) {
                int index = context.getIndex();
                String prev = context.peek(-10);
                String next = context.readSafe(Math.max(10, length));
                int cursor = Math.min(10, index);
                sb.append("\n").append(prev).append(next);
                sb.append("\n").append(Strings.repeat(" ", cursor)).append("^").append(Strings.repeat("~", length - 1));
                context.setIndex(index);
            }
            return sb.toString();
        } catch (Throwable t) {
            System.err.println("uh oh");
            t.printStackTrace();
            return super.toString() + Arrays.toString(t.getStackTrace());
        }
    }
}
