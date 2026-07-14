package org.hospital.management.service;

import org.hospital.management.exception.AppointmentConflictException;
import org.hospital.management.model.Appointment;
import org.hospital.management.model.Doctor;

public class ReassignmentService {
    private final AppointmentScheduler scheduler;

    public ReassignmentService(AppointmentScheduler scheduler) {
        this.scheduler = scheduler;
    }

    public synchronized void handleEmergencySwap(String appointmentId, String substituteDoctorId) {
        Appointment appointment = scheduler.getAppointment(appointmentId);
        Doctor substituteDoctor = scheduler.getDoctor(substituteDoctorId);

        // Take time slot from substitute doctor
        substituteDoctor.bookSlot(appointment.getDateTime());

        // Return time slot to original doctor
        appointment.getDoctor().releaseSlot(appointment.getDateTime());

        // Swap the doctor
        appointment.setDoctor(substituteDoctor);
        
        // Notice: The billing amount is generally kept the same as initially quoted 
        // to not surprise the patient with a different bill during an emergency swap.
    }
}
