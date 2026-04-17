package com.medisphere.telemedicine.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SessionRequestRequest {

    @NotNull(message = "Patient ID is required")
    private String patientId;

    @NotNull(message = "Doctor ID is required")
    private String doctorId;

    @NotNull(message = "Preferred date & time is required")
    private LocalDateTime preferredAt;

    // Optional — patient's reason for requesting the session
    private String reason;

    private String patientName;
    private String doctorName;
}
