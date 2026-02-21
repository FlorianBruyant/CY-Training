package org.cytraining.backend.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder.BCryptVersion;

/**
 * Class used to hash passwords in a cryptographic secure way.
 * It uses the implementation of BCrypt from the SpringSecurityCrypto.
 */
public class PasswordHasher {
    // singleton class
    private static final PasswordHasher that = new PasswordHasher();

    private final BCryptPasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom;

    private PasswordHasher() {
        // TODO have a .env part to change those parameters?
        this.secureRandom = new SecureRandom();
        // the greater the strength, the better the password will be hashed, but the
        // higher the CPU load will be
        this.passwordEncoder = new BCryptPasswordEncoder(BCryptVersion.$2B, 12, this.secureRandom);
    }

    /**
     * Encore a password into a cryptographic secure hash.
     *
     * @param password The password to hash.
     * @return The hashed password.
     */
    public static String hash(String password) {
        // it is aknow limitation that long password can be truncated if they exceed a
        // certain lentgh
        // if it is the case, we will pre hash the password, before encoding it
        if (password.getBytes(StandardCharsets.UTF_8).length > 72) {
            // create a salt
            byte[] salt = new byte[16];
            that.secureRandom.nextBytes(salt);
            // prepare the hash
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                Log.fatal("No algorithm found", e);
                return null;
            }
            md.update(salt);
            byte[] hashed = md.digest(password.getBytes(StandardCharsets.UTF_8));
            // replace the password with its hashed version
            password = Base64.getEncoder().encodeToString(hashed);
        }

        // secure hash
        return that.passwordEncoder.encode(password);
    }

    /**
     * Check if the provided password matched the hash.
     *
     * @param password Plain text password.
     * @param hash     Hashed version of the password
     * @return True if the password match the hash, false otherwise.
     */
    public static boolean matches(String password, String hash) {
        return that.passwordEncoder.matches(password, hash);
    }

}