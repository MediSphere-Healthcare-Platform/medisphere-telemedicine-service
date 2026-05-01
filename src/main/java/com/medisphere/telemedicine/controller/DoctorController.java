package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.client.MedisphereDoctorClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final MedisphereDoctorClient doctorServiceClient;

    public DoctorController(MedisphereDoctorClient doctorServiceClient) {
        this.doctorServiceClient = doctorServiceClient;
    }

    // GET /api/doctors
    @GetMapping
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorServiceClient.getAllDoctors());
    }

    // GET /api/doctors/{doctorId}
    @GetMapping("/{doctorId}")
    public ResponseEntity<?> getDoctorById(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorServiceClient.getDoctorById(doctorId));
    }
}
