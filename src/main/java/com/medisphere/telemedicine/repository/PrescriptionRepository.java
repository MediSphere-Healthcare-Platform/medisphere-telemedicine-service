package com.medisphere.telemedicine.repository;

import com.medisphere.telemedicine.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PrescriptionRepository extends JpaRepository<Prescription, Integer> {

    Optional<Prescription> findByPrescriptionId(String prescriptionId);

    Optional<Prescription> findBySessionId(String sessionId);

    List<Prescription> findByPatientId(Integer patientId);

    List<Prescription> findByDoctorId(Integer doctorId);

    boolean existsBySessionId(String sessionId);
}
