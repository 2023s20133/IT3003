package org.hospital.management.exception;

public class PatientNotFoundException extends HospitalException {
    public PatientNotFoundException(String message) {
        super(message);
    }
}
