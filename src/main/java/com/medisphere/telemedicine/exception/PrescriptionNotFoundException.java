package com.medisphere.telemedicine.exception;

public class PrescriptionNotFoundException extends RuntimeException {

    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}
