package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.EndSessionRequest;
import com.medisphere.telemedicine.dto.SessionCreateRequest;
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
    // Called by Appointment Service when an appointment is confirmed
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'PATIENT')")
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionCreateRequest request) {
        SessionResponse response = sessionService.createSession(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/sessions/{sessionId}
// Update getSession endpoint
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

    // GET /api/sessions/appointment/{appointmentId}
    // Used by frontend to get the room URL for a given appointment
    // Update getByAppointment endpoint
    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<SessionResponse> getByAppointment(
            @PathVariable Integer appointmentId) {

        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        String role = auth.getAuthorities().iterator().next()
                .getAuthority().replace("ROLE_", "");
        String userId = auth.getPrincipal().toString();

        return ResponseEntity.ok(
                sessionService.getSessionByAppointmentId(
                        appointmentId, userId, role));
    }

    // GET /api/sessions/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<List<SessionResponse>> getPatientSessions(
            @PathVariable Integer patientId) {
        return ResponseEntity.ok(sessionService.getPatientSessions(patientId));
    }

    // GET /api/sessions/doctor/{doctorId}
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<SessionResponse>> getDoctorSessions(
            @PathVariable Integer doctorId) {
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
    @PutMapping("/{sessionId}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.cancelSession(sessionId));
    }
}