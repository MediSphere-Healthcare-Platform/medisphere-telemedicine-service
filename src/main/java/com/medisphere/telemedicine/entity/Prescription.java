package com.medisphere.telemedicine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

// Using @Getter/@Setter instead of @Data — avoids equals/hashCode issues
// with Hibernate lazy-loaded proxies on JPA entities
@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "prescription")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "prescription_id", unique = true, nullable = false, length = 50)
    private String prescriptionId;  // UUID

    // One prescription per session
    @Column(name = "session_id", unique = true, nullable = false, length = 50)
    private String sessionId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "diagnosis", columnDefinition = "text")
    private String diagnosis;

    // JSON array of MedicationItem objects, serialised by PrescriptionService
    @Column(name = "medications", columnDefinition = "text")
    private String medications;

    @Column(name = "instructions", columnDefinition = "text")
    private String instructions;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createDate   = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
        issuedAt     = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
