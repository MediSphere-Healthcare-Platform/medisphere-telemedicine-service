package com.medisphere.telemedicine.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class DoctorProfileResponse {

    private Integer doctorId;
    private String name;
    private String specialty;
    private String bio;
    private LocalDateTime createDate;
}
