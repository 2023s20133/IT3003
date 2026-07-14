package org.hospital.management.exception;

/**
 * Thrown when scheduling rules (e.g., past dates, invalid hours) are violated.
 */
public class InvalidAppointmentException extends RuntimeException {
    public InvalidAppointmentException(String message) {
        super(message);
    }
}
