package com.medisphere.telemedicine.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.Base64;

@Component
public class JwtUtil {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractUserId(String authHeader) {
        return extractClaim(authHeader, "sub");
    }

    public String extractRole(String authHeader) {
        return extractClaim(authHeader, "role");
    }

    private String extractClaim(String authHeader, String claimName) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }

        try {
            String token = authHeader.substring(7);
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
            JsonNode payloadNode = objectMapper.readTree(payloadJson);
            
            if (payloadNode.has(claimName)) {
                return payloadNode.get(claimName).asText();
            }
        } catch (Exception e) {
            // Ignore parsing errors, return null
        }

        return null;
    }
}
