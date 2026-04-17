package com.medisphere.telemedicine.service;

import com.medisphere.telemedicine.entity.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationClient {

    public void notifySessionCompleted(Session session) {
        log.info("Notification: Session {} completed for patient {}", 
                session.getSessionId(), session.getPatientId());
    }
}
