package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.EndSessionRequest;
import com.medisphere.telemedicine.dto.SessionCreateRequest;
import com.medisphere.telemedicine.dto.SessionRequestRequest;
import com.medisphere.telemedicine.dto.SessionResponse;
import com.medisphere.telemedicine.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;

    public SessionController(SessionService sessionService) {
        this.sessionService = sessionService;
    }

    // POST /api/sessions
    // Doctor creates a session for a patient — immediately SCHEDULED
    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSessionByDoctor(request));
    }

    // GET /api/sessions/{sessionId}
    @GetMapping("/{sessionId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable String sessionId) {

        // get the role of whoever is calling
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        String userId = auth.getPrincipal().toString();

        return ResponseEntity.ok(
                sessionService.getSession(sessionId, userId, role));
    }

    // GET /api/sessions/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<SessionResponse>> getPatientSessions(
            @PathVariable String patientId) {
        return ResponseEntity.ok(sessionService.getPatientSessions(patientId));
    }

    // GET /api/sessions/doctor/{doctorId}
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SessionResponse>> getDoctorSessions(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(sessionService.getDoctorSessions(doctorId));
    }

    // PUT /api/sessions/{sessionId}/start
    // Called when patient or doctor joins the Jitsi room
    @PutMapping("/{sessionId}/start")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.startSession(sessionId));
    }

    // PUT /api/sessions/{sessionId}/end
    // Called by doctor when consultation is finished
    @PutMapping("/{sessionId}/end")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SessionResponse> endSession(
            @PathVariable String sessionId,
            @RequestBody(required = false) EndSessionRequest request) {
        return ResponseEntity.ok(sessionService.endSession(sessionId, request));
    }

    // PUT /api/sessions/{sessionId}/cancel
    // Patients can cancel their own PENDING_APPROVAL requests; doctors/admins can cancel any
    @PutMapping("/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.cancelSession(sessionId));
    }

    // POST /api/sessions/request
    // Patient requests a session with a specific doctor
    @PostMapping("/request")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<SessionResponse> requestSession(
            @Valid @RequestBody SessionRequestRequest request) {

        String patientUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.requestSession(request, patientUserId));
    }

    // PUT /api/sessions/{sessionId}/accept
    // Doctor accepts a PENDING_APPROVAL session → SCHEDULED
    @PutMapping("/{sessionId}/accept")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SessionResponse> acceptSession(
            @PathVariable String sessionId) {

        String doctorUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        return ResponseEntity.ok(sessionService.acceptSession(sessionId, doctorUserId));
    }

    // PUT /api/sessions/{sessionId}/reject
    // Doctor rejects a PENDING_APPROVAL session → CANCELLED
    @PutMapping("/{sessionId}/reject")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<SessionResponse> rejectSession(
            @PathVariable String sessionId) {

        String doctorUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        return ResponseEntity.ok(sessionService.rejectSession(sessionId, doctorUserId));
    }

    // GET /api/sessions/doctor/{doctorId}/pending
    // Doctor sees all sessions awaiting their approval
    @GetMapping("/doctor/{doctorId}/pending")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SessionResponse>> getPendingForDoctor(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(sessionService.getPendingForDoctor(doctorId));
    }
}