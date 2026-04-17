package com.medisphere.telemedicine.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorServiceClient {

    @Value("${services.doctor-url}")
    private String doctorUrl;

    private final RestTemplate restTemplate;

    public Object getAllDoctors() {
        try {
            return restTemplate.getForObject(doctorUrl + "/doctor/api/v1/getAllDoctors", Object.class);
        } catch (Exception e) {
            log.warn("Could not reach doctor service (getAllDoctors): {}", e.getMessage());
            return null;
        }
    }

    public Object getDoctorById(String doctorId) {
        try {
            return restTemplate.getForObject(
                    doctorUrl + "/doctor/api/v1/getDoctorById/" + doctorId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Doctor {} not found in doctor service", doctorId);
            return null;
        } catch (Exception e) {
            log.warn("Could not reach doctor service (getDoctorById {}): {}", doctorId, e.getMessage());
            return null;
        }
    }

    public boolean doctorExists(String doctorId) {
        return getDoctorById(doctorId) != null;
    }
}
