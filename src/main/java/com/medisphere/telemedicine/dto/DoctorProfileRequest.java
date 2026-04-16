package com.medisphere.telemedicine.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DoctorProfileRequest {

    @NotBlank(message = "Name is required")
    private String name;

    private String specialty;

    private String bio;
}
