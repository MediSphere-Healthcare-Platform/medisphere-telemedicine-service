package com.medisphere.telemedicine.client.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PatientClientResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String patientId;
    private String msUserId;
    private String contactNo;
    private String status;
}
