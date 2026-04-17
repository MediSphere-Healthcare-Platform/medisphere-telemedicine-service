package com.medisphere.telemedicine.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class PrescriptionUpdateRequest {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    @NotEmpty(message = "At least one medication is required")
    @Valid
    private List<MedicationItem> medications;

    private String instructions;
}
