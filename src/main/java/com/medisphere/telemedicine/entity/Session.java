package com.medisphere.telemedicine.entity;

import com.medisphere.telemedicine.domain.SessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

// Using @Getter/@Setter instead of @Data on JPA entities
// @Data generates equals/hashCode using all fields which causes
// issues with lazy-loaded proxies in Hibernate — avoid it on entities
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "telemedicine_session")
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "session_id", unique = true, nullable = false, length = 50)
    private String sessionId;

    // FK → medisphere_appointment.id (null for patient-requested sessions)
    @Column(name = "appointment_id")
    private Integer appointmentId;

    // FK → medisphere_patient.id
    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    // FK → doctor_table.id
    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "room_name", nullable = false, length = 100)
    private String roomName;

    @Column(name = "room_url", nullable = false, length = 500)
    private String roomUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private SessionStatus status;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "notes", columnDefinition = "text")
    private String notes;

    // Filled by patient when requesting a session
    @Column(name = "request_reason", columnDefinition = "text")
    private String requestReason;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createDate = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        if (status == null) {
            status = SessionStatus.SCHEDULED;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}