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
public class DoctorClientResponse {
    private Integer id;
    private String firstName;
    private String lastName;
    private String doctorId;
    private String msUserId;
    private String specialty;
    private String drContactNo;
    private String status;
    private String profilePic;
}
