package net.azisaba.simpleProxy.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StringReader {
    private final String text;
    private int index = 0;

    public StringReader(@NotNull String text) {
        this.text = text;
    }

    @NotNull
    public String getText() {
        return text;
    }

    public int length() {
        return text.length();
    }

    public int getIndex() {
        return index;
    }

    @NotNull
    public StringReader setIndex(int index) {
        if (index > text.length() || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + " >= Size: " + text.length());
        }
        this.index = index;
        return this;
    }

    /**
     * Reads the next character. Does not update the current index.
     */
    public char peek() {
        int idx;
        if (index >= 0) {
            idx = index;
        } else {
            idx = text.length() - 1 - index;
        }
        return text.charAt(idx);
    }

    /**
     * Reads the next <code>n</code> character. Does not update the current index.
     */
    @NotNull
    public String peek(int amount) {
        if (amount == 0) return "";
        if (amount < 0) {
            return text.substring(Math.max(0, index + amount), index);
        } else {
            return text.substring(index, Math.min(text.length() - 1, index + amount));
        }
    }

    /**
     * Reads the remaining characters. Does not update the current index.
     */
    @NotNull
    public String peekRemaining() {
        int end;
        if (index < 0) {
            end = -index;
        } else {
            end = text.length();
        }
        return text.substring(index, end);
    }

    /**
     * Reads the first character from text. Updates the index by 1.
     * @return read string
     * @throws IllegalArgumentException if index is negative
     */
    @NotNull
    public String readFirst() {
        return read(1);
    }

    /**
     * Reads the first character from text. Updates the index by 1.
     * @return read char
     * @throws IllegalArgumentException if index is negative
     */
    public char readFirstAsChar() {
        return readFirst().charAt(0);
    }

    /**
     * Reads the text by provided amount. Updates the index by <code>amount</code>.
     * @param amount the amount to read text
     * @return read string
     * @throws IllegalArgumentException if amount is out of range
     */
    @NotNull
    public String read(int amount) {
        String substr = text.substring(index, index + amount);
        index += amount;
        return substr;
    }

    /**
     * Reads the text by provided amount. Updates the index by <code>amount</code>.
     * Does not throw exception when amount exceeds the string length, and supports negative amount.
     * @param amount the amount to read text
     * @return read string
     */
    @NotNull
    public String readSafe(int amount) {
        String substr;
        if (amount < 0) {
            substr = text.substring(Math.max(0, index + amount), index);
        } else {
            substr = text.substring(index, Math.min(text.length(), index + amount));
        }
        index += amount;
        return substr;
    }

    /**
     * Checks if the remaining text starts with `prefix`.
     * @param prefix the prefix
     */
    public boolean startsWith(String prefix) {
        return peekRemaining().startsWith(prefix);
    }

    /**
     * Updates the current index by `amount`.
     */
    @NotNull
    public StringReader skip(int amount) {
        this.index += amount;
        return this;
    }

    /**
     * Checks if the reader has encountered EOF.
     */
    public boolean isEOF() {
        return index >= text.length();
    }

    /**
     * Reads next token. Does not update current index.
     * @return next token, null if EOF.
     */
    @Nullable
    public String peekToken() {
        if (isEOF()) return null;
        int index = getIndex();
        StringBuilder sb = new StringBuilder();
        while (!isEOF()) {
            char read = readFirstAsChar();
            if (read == ' ') break;
            sb.append(read);
        }
        setIndex(index);
        return sb.toString();
    }

    /**
     * Reads next token.
     * @return next token, null if EOF.
     */
    @Nullable
    public String readToken() {
        if (isEOF()) return null;
        StringBuilder sb = new StringBuilder();
        while (!isEOF()) {
            char read = readFirstAsChar();
            if (read == ' ') {
                break;
            }
            sb.append(read);
        }
        return sb.toString();
    }

    /**
     * Copy StringReader with current index and text.
     * @return new string reader
     */
    @NotNull
    public StringReader copy() {
        StringReader reader = new StringReader(text);
        reader.index = this.index;
        return reader;
    }
}
