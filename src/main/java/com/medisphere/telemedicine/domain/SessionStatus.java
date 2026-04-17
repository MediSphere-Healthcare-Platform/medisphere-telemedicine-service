package com.medisphere.telemedicine.domain;

public enum SessionStatus {
    PENDING_APPROVAL,  // Patient requested; waiting for doctor to accept
    SCHEDULED,         // Doctor accepted (or created directly); ready to start
    ACTIVE,            // Either party joined the Jitsi room
    COMPLETED,         // Doctor ended the session
    CANCELLED          // Rejected by doctor, or cancelled before completion
}