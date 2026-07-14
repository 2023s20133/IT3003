package org.hospital.management.exception;

/**
 * Thrown when an insurance policy is expired or invalid.
 */
public class PolicyValidationException extends RuntimeException {
    public PolicyValidationException(String message) {
        super(message);
    }
}
