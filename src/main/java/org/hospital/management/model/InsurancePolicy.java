package org.hospital.management.model;

import org.hospital.management.exception.PolicyValidationException;

/**
 * Represents an insurance policy for a patient.
 */
public class InsurancePolicy {
    private final String providerName;
    private final String policyNumber;
    private final double coveragePercentage;

    public InsurancePolicy(String providerName, String policyNumber, double coveragePercentage) {
        if (coveragePercentage < 0.0 || coveragePercentage > 1.0) {
            throw new PolicyValidationException("Coverage percentage must be between 0.0 and 1.0");
        }
        this.providerName = providerName;
        this.policyNumber = policyNumber;
        this.coveragePercentage = coveragePercentage;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getPolicyNumber() {
        return policyNumber;
    }

    public double getCoveragePercentage() {
        return coveragePercentage;
    }

    @Override
    public String toString() {
        return "InsurancePolicy{" +
                "providerName='" + providerName + '\'' +
                ", policyNumber='" + policyNumber + '\'' +
                ", coveragePercentage=" + (coveragePercentage * 100) + "%" +
                '}';
    }
}
