package com.medisphere.telemedicine.controller;

import com.medisphere.telemedicine.dto.DoctorProfileRequest;
import com.medisphere.telemedicine.dto.DoctorProfileResponse;
import com.medisphere.telemedicine.service.DoctorProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    private final DoctorProfileService doctorProfileService;

    public DoctorController(DoctorProfileService doctorProfileService) {
        this.doctorProfileService = doctorProfileService;
    }

    // GET /api/doctors
    // Any authenticated user can browse doctors (patient needs this for the dropdown)
    @GetMapping
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<DoctorProfileResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorProfileService.getAllDoctors());
    }

    // PUT /api/doctors/profile
    // Doctor registers or updates their own profile (upsert)
    @PutMapping("/profile")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<DoctorProfileResponse> upsertProfile(
            @Valid @RequestBody DoctorProfileRequest request) {

        String callerUserId = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal().toString();

        return ResponseEntity.ok(
                doctorProfileService.upsertProfile(request, callerUserId));
    }
}
