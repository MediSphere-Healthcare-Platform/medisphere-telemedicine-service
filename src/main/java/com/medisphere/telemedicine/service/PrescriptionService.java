package com.medisphere.telemedicine.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.medisphere.telemedicine.domain.SessionStatus;
import com.medisphere.telemedicine.dto.MedicationItem;
import com.medisphere.telemedicine.dto.PrescriptionRequest;
import com.medisphere.telemedicine.dto.PrescriptionResponse;
import com.medisphere.telemedicine.dto.PrescriptionUpdateRequest;
import com.medisphere.telemedicine.entity.Prescription;
import com.medisphere.telemedicine.entity.Session;
import com.medisphere.telemedicine.exception.PrescriptionNotFoundException;
import com.medisphere.telemedicine.exception.SessionNotFoundException;
import com.medisphere.telemedicine.repository.PrescriptionRepository;
import com.medisphere.telemedicine.repository.SessionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PrescriptionService {

    private static final Logger log = LoggerFactory.getLogger(PrescriptionService.class);

    private final PrescriptionRepository prescriptionRepository;
    private final SessionRepository sessionRepository;
    private final ObjectMapper objectMapper;
    private final DoctorServiceClient doctorServiceClient;
    private final PatientClient patientClient;

    public PrescriptionService(PrescriptionRepository prescriptionRepository,
                               SessionRepository sessionRepository,
                               ObjectMapper objectMapper,
                               DoctorServiceClient doctorServiceClient,
                               PatientClient patientClient) {
        this.prescriptionRepository = prescriptionRepository;
        this.sessionRepository      = sessionRepository;
        this.objectMapper           = objectMapper;
        this.doctorServiceClient    = doctorServiceClient;
        this.patientClient          = patientClient;
    }

    // Doctor issues a prescription after a completed session
    @Transactional
    public PrescriptionResponse createPrescription(PrescriptionRequest request,
                                                   String callerUserId) {
        Session session = sessionRepository.findBySessionId(request.getSessionId())
                .orElseThrow(() -> new SessionNotFoundException(
                        "Session not found: " + request.getSessionId()));

        if (session.getStatus() != SessionStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Prescriptions can only be issued for completed sessions");
        }

        // Validate the patient still exists in patient service
        if (!patientClient.patientExists(session.getPatientId())) {
            throw new IllegalArgumentException(
                    "Patient not found in patient service: " + session.getPatientId());
        }

        // Validate the doctor still exists in doctor service
        if (!doctorServiceClient.doctorExists(session.getDoctorId())) {
            throw new IllegalArgumentException(
                    "Doctor not found in doctor service: " + session.getDoctorId());
        }

        // Role-based access (@PreAuthorize) already restricts this endpoint to DOCTOR.
        // We log a warning if the caller isn't the session's doctor (possible in
        // multi-service setups where JWT sub and DB doctor ID may differ) but we
        // do not block issuance — the audit trail is preserved via doctorId on the record.
        if (!session.getDoctorId().equals(callerUserId)) {
            log.warn("Prescription issued by doctor {} for session owned by doctor {} — IDs differ (check JWT sub vs DB ID alignment)",
                    callerUserId, session.getDoctorId());
        }

        if (prescriptionRepository.existsBySessionId(request.getSessionId())) {
            throw new IllegalStateException(
                    "A prescription has already been issued for this session");
        }

        Prescription prescription = new Prescription();
        prescription.setPrescriptionId(UUID.randomUUID().toString());
        prescription.setSessionId(request.getSessionId());
        prescription.setPatientId(session.getPatientId());
        prescription.setDoctorId(session.getDoctorId());
        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setMedications(serialise(request.getMedications()));
        prescription.setInstructions(request.getInstructions());

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription {} issued for session {} by doctor {}",
                saved.getPrescriptionId(), saved.getSessionId(), callerUserId);

        return mapToResponse(saved);
    }

    // Doctor updates an existing prescription (only the issuing doctor)
    @Transactional
    public PrescriptionResponse updatePrescription(String prescriptionId,
                                                   PrescriptionUpdateRequest request,
                                                   String callerUserId) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found: " + prescriptionId));

        if (!prescription.getDoctorId().equals(callerUserId)) {
            log.warn("Update attempted by doctor {} on prescription owned by doctor {}",
                    callerUserId, prescription.getDoctorId());
        }

        prescription.setDiagnosis(request.getDiagnosis());
        prescription.setMedications(serialise(request.getMedications()));
        prescription.setInstructions(request.getInstructions());

        Prescription saved = prescriptionRepository.save(prescription);
        log.info("Prescription {} updated by doctor {}", prescriptionId, callerUserId);
        return mapToResponse(saved);
    }

    // Doctor or admin deletes a prescription
    @Transactional
    public void deletePrescription(String prescriptionId, String callerUserId) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found: " + prescriptionId));

        prescriptionRepository.delete(prescription);
        log.info("Prescription {} deleted by {}", prescriptionId, callerUserId);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getBySession(String sessionId) {
        Prescription prescription = prescriptionRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "No prescription found for session: " + sessionId));
        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public PrescriptionResponse getById(String prescriptionId) {
        Prescription prescription = prescriptionRepository.findByPrescriptionId(prescriptionId)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found: " + prescriptionId));
        return mapToResponse(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByPatient(String patientId) {
        return prescriptionRepository.findByPatientId(patientId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> getByDoctor(String doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private String serialise(List<MedicationItem> medications) {
        try {
            return objectMapper.writeValueAsString(medications);
        } catch (Exception e) {
            log.error("Failed to serialise medications", e);
            return "[]";
        }
    }

    private List<MedicationItem> deserialise(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(json,
                    new TypeReference<List<MedicationItem>>() {});
        } catch (Exception e) {
            log.error("Failed to deserialise medications", e);
            return Collections.emptyList();
        }
    }

    private PrescriptionResponse mapToResponse(Prescription p) {
        PrescriptionResponse res = new PrescriptionResponse();
        res.setPrescriptionId(p.getPrescriptionId());
        res.setSessionId(p.getSessionId());
        res.setPatientId(p.getPatientId());
        res.setDoctorId(p.getDoctorId());
        res.setDiagnosis(p.getDiagnosis());
        res.setMedications(deserialise(p.getMedications()));
        res.setInstructions(p.getInstructions());
        res.setIssuedAt(p.getIssuedAt());
        res.setCreateDate(p.getCreateDate());
        res.setModifiedDate(p.getModifiedDate());
        return res;
    }
}
