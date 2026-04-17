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
public class PatientClient {

    @Value("${services.patient-url}")
    private String patientUrl;

    private final RestTemplate restTemplate;

    public Object getPatientById(String patientId) {
        try {
            return restTemplate.getForObject(
                    patientUrl + "/patient/api/v1/getPatientById/" + patientId, Object.class);
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Patient {} not found in patient service", patientId);
            return null;
        } catch (Exception e) {
            log.warn("Could not reach patient service (getPatientById {}): {}", patientId, e.getMessage());
            return null;
        }
    }

    public boolean patientExists(String patientId) {
        return getPatientById(patientId) != null;
    }

    public Object getAllPatients() {
        try {
            return restTemplate.getForObject(
                    patientUrl + "/patient/api/v1/getAllPatientForAdmin", Object.class);
        } catch (Exception e) {
            log.warn("Could not reach patient service (getAllPatients): {}", e.getMessage());
            return null;
        }
    }
}
