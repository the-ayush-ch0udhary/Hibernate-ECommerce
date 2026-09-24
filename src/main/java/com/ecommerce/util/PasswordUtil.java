package com.ecommerce.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 * Utility for hashing and verifying passwords securely with BCrypt.
 */
public final class PasswordUtil {

    private static final int BCRYPT_WORK_FACTOR = 12;

    private PasswordUtil() {
        // Private constructor to prevent instantiation
    }

    /**
     * Hashes a plain-text password using BCrypt with a secure salt.
     *
     * @param plainPassword the plain-text password
     * @return the hashed password string
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        return BCrypt.hashpw(plainPassword, BCrypt.gensalt(BCRYPT_WORK_FACTOR));
    }

    /**
     * Verifies a plain-text password against a stored BCrypt hash.
     *
     * @param plainPassword the plain-text password
     * @param hashedPassword the hashed password
     * @return true if matches, false otherwise
     */
    public static boolean checkPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plainPassword, hashedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
