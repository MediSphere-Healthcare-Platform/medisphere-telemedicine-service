package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.EndSessionRequest;
import com.medisphere.telemedicine.dto.SessionCreateRequest;
import com.medisphere.telemedicine.dto.SessionRequestRequest;
import com.medisphere.telemedicine.dto.SessionResponse;
import com.medisphere.telemedicine.service.SessionService;
import com.medisphere.telemedicine.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService sessionService;
    private final JwtUtil jwtUtil;

    public SessionController(SessionService sessionService, JwtUtil jwtUtil) {
        this.sessionService = sessionService;
        this.jwtUtil = jwtUtil;
    }

    // POST /api/sessions
    // Doctor creates a session for a patient — immediately SCHEDULED
    @PostMapping
    public ResponseEntity<SessionResponse> createSession(
            @Valid @RequestBody SessionCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.createSessionByDoctor(request));
    }

    // GET /api/sessions/{sessionId}
    @GetMapping("/{sessionId}")
    public ResponseEntity<SessionResponse> getSession(
            @PathVariable String sessionId,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String role,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (userId == null) {
            userId = jwtUtil.extractUserId(authHeader);
        }
        if (role == null) {
            role = jwtUtil.extractRole(authHeader);
        }

        return ResponseEntity.ok(
                sessionService.getSession(sessionId, userId, role));
    }

    // GET /api/sessions/patient/{patientId}
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<SessionResponse>> getPatientSessions(
            @PathVariable String patientId) {
        return ResponseEntity.ok(sessionService.getPatientSessions(patientId));
    }

    // GET /api/sessions/doctor/{doctorId}
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<SessionResponse>> getDoctorSessions(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(sessionService.getDoctorSessions(doctorId));
    }

    // PUT /api/sessions/{sessionId}/start
    // Called when patient or doctor joins the Jitsi room
    @PutMapping("/{sessionId}/start")
    public ResponseEntity<SessionResponse> startSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.startSession(sessionId));
    }

    // PUT /api/sessions/{sessionId}/end
    // Called by doctor when consultation is finished
    @PutMapping("/{sessionId}/end")
    public ResponseEntity<SessionResponse> endSession(
            @PathVariable String sessionId,
            @RequestBody(required = false) EndSessionRequest request) {
        return ResponseEntity.ok(sessionService.endSession(sessionId, request));
    }

    // PUT /api/sessions/{sessionId}/cancel
    // Patients can cancel their own PENDING_APPROVAL requests; doctors/admins can cancel any
    @PutMapping("/{sessionId}/cancel")
    public ResponseEntity<SessionResponse> cancelSession(
            @PathVariable String sessionId) {
        return ResponseEntity.ok(sessionService.cancelSession(sessionId));
    }

    // POST /api/sessions/request
    // Patient requests a session with a specific doctor
    @PostMapping("/request")
    public ResponseEntity<SessionResponse> requestSession(
            @Valid @RequestBody SessionRequestRequest request,
            @RequestParam(required = false) String patientUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (patientUserId == null) {
            patientUserId = jwtUtil.extractUserId(authHeader);
        }

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.requestSession(request, patientUserId));
    }

    // PUT /api/sessions/{sessionId}/accept
    // Doctor accepts a PENDING_APPROVAL session → SCHEDULED
    @PutMapping("/{sessionId}/accept")
    public ResponseEntity<SessionResponse> acceptSession(
            @PathVariable String sessionId,
            @RequestParam(required = false) String doctorUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (doctorUserId == null) {
            doctorUserId = jwtUtil.extractUserId(authHeader);
        }

        return ResponseEntity.ok(sessionService.acceptSession(sessionId, doctorUserId));
    }

    // PUT /api/sessions/{sessionId}/reject
    // Doctor rejects a PENDING_APPROVAL session → CANCELLED
    @PutMapping("/{sessionId}/reject")
    public ResponseEntity<SessionResponse> rejectSession(
            @PathVariable String sessionId,
            @RequestParam(required = false) String doctorUserId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {

        if (doctorUserId == null) {
            doctorUserId = jwtUtil.extractUserId(authHeader);
        }

        return ResponseEntity.ok(sessionService.rejectSession(sessionId, doctorUserId));
    }

    // GET /api/sessions/doctor/{doctorId}/pending
    // Doctor sees all sessions awaiting their approval
    @GetMapping("/doctor/{doctorId}/pending")
    public ResponseEntity<List<SessionResponse>> getPendingForDoctor(
            @PathVariable String doctorId) {
        return ResponseEntity.ok(sessionService.getPendingForDoctor(doctorId));
    }
}