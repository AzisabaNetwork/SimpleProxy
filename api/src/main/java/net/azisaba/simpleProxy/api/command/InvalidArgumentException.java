package net.azisaba.simpleProxy.api.command;

import net.azisaba.simpleProxy.api.util.StringReader;
import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidArgumentException extends Exception {
    private String generatedMessage = "<message not yet generated>";
    private StringReader context;
    private int length = 1;

    public InvalidArgumentException() {
        super();
    }

    public InvalidArgumentException(@Nullable String message) {
        this(message, null);
    }

    public InvalidArgumentException(@Nullable Throwable cause) {
        this(null, cause);
    }

    public InvalidArgumentException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
        generateMessage();
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
        generateMessage();
        return this;
    }

    @NotNull
    public InvalidArgumentException withContext(@NotNull StringReader reader, int offset, int length) {
        this.context = reader.copy();
        this.context.setIndex(Math.min(this.context.getText().length(), Math.max(0, this.context.getIndex() + offset)));
        this.length = Math.max(1, length);
        generateMessage();
        return this;
    }

    private void generateMessage() {
        try {
            StringBuilder sb = new StringBuilder(super.getMessage());
            if (context != null) {
                int index = context.getIndex();
                String prev = context.peek(-10);
                String next = context.readSafe(length + 10);
                int cursor = Math.min(10, index);
                sb.append("\n");
                if (index > 10) sb.append("...");
                sb.append(prev).append(next);
                if (context.length() > index + 10) sb.append("...");
                sb.append("\n");
                if (index > 10) sb.append("   ");
                sb.append(Strings.repeat(" ", cursor)).append("^").append(Strings.repeat("~", length - 1));
                context.setIndex(index);
            }
            this.generatedMessage = sb.toString();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    @Override
    public String getMessage() {
        return this.generatedMessage;
    }
}
