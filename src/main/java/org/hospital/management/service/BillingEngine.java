package org.hospital.management.service;

import org.hospital.management.model.Appointment;

public class BillingEngine {

    public double calculatePatientBill(Appointment appointment) {
        double fee = appointment.getDoctor().getBaseConsultationFee();
        double finalBill;

        if (appointment.getPatient().hasInsurance()) {
            double coverage = appointment.getPatient().getInsurancePolicy().getCoveragePercentage();
            finalBill = fee * (1.0 - coverage);
        } else {
            finalBill = fee;
        }

        // Update the bill amount on the appointment
        appointment.setBillingAmount(finalBill);
        
        return finalBill;
    }
}
