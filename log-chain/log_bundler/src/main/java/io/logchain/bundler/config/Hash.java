package io.logchain.bundler.config;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

public enum Hash implements Function<byte[], byte[]> {
    NONE(Function.identity()),
    SHA256((message) -> {
        return digest("SHA-256", (byte[]) message);
    }),
    SHA384((message) -> {
        return digest("SHA-384", (byte[]) message);
    }),
    SHA3_256((message) -> {
        return digest("SHA3-256", (byte[]) message);
    }),
    SHA3_384((message) -> {
        return digest("SHA3-384", (byte[]) message);
    });

    private final Function<byte[], byte[]> implementation;

    /** @deprecated */
    @Deprecated
    public static byte[] sha256(byte[] message) {
        return SHA256.apply(message);
    }

    private Hash(final Function implementation) {
        this.implementation = implementation;
    }

    public byte[] apply(byte[] message) {
        return (byte[])this.implementation.apply(message);
    }

    private static byte[] digest(String algorithm, byte[] message) {
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            return digest.digest(message);
        } catch (NoSuchAlgorithmException var3) {
            throw new AssertionError(var3);
        }
    }
}