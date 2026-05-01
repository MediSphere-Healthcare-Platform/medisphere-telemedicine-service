package com.medisphere.telemedicine.client;

import com.medisphere.telemedicine.client.response.DoctorByIdClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "medisphere-doctor-service", dismiss404 = true)
public interface MedisphereDoctorClient {

    @GetMapping("/doctor/api/v1/getDoctorById/{id}")
    ResponseEntity<DoctorByIdClientResponse> getDoctorById(@PathVariable("id") String id);
}
