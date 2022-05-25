package net.azisaba.simpleProxy.proxy.util;

import net.azisaba.simpleProxy.proxy.ProxyInstance;

public class MemoryReserve {
    private static byte[] reserve = null; // 5 MB

    public static void reserve() {
        if (reserve == null) {
            reserve = new byte[1024 * 1024 * 5];
        }
    }

    public static void release() {
        reserve = null;
    }

    public static void tryShutdownGracefully() {
        try {
            release();
            System.gc();
            ProxyInstance.getInstance().stop();
            System.exit(0);
        } catch (Throwable t) {
            System.exit(0xc0000001);
        }
    }
}
