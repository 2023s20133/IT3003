package org.hospital.management.exception;

public class AppointmentConflictException extends HospitalException {
    public AppointmentConflictException(String message) {
        super(message);
    }
}
