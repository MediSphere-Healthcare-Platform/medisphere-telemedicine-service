package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.PrescriptionRequest;
import com.medisphere.telemedicine.dto.PrescriptionResponse;
import com.medisphere.telemedicine.dto.PrescriptionUpdateRequest;
import com.medisphere.telemedicine.service.PrescriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    // POST /api/prescriptions
    // Doctor issues a prescription after a completed session
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request) {

        String callerUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        PrescriptionResponse response =
                prescriptionService.createPrescription(request, callerUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/prescriptions/{prescriptionId}
    @GetMapping("/{prescriptionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<PrescriptionResponse> getById(
            @PathVariable String prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getById(prescriptionId));
    }

    // GET /api/prescriptions/session/{sessionId}
    // Fetches the prescription for a given session (if issued)
    @GetMapping("/session/{sessionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'PATIENT', 'ADMIN')")
    public ResponseEntity<PrescriptionResponse> getBySession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(prescriptionService.getBySession(sessionId));
    }

    // GET /api/prescriptions/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<PrescriptionResponse>> getByPatient(
            @PathVariable String patientId) {
        return ResponseEntity.ok(prescriptionService.getByPatient(patientId));
    }

    // GET /api/prescriptions/doctor/{doctorId}
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PrescriptionResponse>> getByDoctor(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(prescriptionService.getByDoctor(doctorId));
    }

    // PUT /api/prescriptions/{prescriptionId}
    // Doctor updates diagnosis, medications, or instructions
    @PutMapping("/{prescriptionId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable String prescriptionId,
            @Valid @RequestBody PrescriptionUpdateRequest request) {

        String callerUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        return ResponseEntity.ok(
                prescriptionService.updatePrescription(prescriptionId, request, callerUserId));
    }

    // DELETE /api/prescriptions/{prescriptionId}
    // Doctor or admin removes a prescription
    @DeleteMapping("/{prescriptionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Void> deletePrescription(
            @PathVariable String prescriptionId) {

        String callerUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        prescriptionService.deletePrescription(prescriptionId, callerUserId);
        return ResponseEntity.noContent().build();
    }
}
