package com.medisphere.telemedicine.client;

import com.medisphere.telemedicine.client.response.PatientByIdClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medisphere-patient-service", dismiss404 = true)
public interface MedispherePatientClient {

    @GetMapping("/patient/api/v1/getPatientById/{id}")
    ResponseEntity<PatientByIdClientResponse> getPatientById(@PathVariable("id") String id);
}
