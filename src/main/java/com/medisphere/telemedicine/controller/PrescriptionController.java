package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.PrescriptionRequest;
import com.medisphere.telemedicine.dto.PrescriptionResponse;
import com.medisphere.telemedicine.dto.PrescriptionUpdateRequest;
import com.medisphere.telemedicine.service.PrescriptionService;
import com.medisphere.telemedicine.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final JwtUtil jwtUtil;

    public PrescriptionController(PrescriptionService prescriptionService, JwtUtil jwtUtil) {
        this.prescriptionService = prescriptionService;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/prescriptions
    // Doctor issues a prescription after a completed session
    @PostMapping
    public ResponseEntity<PrescriptionResponse> createPrescription(
            @Valid @RequestBody PrescriptionRequest request,
            @RequestParam(required = false) String doctorUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (doctorUserId == null) {
            doctorUserId = jwtUtil.extractUserId(authHeader);
        }

        PrescriptionResponse response =
                prescriptionService.createPrescription(request, doctorUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/prescriptions/{prescriptionId}
    @GetMapping("/{prescriptionId}")
    public ResponseEntity<PrescriptionResponse> getById(
            @PathVariable String prescriptionId) {
        return ResponseEntity.ok(prescriptionService.getById(prescriptionId));
    }

    // GET /api/prescriptions/session/{sessionId}
    // Fetches the prescription for a given session (if issued)
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<PrescriptionResponse> getBySession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(prescriptionService.getBySession(sessionId));
    }

    // GET /api/prescriptions/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PrescriptionResponse>> getByPatient(
            @PathVariable String patientId) {
        return ResponseEntity.ok(prescriptionService.getByPatient(patientId));
    }

    // GET /api/prescriptions/doctor/{doctorId}
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<PrescriptionResponse>> getByDoctor(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(prescriptionService.getByDoctor(doctorId));
    }

    // PUT /api/prescriptions/{prescriptionId}
    // Doctor updates diagnosis, medications, or instructions
    @PutMapping("/{prescriptionId}")
    public ResponseEntity<PrescriptionResponse> updatePrescription(
            @PathVariable String prescriptionId,
            @Valid @RequestBody PrescriptionUpdateRequest request,
            @RequestParam(required = false) String doctorUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (doctorUserId == null) {
            doctorUserId = jwtUtil.extractUserId(authHeader);
        }

        return ResponseEntity.ok(
                prescriptionService.updatePrescription(prescriptionId, request, doctorUserId));
    }

    // DELETE /api/prescriptions/{prescriptionId}
    // Doctor or admin removes a prescription
    @DeleteMapping("/{prescriptionId}")
    public ResponseEntity<Void> deletePrescription(
            @PathVariable String prescriptionId,
            @RequestParam(required = false) String doctorUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (doctorUserId == null) {
            doctorUserId = jwtUtil.extractUserId(authHeader);
        }

        prescriptionService.deletePrescription(prescriptionId, doctorUserId);
        return ResponseEntity.noContent().build();
    }
}
