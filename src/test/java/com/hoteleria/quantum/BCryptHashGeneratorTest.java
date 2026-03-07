package com.hoteleria.quantum;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility test to generate BCrypt hashes for seed data.
 * Run with: ./gradlew test --tests "com.hoteleria.quantum.BCryptHashGeneratorTest"
 */
class BCryptHashGeneratorTest {

    @Test
    void generateAdminHash() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(10);
        String rawPassword = "Admin2026!";
        String hash = encoder.encode(rawPassword);

        System.out.println("==============================================");
        System.out.println("BCrypt Hash Generator");
        System.out.println("==============================================");
        System.out.println("Password: " + rawPassword);
        System.out.println("Hash:     " + hash);
        System.out.println("Verify:   " + encoder.matches(rawPassword, hash));
        System.out.println("==============================================");
    }
}
