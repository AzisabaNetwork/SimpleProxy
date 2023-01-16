package net.azisaba.simpleproxy.proxy.config;

import net.azisaba.simpleproxy.api.config.ProxyConfig;

public class ProxyConfigImpl implements ProxyConfig {
    @Override
    public boolean isDebug() {
        return ProxyConfigInstance.debug;
    }

    @Override
    public boolean isVerbose() {
        return ProxyConfigInstance.verbose;
    }

    @Override
    public boolean isEpoll() {
        return ProxyConfigInstance.isEpoll();
    }
}
