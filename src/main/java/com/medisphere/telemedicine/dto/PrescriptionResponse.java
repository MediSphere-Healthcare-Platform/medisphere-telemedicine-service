package com.medisphere.telemedicine.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PrescriptionResponse {

    private String prescriptionId;
    private String sessionId;
    private String patientId;
    private String doctorId;
    private String diagnosis;
    private List<MedicationItem> medications;
    private String instructions;
    private LocalDateTime issuedAt;
    private LocalDateTime createDate;
    private LocalDateTime modifiedDate;
}
