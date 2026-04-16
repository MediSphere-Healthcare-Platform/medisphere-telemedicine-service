package com.medisphere.telemedicine.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "doctor_profile")
public class DoctorProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Matches Session.doctorId and the doctor's JWT sub (parsed as Integer)
    @Column(name = "doctor_id", unique = true, nullable = false)
    private Integer doctorId;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 100)
    private String specialty;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(name = "create_date")
    private LocalDateTime createDate;

    @Column(name = "modified_date")
    private LocalDateTime modifiedDate;

    @PrePersist
    protected void onCreate() {
        createDate   = LocalDateTime.now();
        modifiedDate = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedDate = LocalDateTime.now();
    }
}
