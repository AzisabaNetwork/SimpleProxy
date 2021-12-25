package net.azisaba.simpleProxy.api.plugin.loader;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.security.CodeSigner;

public class PluginFileEntry {
    @NotNull private final InputStream inputStream;
    @Nullable private final CodeSigner[] codeSigners;

    public PluginFileEntry(@NotNull InputStream inputStream, @Nullable CodeSigner[] codeSigners) {
        this.inputStream = inputStream;
        this.codeSigners = codeSigners;
    }

    @NotNull
    public InputStream getInputStream() {
        return inputStream;
    }

    @Nullable
    public CodeSigner[] getCodeSigners() {
        return codeSigners;
    }
}
