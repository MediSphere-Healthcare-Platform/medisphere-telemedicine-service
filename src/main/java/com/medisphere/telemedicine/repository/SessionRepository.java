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

    Optional<Session> findByAppointmentId(Integer appointmentId);

    List<Session> findByPatientId(Integer patientId);

    List<Session> findByDoctorId(Integer doctorId);

    List<Session> findByPatientIdAndStatus(Integer patientId, SessionStatus status);

    List<Session> findByDoctorIdAndStatus(Integer doctorId, SessionStatus status);
}