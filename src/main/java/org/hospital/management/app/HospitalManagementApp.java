package org.hospital.management.app;

import org.hospital.management.exception.HospitalException;
import org.hospital.management.model.Appointment;
import org.hospital.management.model.Doctor;
import org.hospital.management.model.Patient;
import org.hospital.management.seeder.MockDataSeeder;
import org.hospital.management.service.AppointmentScheduler;
import org.hospital.management.service.BillingEngine;
import org.hospital.management.service.ReassignmentService;

import java.time.LocalDateTime;

public class HospitalManagementApp {

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Outpatient Hospital Management System          ");
        System.out.println("==================================================");

        // 1. Initialize Services
        BillingEngine billingEngine = new BillingEngine();
        AppointmentScheduler scheduler = new AppointmentScheduler(billingEngine);
        ReassignmentService reassignmentService = new ReassignmentService(scheduler);

        // 2. Seed Mock Data
        MockDataSeeder.seed(scheduler);

        System.out.println("\n--- Current Doctors ---");
        for (Doctor doc : scheduler.getAllDoctors()) {
            System.out.println(doc.getName() + " - " + doc.getSpecialization() +
                    " | Booked slots: " + doc.getBookedSlots().size());
        }

        System.out.println("\n--- Scheduling Appointments ---");
        try {
            // Get available slot for D001
            Doctor d1 = scheduler.getDoctor("D001");
            LocalDateTime d1Slot = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);

            // Schedule an insured patient
            System.out.println("Scheduling appointment for P001 (Insured) with D001...");
            Appointment appt1 = scheduler.schedule("P001", "D001", d1Slot);
            System.out.println("Success! " + appt1);

            // Get available slot for D002
            Doctor d2 = scheduler.getDoctor("D002");
            LocalDateTime d2Slot = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);

            // Schedule an uninsured patient
            System.out.println("\nScheduling appointment for P002 (Uninsured) with D002...");
            Appointment appt2 = scheduler.schedule("P002", "D002", d2Slot);
            System.out.println("Success! " + appt2);

            System.out.println("\n--- Simulating Emergency Doctor Swap ---");
            System.out.println(
                    "D002 has an emergency. Attempting to swap appt: " + appt2.getAppointmentId() + " to D003");

            reassignmentService.handleEmergencySwap(appt2.getAppointmentId(), "D003");
            System.out.println(
                    "Swap successful! New appointment details: " + scheduler.getAppointment(appt2.getAppointmentId()));

            System.out.println("\n--- Completing Visits ---");
            scheduler.completeAppointment(appt1.getAppointmentId());
            System.out.println("Completed appointment: " + scheduler.getAppointment(appt1.getAppointmentId()));

        } catch (HospitalException e) {
            System.err.println("Error occurred during operations: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error: " + e.getMessage());
        }

        System.out.println("\n==================================================");
        System.out.println("   System Shutdown                                ");
        System.out.println("==================================================");
    }
}
