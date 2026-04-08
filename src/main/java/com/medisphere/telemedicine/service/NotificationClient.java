package com.medisphere.telemedicine.service;

import com.medisphere.telemedicine.entity.Session;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationClient {

    private final RestTemplate restTemplate;

    @Value("${services.notification-url}")
    private String notificationServiceUrl;

    public NotificationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void notifySessionCompleted(Session session) {
        String url = notificationServiceUrl + "/api/notifications/session-completed";

        Map<String, Object> payload = new HashMap<>();
        payload.put("sessionId",        session.getSessionId());
        payload.put("patientId",        session.getPatientId());
        payload.put("doctorId",         session.getDoctorId());
        payload.put("durationMinutes",  session.getDurationMinutes());
        payload.put("endedAt",          session.getEndedAt().toString());

        try {
            restTemplate.postForObject(url, payload, Void.class);
        } catch (Exception e) {
            // Log but don't crash — notification is non-critical
            System.err.println("[NotificationClient] Failed to notify: " + e.getMessage());
        }
    }
}