package com.medisphere.telemedicine.dto;

import com.medisphere.telemedicine.domain.SessionStatus;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionResponse {

    private String sessionId;
    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private String roomName;
    private String roomUrl;
    private SessionStatus status;
    private LocalDateTime scheduledAt;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private Integer durationMinutes;
    private String notes;
    private String requestReason;
    private LocalDateTime createDate;
    private LocalDateTime modifiedDate;
    private String jitsiToken;
}