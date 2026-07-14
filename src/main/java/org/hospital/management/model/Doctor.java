package org.hospital.management.model;

import org.hospital.management.exception.DoctorUnavailableException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a doctor in the hospital.
 */
public class Doctor {
    private String doctorId;
    private String name;
    private String specialization;
    private double baseConsultationFee;
    private final List<LocalDateTime> bookedSlots;

    public Doctor(String doctorId, String name, String specialization, double baseConsultationFee) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.baseConsultationFee = baseConsultationFee;
        this.bookedSlots = new ArrayList<>();
    }

    public String getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(String doctorId) {
        this.doctorId = doctorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public double getBaseConsultationFee() {
        return baseConsultationFee;
    }

    public void setBaseConsultationFee(double baseConsultationFee) {
        this.baseConsultationFee = baseConsultationFee;
    }

    /**
     * Returns an unmodifiable view of the booked slots.
     */
    public List<LocalDateTime> getBookedSlots() {
        synchronized (bookedSlots) {
            return Collections.unmodifiableList(new ArrayList<>(bookedSlots));
        }
    }

    /**
     * Thread-safe method to book a time slot.
     */
    public void bookSlot(LocalDateTime slot) {
        synchronized (bookedSlots) {
            if (bookedSlots.contains(slot)) {
                throw new DoctorUnavailableException("Doctor is already booked at: " + slot);
            }
            bookedSlots.add(slot);
        }
    }

    /**
     * Thread-safe method to release a time slot.
     */
    public void releaseSlot(LocalDateTime slot) {
        synchronized (bookedSlots) {
            bookedSlots.remove(slot);
        }
    }

    @Override
    public String toString() {
        return "Doctor{" +
                "doctorId='" + doctorId + '\'' +
                ", name='" + name + '\'' +
                ", specialization='" + specialization + '\'' +
                ", baseConsultationFee=$" + baseConsultationFee +
                '}';
    }
}
