package com.medisphere.telemedicine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionCreateRequest {

    @NotNull(message = "Patient ID is required")
    private String patientId;

    @NotNull(message = "Doctor ID is required")
    private String doctorId;

    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;

    private String patientName;
    private String doctorName;
}