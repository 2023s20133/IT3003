package org.hospital.management.model;

import org.hospital.management.exception.InvalidAppointmentException;
import java.time.LocalDateTime;

/**
 * Represents an appointment between a patient and a doctor.
 */
public class Appointment {
    private String appointmentId;
    private Patient patient;
    private Doctor doctor;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private double billingAmount;

    public Appointment(String appointmentId, Patient patient, Doctor doctor, LocalDateTime dateTime) {
        if (dateTime != null && dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date and time cannot be in the past.");
        }
        this.appointmentId = appointmentId;
        this.patient = patient;
        this.doctor = doctor;
        this.dateTime = dateTime;
        this.status = AppointmentStatus.SCHEDULED;
        this.billingAmount = 0.0;
    }

    public String getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(String appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        if (dateTime != null && dateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidAppointmentException("Appointment date and time cannot be in the past.");
        }
        this.dateTime = dateTime;
    }

    public AppointmentStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentStatus status) {
        this.status = status;
    }

    public double getBillingAmount() {
        return billingAmount;
    }

    public void setBillingAmount(double billingAmount) {
        this.billingAmount = billingAmount;
    }

    /**
     * Completes the appointment and sets the final bill.
     * @param finalBill the final calculated billing amount
     */
    public void completeAppointment(double finalBill) {
        if (this.status == AppointmentStatus.CANCELLED) {
            throw new InvalidAppointmentException("Cannot complete a cancelled appointment.");
        }
        this.status = AppointmentStatus.COMPLETED;
        this.billingAmount = finalBill;
    }

    /**
     * Cancels the appointment and releases the doctor's slot.
     */
    public void cancelAppointment() {
        if (this.status == AppointmentStatus.COMPLETED) {
            throw new InvalidAppointmentException("Cannot cancel an already completed appointment.");
        }
        this.status = AppointmentStatus.CANCELLED;
        if (this.doctor != null && this.dateTime != null) {
            this.doctor.releaseSlot(this.dateTime);
        }
    }

    @Override
    public String toString() {
        return "Appointment{" +
                "appointmentId='" + appointmentId + '\'' +
                ", patient=" + (patient != null ? patient.getName() : "None") +
                ", doctor=" + (doctor != null ? doctor.getName() : "None") +
                ", dateTime=" + dateTime +
                ", status=" + status +
                ", billingAmount=$" + billingAmount +
                '}';
    }
}
