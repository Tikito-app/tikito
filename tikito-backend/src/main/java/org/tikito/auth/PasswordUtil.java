package org.tikito.auth;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.nio.CharBuffer;

public final class PasswordUtil {
    private PasswordUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Return bcrypt password hash
     *
     * @param plaintext toCharArray() of password
     * @return Stringified password hash
     */
    public static String createBcryptHash(final char[] plaintext) {
        return new BCryptPasswordEncoder().encode(CharBuffer.wrap(plaintext));
    }

    /**
     * Checks whether bcrypt hash matches the string
     *
     * @param plaintext char[] of plaintext string
     * @param hash      String bcrypt hash
     * @return true if hash is a correct Bcrypt hash for the given plaintext
     */
    public static boolean checkBcryptHash(final char[] plaintext, final String hash) {
        return new BCryptPasswordEncoder().matches(CharBuffer.wrap(plaintext), hash);
    }
}
