package com.medisphere.telemedicine.repository;

import com.medisphere.telemedicine.domain.SessionStatus;
import com.medisphere.telemedicine.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    Optional<Session> findBySessionId(String sessionId);

    List<Session> findByPatientId(String patientId);

    List<Session> findByDoctorId(String doctorId);

    List<Session> findByPatientIdAndStatus(String patientId, SessionStatus status);

    List<Session> findByDoctorIdAndStatus(String doctorId, SessionStatus status);
}