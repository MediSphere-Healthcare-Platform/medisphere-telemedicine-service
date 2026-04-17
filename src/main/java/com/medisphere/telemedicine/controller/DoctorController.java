package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.service.DoctorServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorServiceClient doctorServiceClient;

    public DoctorController(DoctorServiceClient doctorServiceClient) {
        this.doctorServiceClient = doctorServiceClient;
    }

    // GET /api/doctors
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<?> getAllDoctors() {
        return ResponseEntity.ok(doctorServiceClient.getAllDoctors());
    }

    // GET /api/doctors/{doctorId}
    @GetMapping("/{doctorId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<?> getDoctorById(@PathVariable String doctorId) {
        return ResponseEntity.ok(doctorServiceClient.getDoctorById(doctorId));
    }
}
