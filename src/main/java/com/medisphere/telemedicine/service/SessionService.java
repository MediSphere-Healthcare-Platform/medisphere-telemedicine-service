package com.medisphere.telemedicine.service;

import com.medisphere.telemedicine.domain.SessionStatus;
import com.medisphere.telemedicine.dto.EndSessionRequest;
import com.medisphere.telemedicine.dto.SessionCreateRequest;
import com.medisphere.telemedicine.dto.SessionRequestRequest;
import com.medisphere.telemedicine.dto.SessionResponse;
import com.medisphere.telemedicine.entity.Session;
import com.medisphere.telemedicine.exception.SessionNotFoundException;
import com.medisphere.telemedicine.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SessionService {

    private static final Logger log =
            LoggerFactory.getLogger(SessionService.class);

    private final SessionRepository sessionRepository;
    private final NotificationClient notificationClient;
    private final AppointmentClient appointmentClient;
    private final JitsiTokenService jitsiTokenService;

    @Value("${jitsi.base-url}")
    private String jitsiBaseUrl;

    @Value("${jitsi.room-prefix}")
    private String jitsiRoomPrefix;

    public SessionService(SessionRepository sessionRepository,
                          NotificationClient notificationClient,
                          AppointmentClient appointmentClient,
                          JitsiTokenService jitsiTokenService) {
        this.sessionRepository  = sessionRepository;
        this.notificationClient = notificationClient;
        this.appointmentClient  = appointmentClient;
        this.jitsiTokenService  = jitsiTokenService;
    }

    // Called by Appointment Service when appointment is confirmed
    @Transactional
    public SessionResponse createSession(SessionCreateRequest request) {

        // Graceful degradation — warn if Appointment Service is unreachable
        if (!appointmentClient.appointmentExists(request.getAppointmentId())) {
            log.warn("Could not verify appointment {} — " +
                            "Appointment Service may be unavailable",
                    request.getAppointmentId());
        }

        String uniquePart = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);
        String roomName = jitsiRoomPrefix + uniquePart;
        String roomUrl  = jitsiBaseUrl.stripTrailing().replaceAll("/+$", "") + "/" + roomName;

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

        // No role context at creation time — return without jitsiToken
        return mapToResponse(saved);
    }

    // Role-aware — generates correct jitsiToken for caller
    @Transactional(readOnly = true)
    public SessionResponse getSession(String sessionId,
                                      String userId, String role) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));
        return mapToResponse(session, userId, role);
    }

    // Role-aware — generates correct jitsiToken for caller
    @Transactional(readOnly = true)
    public SessionResponse getSessionByAppointmentId(Integer appointmentId,
                                                     String userId,
                                                     String role) {
        Session session = sessionRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "No session found for appointment: " + appointmentId));
        return mapToResponse(session, userId, role);
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getPatientSessions(Integer patientId) {
        return sessionRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> getDoctorSessions(Integer doctorId) {
        return sessionRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Patient requests a session with a specific doctor
    @Transactional
    public SessionResponse requestSession(SessionRequestRequest request,
                                          String patientUserId) {
        String uniquePart = UUID.randomUUID()
                .toString().replace("-", "").substring(0, 12);
        String roomName = jitsiRoomPrefix + uniquePart;
        String roomUrl  = jitsiBaseUrl.stripTrailing().replaceAll("/+$", "") + "/" + roomName;

        Session session = new Session();
        session.setSessionId(UUID.randomUUID().toString());
        session.setPatientId(Integer.valueOf(patientUserId));
        session.setDoctorId(request.getDoctorId());
        session.setRoomName(roomName);
        session.setRoomUrl(roomUrl);
        session.setScheduledAt(request.getPreferredAt());
        session.setRequestReason(request.getReason());
        session.setStatus(SessionStatus.PENDING_APPROVAL);

        Session saved = sessionRepository.save(session);
        log.info("Patient {} requested session {} with doctor {}",
                patientUserId, saved.getSessionId(), request.getDoctorId());
        return mapToResponse(saved);
    }

    // Doctor accepts a pending session request → moves to SCHEDULED
    @Transactional
    public SessionResponse acceptSession(String sessionId, String doctorUserId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Only pending sessions can be accepted");
        }

        if (!session.getDoctorId().toString().equals(doctorUserId)) {
            throw new IllegalStateException(
                    "Only the assigned doctor can accept this session");
        }

        session.setStatus(SessionStatus.SCHEDULED);
        Session saved = sessionRepository.save(session);
        log.info("Doctor {} accepted session {}", doctorUserId, sessionId);
        return mapToResponse(saved);
    }

    // Doctor rejects a pending session request → moves to CANCELLED
    @Transactional
    public SessionResponse rejectSession(String sessionId, String doctorUserId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() != SessionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "Only pending sessions can be rejected");
        }

        if (!session.getDoctorId().toString().equals(doctorUserId)) {
            throw new IllegalStateException(
                    "Only the assigned doctor can reject this session");
        }

        session.setStatus(SessionStatus.CANCELLED);
        Session saved = sessionRepository.save(session);
        log.info("Doctor {} rejected session {}", doctorUserId, sessionId);
        return mapToResponse(saved);
    }

    // All pending-approval sessions for a specific doctor
    @Transactional(readOnly = true)
    public List<SessionResponse> getPendingForDoctor(Integer doctorId) {
        return sessionRepository.findByDoctorIdAndStatus(doctorId, SessionStatus.PENDING_APPROVAL)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Mark session as ACTIVE when either party joins the room
    @Transactional
    public SessionResponse startSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.PENDING_APPROVAL) {
            throw new IllegalStateException("Session has not been accepted by the doctor yet");
        }
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
    @Transactional
    public SessionResponse endSession(String sessionId,
                                      EndSessionRequest request) {
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

        // Non-blocking — failure won't break the response
        notificationClient.notifySessionCompleted(saved);

        return mapToResponse(saved);
    }

    @Transactional
    public SessionResponse cancelSession(String sessionId) {
        Session session = sessionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + sessionId));

        if (session.getStatus() == SessionStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel a completed session");
        }
        if (session.getStatus() == SessionStatus.CANCELLED) {
            throw new IllegalStateException("Session is already cancelled");
        }

        session.setStatus(SessionStatus.CANCELLED);
        return mapToResponse(sessionRepository.save(session));
    }

    // ---------------------------------------------------------------
    // Mappers
    // ---------------------------------------------------------------

    // Role-aware mapper — generates jitsiToken specific to caller
    private SessionResponse mapToResponse(Session session,
                                          String userId, String role) {
        SessionResponse res = buildBaseResponse(session);

        boolean isModerator = "DOCTOR".equals(role);
        String userName = isModerator
                ? "Dr. " + userId
                : "Patient " + userId;

        res.setJitsiToken(jitsiTokenService.generateToken(
                session.getRoomName(),
                userId,
                userName,
                isModerator
        ));

        return res;
    }

    // Plain mapper — no jitsiToken (used for list endpoints,
    // create, start, end, cancel where token isn't needed)
    private SessionResponse mapToResponse(Session session) {
        return buildBaseResponse(session);
    }

    // Shared base — builds everything except jitsiToken
    private SessionResponse buildBaseResponse(Session session) {
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
        res.setRequestReason(session.getRequestReason());
        res.setCreateDate(session.getCreateDate());
        res.setModifiedDate(session.getModifiedDate());
        return res;
    }
}