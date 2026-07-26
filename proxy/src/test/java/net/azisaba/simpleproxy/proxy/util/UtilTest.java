package net.azisaba.simpleproxy.proxy.util;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UtilTest {
    @Test
    void returnsNullAtEofWithoutInput() throws IOException {
        assertNull(Util.readLine(input("")));
    }

    @Test
    void returnsEmptyStringForEmptyLine() throws IOException {
        assertEquals("", Util.readLine(input("\n")));
    }

    @Test
    void returnsRemainingCharactersWhenEofFollowsInput() throws IOException {
        assertEquals("reload", Util.readLine(input("reload")));
    }

    @Test
    void returnsCharactersBeforeLineFeed() throws IOException {
        assertEquals("reload", Util.readLine(input("reload\nignored")));
    }

    private static ByteArrayInputStream input(String value) {
        return new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8));
    }
}
