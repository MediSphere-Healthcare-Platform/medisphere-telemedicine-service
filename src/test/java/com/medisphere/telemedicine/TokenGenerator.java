package com.medisphere.telemedicine;// Put this anywhere temporarily, e.g. in your test folder
// Run it as a plain Java main — NOT a Spring Boot test
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class TokenGenerator {
    public static void main(String[] args) {
        String secret = "your-shared-secret-key-must-be-at-least-32-chars-long";
        var key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        String doctorToken = Jwts.builder()
                .setSubject("1")           // doctor id
                .claim("role", "DOCTOR")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000)) // 24h
                .signWith(key)
                .compact();

        String patientToken = Jwts.builder()
                .setSubject("2")           // patient id
                .claim("role", "PATIENT")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86400000))
                .signWith(key)
                .compact();

        System.out.println("DOCTOR:  " + doctorToken);
        System.out.println("PATIENT: " + patientToken);
    }
}