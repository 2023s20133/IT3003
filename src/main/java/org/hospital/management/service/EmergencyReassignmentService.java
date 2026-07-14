package org.hospital.management.service;

import org.hospital.management.model.Appointment;
import org.hospital.management.model.AppointmentStatus;
import org.hospital.management.model.Doctor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class EmergencyReassignmentService {

    private final BillingEngine billingEngine;

    public EmergencyReassignmentService() {
        this.billingEngine = new BillingEngine();
    }

    public void handleEmergencyDoctorAbsence(Doctor absentDoctor, LocalDateTime targetDate, List<Doctor> globalDoctors, List<Appointment> globalAppointments) {
        
        for (Appointment appointment : globalAppointments) {
            // Find all SCHEDULED appointments for the absentDoctor on that specific date
            if (appointment.getStatus() == AppointmentStatus.SCHEDULED &&
                appointment.getDoctor().getDoctorId().equals(absentDoctor.getDoctorId()) &&
                appointment.getDateTime().toLocalDate().equals(targetDate.toLocalDate())) {
                
                boolean reassigned = false;
                LocalDateTime apptTime = appointment.getDateTime();

                for (Doctor replacementDoctor : globalDoctors) {
                    // Check for exact same specialization and ensure it's not the absent doctor
                    if (!replacementDoctor.getDoctorId().equals(absentDoctor.getDoctorId()) &&
                        replacementDoctor.getSpecialization().equals(absentDoctor.getSpecialization())) {
                        
                        // Check for schedule conflicts (30-minute block rule)
                        boolean hasConflict = false;
                        for (LocalDateTime bookedSlot : replacementDoctor.getBookedSlots()) {
                            if (Math.abs(ChronoUnit.MINUTES.between(bookedSlot, apptTime)) < 30) {
                                hasConflict = true;
                                break;
                            }
                        }

                        if (!hasConflict) {
                            // Replacement found: Free up old doctor's slot and book new doctor's slot
                            absentDoctor.releaseSlot(apptTime);
                            replacementDoctor.bookSlot(apptTime);

                            // Update appointment with new doctor
                            appointment.setDoctor(replacementDoctor);
                            
                            // Recalculate the billing dynamically
                            billingEngine.calculatePatientBill(appointment);
                            
                            // Log the successful reassignment
                            System.out.println("SUCCESS: Reassigned Appointment " + appointment.getAppointmentId() + 
                                               " to " + replacementDoctor.getName());
                            reassigned = true;
                            break; // Stop searching once a replacement is found
                        }
                    }
                }

                if (!reassigned) {
                    // No replacement doctor is available
                    appointment.cancelAppointment(); // This automatically frees up the absent doctor's slot
                    
                    // Log a warning
                    System.out.println("WARNING: Cancelled Appointment " + appointment.getAppointmentId() + 
                                       " as no replacement doctor was available.");
                }
            }
        }
    }
}
