package com.azuriom.azlink.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple class to create md5 and sha hash.
 *
 * <p>md5 and sha1 shouldn't be use anymore.</p>
 */
public enum Hash {

    @Deprecated
    MD5("MD5"),
    @Deprecated
    SHA_1("SHA-1"),
    SHA_256("SHA-256"),
    SHA_384("SHA-384"),
    SHA_512("SHA-512");

    private final String name;

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String hash(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance(name);
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            StringBuilder result = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }

            return result.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(name + " is not supported on this platform", e);
        }
    }
}
