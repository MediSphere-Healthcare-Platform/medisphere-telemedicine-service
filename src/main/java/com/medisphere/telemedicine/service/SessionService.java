package com.medisphere.telemedicine.service;

import com.medisphere.telemedicine.domain.SessionStatus;
import com.medisphere.telemedicine.dto.EndSessionRequest;
import com.medisphere.telemedicine.dto.SessionCreateRequest;
import com.medisphere.telemedicine.dto.SessionResponse;
import com.medisphere.telemedicine.entity.Session;
import com.medisphere.telemedicine.exception.SessionNotFoundException;
import com.medisphere.telemedicine.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final NotificationClient notificationClient;

    @Value("${jitsi.base-url}")
    private String jitsiBaseUrl;

    @Value("${jitsi.room-prefix}")
    private String jitsiRoomPrefix;

    public SessionService(SessionRepository sessionRepository,
                          NotificationClient notificationClient) {
        this.sessionRepository = sessionRepository;
        this.notificationClient = notificationClient;
    }

    // Called by Appointment Service when appointment is confirmed
    public SessionResponse createSession(SessionCreateRequest request) {

        // Generate a unique, URL-safe Jitsi room name
        String uniquePart = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String roomName   = jitsiRoomPrefix + uniquePart;
        String roomUrl    = jitsiBaseUrl + "/" + roomName;

        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setAppointmentId(request.getAppointmentId());
        session.setPatientId(request.getPatientId());
        session.setDoctorId(request.getDoctorId());
        session.setRoomName(roomName);
        session.setRoomUrl(roomUrl);
        session.setScheduledAt(request.getScheduledAt());
        session.setStatus(SessionStatus.SCHEDULED);

        Session saved = sessionRepository.save(session);
        return mapToResponse(saved);
    }

    public SessionResponse getSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));
        return mapToResponse(session);
    }

    public SessionResponse getSessionByAppointmentId(Integer appointmentId) {
        Session session = sessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "No session found for appointment: " + appointmentId));
        return mapToResponse(session);
    }

    public List<SessionResponse> getPatientSessions(Integer patientId) {
        return sessionRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<SessionResponse> getDoctorSessions(Integer doctorId) {
        return sessionRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Mark session as ACTIVE when either party joins the room
    public SessionResponse startSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot start a cancelled session");
        }
        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Session is already completed");
        }

        session.setStatus(SessionStatus.ACTIVE);
        session.setStartedAt(LocalDateTime.now());

        return mapToResponse(sessionRepository.save(session));
    }

    // Doctor ends session, calculates duration, fires notification
    public SessionResponse endSession(String sessionId, EndSessionRequest request) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Cannot end a cancelled session");
        }

        session.setStatus(SessionStatus.COMPLETED);
        session.setEndedAt(LocalDateTime.now());

        if (session.getStartedAt() != null) {
            long minutes = ChronoUnit.MINUTES.between(
                    session.getStartedAt(), session.getEndedAt());
            session.setDurationMinutes((int) minutes);
        }

        if (request != null && request.getNotes() != null) {
            session.setNotes(request.getNotes());
        }

        Session saved = sessionRepository.save(session);

        // Fire notification (non-blocking — failure won't break response)
        notificationClient.notifySessionCompleted(saved);

        return mapToResponse(saved);
    }

    public SessionResponse cancelSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed session");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return mapToResponse(sessionRepository.save(session));
    }

    // --- Entity → DTO mapper ---
    private SessionResponse mapToResponse(Session session) {
        SessionResponse res = new SessionResponse();
        res.setSessionId(session.getSessionId());
        res.setAppointmentId(session.getAppointmentId());
        res.setPatientId(session.getPatientId());
        res.setDoctorId(session.getDoctorId());
        res.setRoomName(session.getRoomName());
        res.setRoomUrl(session.getRoomUrl());
        res.setStatus(session.getStatus());
        res.setScheduledAt(session.getScheduledAt());
        res.setStartedAt(session.getStartedAt());
        res.setEndedAt(session.getEndedAt());
        res.setDurationMinutes(session.getDurationMinutes());
        res.setNotes(session.getNotes());
        res.setCreateDate(session.getCreateDate());
        res.setModifiedDate(session.getModifiedDate());
        return res;
    }
}