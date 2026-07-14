package org.hospital.management.service;

import org.hospital.management.exception.DoctorUnavailableException;
import org.hospital.management.exception.InvalidAppointmentException;
import org.hospital.management.model.Appointment;
import org.hospital.management.model.AppointmentStatus;
import org.hospital.management.model.Doctor;
import org.hospital.management.model.Patient;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

public class AppointmentSchedulerService {

    public Appointment scheduleAppointment(Patient patient, Doctor doctor, LocalDateTime dateTime, List<Appointment> globalAppointments) {
        
        // Rule A (Conflict Detection): Check if the doctor already has a scheduled appointment within 30 minutes
        for (LocalDateTime bookedSlot : doctor.getBookedSlots()) {
            long minutesDifference = Math.abs(ChronoUnit.MINUTES.between(bookedSlot, dateTime));
            if (minutesDifference < 30) {
                throw new DoctorUnavailableException("Doctor has a conflicting appointment within 30 minutes of the requested time.");
            }
        }

        // Rule B (Patient Multi-booking Check): Prevent scheduling two appointments at the exact same time slot
        if (globalAppointments != null) {
            for (Appointment existingAppt : globalAppointments) {
                if (existingAppt.getPatient().getPatientId().equals(patient.getPatientId())) {
                    if (existingAppt.getStatus() != AppointmentStatus.CANCELLED) {
                        if (existingAppt.getDateTime().equals(dateTime)) {
                            throw new InvalidAppointmentException("Patient is already scheduled for another appointment at the exact same time.");
                        }
                    }
                }
            }
        }

        // Reserve the time slot for the doctor
        doctor.bookSlot(dateTime);

        // Construct new Appointment in the SCHEDULED state
        String appointmentId = "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Appointment appointment = new Appointment(appointmentId, patient, doctor, dateTime);
        
        // Optionally add to global list if managed externally, we assume caller handles list mutation
        if (globalAppointments != null) {
            globalAppointments.add(appointment);
        }

        return appointment;
    }
}
