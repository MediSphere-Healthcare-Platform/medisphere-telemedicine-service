package com.medisphere.telemedicine.service;

import com.medisphere.telemedicine.dto.DoctorProfileRequest;
import com.medisphere.telemedicine.dto.DoctorProfileResponse;
import com.medisphere.telemedicine.entity.DoctorProfile;
import com.medisphere.telemedicine.repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorProfileService {

    private final DoctorProfileRepository doctorProfileRepository;

    public DoctorProfileService(DoctorProfileRepository doctorProfileRepository) {
        this.doctorProfileRepository = doctorProfileRepository;
    }

    // Returns all registered doctor profiles (used to populate the patient dropdown)
    @Transactional(readOnly = true)
    public List<DoctorProfileResponse> getAllDoctors() {
        return doctorProfileRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Doctor registers or updates their own profile (upsert on doctorId)
    @Transactional
    public DoctorProfileResponse upsertProfile(DoctorProfileRequest request,
                                               String callerUserId) {
        Integer doctorId = Integer.valueOf(callerUserId);

        DoctorProfile profile = doctorProfileRepository
                .findByDoctorId(doctorId)
                .orElseGet(DoctorProfile::new);

        profile.setDoctorId(doctorId);
        profile.setName(request.getName());
        profile.setSpecialty(request.getSpecialty());
        profile.setBio(request.getBio());

        return mapToResponse(doctorProfileRepository.save(profile));
    }

    private DoctorProfileResponse mapToResponse(DoctorProfile p) {
        DoctorProfileResponse res = new DoctorProfileResponse();
        res.setDoctorId(p.getDoctorId());
        res.setName(p.getName());
        res.setSpecialty(p.getSpecialty());
        res.setBio(p.getBio());
        res.setCreateDate(p.getCreateDate());
        return res;
    }
}
