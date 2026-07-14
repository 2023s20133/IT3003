package org.hospital.management.exception;

/**
 * Thrown when a doctor is already booked during a requested time slot.
 */
public class DoctorUnavailableException extends RuntimeException {
    public DoctorUnavailableException(String message) {
        super(message);
    }
}
