package com.medisphere.telemedicine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class AppointmentClient {

    private static final Logger log =
            LoggerFactory.getLogger(AppointmentClient.class);

    private final RestTemplate restTemplate;

    @Value("${services.appointment-url}")
    private String appointmentServiceUrl;

    public AppointmentClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public boolean appointmentExists(Integer appointmentId) {
        try {
            String url = appointmentServiceUrl +
                    "/api/appointments/" + appointmentId;
            restTemplate.getForObject(url, Object.class);
            return true;
        } catch (Exception e) {
            log.warn("Appointment {} not found or service unavailable: {}",
                    appointmentId, e.getMessage());
            return false;
        }
    }
}