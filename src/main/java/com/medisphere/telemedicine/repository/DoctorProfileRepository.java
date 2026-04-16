package com.medisphere.telemedicine.repository;

import com.medisphere.telemedicine.entity.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface DoctorProfileRepository extends JpaRepository<DoctorProfile, Integer> {

    Optional<DoctorProfile> findByDoctorId(Integer doctorId);

    boolean existsByDoctorId(Integer doctorId);
}
