package com.azuriom.azlink.common.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Simple class to compare MD5 and SHA hashes.
 *
 * <p>MD5 and SHA-1 shouldn't be used anymore.</p>
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
        return this.name;
    }

    /**
     * Hashes {@code text}, decodes the expected hexadecimal hash
     * into bytes, then compares both byte arrays.
     */
    public boolean matches(String text, String expectedHexHash) {
        if (expectedHexHash == null) {
            return false;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(this.name);
            byte[] actualHash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            byte[] expectedHash = decodeHex(expectedHexHash);

            return expectedHash != null && MessageDigest.isEqual(actualHash, expectedHash);
        } catch (NoSuchAlgorithmException e) {
            throw new UnsupportedOperationException(this.name + " is not supported on this platform", e);
        }
    }

    private static byte[] decodeHex(String value) {
        byte[] result = new byte[value.length() / 2];

        for (int i = 0; i < result.length; i++) {
            int high = Character.digit(value.charAt(i * 2), 16);
            int low = Character.digit(value.charAt(i * 2 + 1), 16);

            if (high < 0 || low < 0) {
                return null;
            }

            result[i] = (byte) ((high << 4) | low);
        }

        return result;
    }
}
