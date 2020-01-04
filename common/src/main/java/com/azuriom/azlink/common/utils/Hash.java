package com.azuriom.azlink.common.utils;


import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;

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
    SHA_512("SHA-512"),

    /**
     * Argon2i.
     */

    ARGON2i("Argon2i"),

    /**
     * Argon2d.
     */

    ARGON2d("Argon2d"),

    /**
     * Argon2id
     */

    ARGON2id("Argon2id");


    private final String name;

    Hash(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String hash(String text) {

        byte[] hash;


        if (getName().equals(MD5.getName()) || getName().equals(SHA_1.getName()) || getName().equals(SHA_256.getName()) || getName().equals(SHA_384.getName()) || getName().equals(SHA_512.getName())) {
            try {

                MessageDigest digest = MessageDigest.getInstance(name);

                hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));

            } catch (NoSuchAlgorithmException e) {

                throw new UnsupportedOperationException(name + " is not supported on this platform", e);
            }

        } else {
            Argon2 argon2;

            if (getName().equals(ARGON2i.getName())) {

                argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2i);

            } else if (getName().equals(ARGON2d.getName())) {

                argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2d);

            } else {

                argon2 = Argon2Factory.create(Argon2Factory.Argon2Types.ARGON2id);

            }

            hash = argon2.hash(1, 512 * 512, 2, text).getBytes(StandardCharsets.UTF_8);

        }

        StringBuilder result = new StringBuilder(2 * hash.length);

        for (byte b : hash) {

            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));

        }

        return result.toString();
    }
}
