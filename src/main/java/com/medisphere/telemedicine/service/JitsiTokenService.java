package com.medisphere.telemedicine.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JitsiTokenService {

    @Value("${jitsi.app-id}")
    private String appId;

    @Value("${jitsi.app-secret}")
    private String appSecret;

    public String generateToken(String roomName, String userId,
                                String userName, boolean isModerator) {
        Key key = Keys.hmacShaKeyFor(
                appSecret.getBytes(StandardCharsets.UTF_8));

        Map<String, Object> context = new HashMap<>();
        Map<String, String> user = new HashMap<>();
        user.put("id", userId);
        user.put("name", userName);
        context.put("user", user);

        return Jwts.builder()
                .setIssuer(appId)
                .setSubject("*")
                .setAudience("jitsi")
                .claim("room", roomName)
                .claim("context", context)
                .claim("moderator", isModerator)
                .setIssuedAt(new Date())
                // token valid for 2 hours
                .setExpiration(
                        new Date(System.currentTimeMillis() + 7200000))
                .signWith(key)
                .compact();
    }
}