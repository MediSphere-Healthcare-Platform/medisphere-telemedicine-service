package com.medisphere.telemedicine.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicationItem {

    private String name;
    private String dosage;
    private String frequency;
    private String duration;
}
