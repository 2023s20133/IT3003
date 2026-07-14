package org.hospital.management.app;

import org.hospital.management.exception.DoctorUnavailableException;
import org.hospital.management.exception.InvalidAppointmentException;
import org.hospital.management.model.Appointment;
import org.hospital.management.model.Doctor;
import org.hospital.management.model.Patient;
import org.hospital.management.seeder.DataSeeder;
import org.hospital.management.service.AppointmentSchedulerService;
import org.hospital.management.service.BillingEngine;
import org.hospital.management.service.EmergencyReassignmentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        System.out.println("==========================================================");
        System.out.println("     HOSPITAL MANAGEMENT SYSTEM - TEST SUITE EXECUTOR     ");
        System.out.println("==========================================================");

        // -----------------------------------------------------------------
        // SYSTEM INITIALIZATION
        // -----------------------------------------------------------------
        List<Doctor> globalDoctors = DataSeeder.getMockDoctors();
        List<Patient> globalPatients = DataSeeder.getMockPatients();
        // Using CopyOnWriteArrayList for thread-safety in Scenario 10
        List<Appointment> globalAppointments = new CopyOnWriteArrayList<>();
        
        AppointmentSchedulerService scheduler = new AppointmentSchedulerService();
        BillingEngine billingEngine = new BillingEngine();
        EmergencyReassignmentService reassignmentService = new EmergencyReassignmentService();

        // Specific test entities
        Patient uninsuredPatient = globalPatients.stream().filter(p -> !p.hasInsurance()).findFirst().get();
        Patient insuredPatient = globalPatients.stream().filter(Patient::hasInsurance).findFirst().get();
        
        Doctor cardiologist1 = globalDoctors.stream().filter(d -> d.getSpecialization().equals("Cardiology")).findFirst().get();
        Doctor cardiologist2 = globalDoctors.stream().filter(d -> d.getSpecialization().equals("Cardiology") && !d.getDoctorId().equals(cardiologist1.getDoctorId())).findFirst().get();
        
        Doctor rareSpecialist = new Doctor("DOC-999", "Dr. Rare", "Neurosurgeon", 500.0);
        globalDoctors.add(rareSpecialist);

        Doctor generalPhysician = globalDoctors.stream().filter(d -> d.getSpecialization().equals("General Medicine")).findFirst().get();

        LocalDateTime tomorrow10AM = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow11AM = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0).withSecond(0).withNano(0);

        // -----------------------------------------------------------------
        // SCENARIO 1
        // -----------------------------------------------------------------
        printHeader("Scenario 1 (Successful Scheduling)");
        Appointment appt1 = null;
        try {
            appt1 = scheduler.scheduleAppointment(uninsuredPatient, generalPhysician, tomorrow10AM, globalAppointments);
            billingEngine.calculatePatientBill(appt1);
            System.out.println("Success: Scheduled checkup for " + uninsuredPatient.getName());
            System.out.println(appt1);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 2
        // -----------------------------------------------------------------
        printHeader("Scenario 2 (Doctor Double-Booking Conflict)");
        try {
            System.out.println("Attempting to book " + generalPhysician.getName() + " again at " + tomorrow10AM);
            Patient anotherPatient = globalPatients.get(3);
            scheduler.scheduleAppointment(anotherPatient, generalPhysician, tomorrow10AM, globalAppointments);
        } catch (DoctorUnavailableException e) {
            System.out.println("[CAUGHT EXPECTED EXCEPTION] -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 3
        // -----------------------------------------------------------------
        printHeader("Scenario 3 (Polymorphic Billing - Uninsured)");
        if (appt1 != null) {
            double finalBill = appt1.getBillingAmount();
            appt1.completeAppointment(finalBill);
            System.out.println("--- PATIENT INVOICE ---");
            System.out.println("Patient: " + uninsuredPatient.getName() + " [UNINSURED]");
            System.out.println("Doctor Base Fee: $" + generalPhysician.getBaseConsultationFee());
            System.out.println("Total Amount Due: $" + finalBill);
            System.out.println("Status: " + appt1.getStatus());
        }

        // -----------------------------------------------------------------
        // SCENARIO 4
        // -----------------------------------------------------------------
        printHeader("Scenario 4 (Polymorphic Billing - Insured)");
        try {
            Appointment appt2 = scheduler.scheduleAppointment(insuredPatient, cardiologist1, tomorrow11AM, globalAppointments);
            double billed = billingEngine.calculatePatientBill(appt2);
            appt2.completeAppointment(billed);
            System.out.println("--- PATIENT INVOICE ---");
            System.out.println("Patient: " + insuredPatient.getName() + " [INSURED]");
            System.out.println("Provider: " + insuredPatient.getInsurancePolicy().getProviderName() + " (" + (insuredPatient.getInsurancePolicy().getCoveragePercentage()*100) + "% coverage)");
            System.out.println("Doctor Base Fee: $" + cardiologist1.getBaseConsultationFee());
            System.out.println("Total Amount Due: $" + billed);
            System.out.println("Status: " + appt2.getStatus());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 5
        // -----------------------------------------------------------------
        printHeader("Scenario 5 (Patient Multi-Booking Conflict)");
        try {
            LocalDateTime tomorrow1PM = LocalDateTime.now().plusDays(1).withHour(13).withMinute(0).withSecond(0).withNano(0);
            System.out.println("Booking first appointment for " + uninsuredPatient.getName() + " at " + tomorrow1PM);
            scheduler.scheduleAppointment(uninsuredPatient, cardiologist2, tomorrow1PM, globalAppointments);
            
            System.out.println("Attempting second booking for the same patient at the exact same time...");
            scheduler.scheduleAppointment(uninsuredPatient, generalPhysician, tomorrow1PM, globalAppointments);
        } catch (InvalidAppointmentException e) {
            System.out.println("[CAUGHT EXPECTED EXCEPTION] -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 6
        // -----------------------------------------------------------------
        printHeader("Scenario 6 (Emergency Absence Reassignment - Success)");
        try {
            LocalDateTime tomorrow2PM = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
            Appointment appt3 = scheduler.scheduleAppointment(globalPatients.get(4), cardiologist1, tomorrow2PM, globalAppointments);
            System.out.println("Originally Scheduled: " + cardiologist1.getName() + " at " + tomorrow2PM);
            System.out.println("ALERT: " + cardiologist1.getName() + " has an emergency. Triggering reassignment...");
            reassignmentService.handleEmergencyDoctorAbsence(cardiologist1, tomorrow2PM, globalDoctors, globalAppointments);
            System.out.println("Updated Appointment Details:");
            System.out.println(appt3);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 7
        // -----------------------------------------------------------------
        printHeader("Scenario 7 (Emergency Absence Reassignment - Cancellation)");
        try {
            LocalDateTime tomorrow3PM = LocalDateTime.now().plusDays(1).withHour(15).withMinute(0).withSecond(0).withNano(0);
            Appointment appt4 = scheduler.scheduleAppointment(globalPatients.get(5), rareSpecialist, tomorrow3PM, globalAppointments);
            System.out.println("Originally Scheduled: " + rareSpecialist.getName() + " (Neurosurgeon) at " + tomorrow3PM);
            System.out.println("ALERT: " + rareSpecialist.getName() + " has an emergency. Triggering reassignment...");
            reassignmentService.handleEmergencyDoctorAbsence(rareSpecialist, tomorrow3PM, globalDoctors, globalAppointments);
            System.out.println("Final Appointment Status: " + appt4.getStatus());
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 8
        // -----------------------------------------------------------------
        printHeader("Scenario 8 (Dynamic Doctor Availability Search)");
        LocalDateTime searchTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        System.out.println("Querying General Medicine doctors available at " + searchTime + "...");
        List<Doctor> availableGPs = globalDoctors.stream()
            .filter(d -> d.getSpecialization().equals("General Medicine"))
            .filter(d -> {
                for (LocalDateTime slot : d.getBookedSlots()) {
                    if (Math.abs(java.time.temporal.ChronoUnit.MINUTES.between(slot, searchTime)) < 30) {
                        return false;
                    }
                }
                return true;
            })
            .collect(Collectors.toList());
        System.out.println("Found " + availableGPs.size() + " available doctors:");
        availableGPs.forEach(d -> System.out.println(" - " + d.getName() + " (Fee: $" + d.getBaseConsultationFee() + ")"));

        // -----------------------------------------------------------------
        // SCENARIO 9
        // -----------------------------------------------------------------
        printHeader("Scenario 9 (Custom Exception Validation)");
        try {
            LocalDateTime pastDate = LocalDateTime.now().minusDays(5);
            System.out.println("Attempting to book an appointment on a past date: " + pastDate);
            scheduler.scheduleAppointment(insuredPatient, cardiologist2, pastDate, globalAppointments);
        } catch (InvalidAppointmentException e) {
            System.out.println("[CAUGHT EXPECTED EXCEPTION] -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected Error: " + e.getMessage());
        }

        // -----------------------------------------------------------------
        // SCENARIO 10
        // -----------------------------------------------------------------
        printHeader("Scenario 10 (Multi-threaded Stress Test)");
        Doctor highProfileDoc = globalDoctors.get(0);
        LocalDateTime stressTime = LocalDateTime.now().plusDays(2).withHour(9).withMinute(0).withSecond(0).withNano(0);
        System.out.println("10 Receptionists concurrently attempting to book " + highProfileDoc.getName() + " at " + stressTime);
        
        ExecutorService executor = Executors.newFixedThreadPool(10);
        for (int i = 0; i < 10; i++) {
            final int repId = i + 1;
            Patient pt = globalPatients.get(10 + i);
            executor.submit(() -> {
                try {
                    scheduler.scheduleAppointment(pt, highProfileDoc, stressTime, globalAppointments);
                    System.out.println("[Receptionist " + String.format("%02d", repId) + "] SUCCESS! Booked " + pt.getName());
                } catch (DoctorUnavailableException e) {
                    System.out.println("[Receptionist " + String.format("%02d", repId) + "] FAILED (Slot Taken).");
                } catch (Exception e) {
                    System.out.println("[Receptionist " + String.format("%02d", repId) + "] ERROR -> " + e.getMessage());
                }
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println("\n==========================================================");
        System.out.println("        ALL TEST SCENARIOS COMPLETED SUCCESSFULLY         ");
        System.out.println("==========================================================");
    }

    private static void printHeader(String title) {
        System.out.println("\n+-----------------------------------------------------------------------------+");
        System.out.printf("| %-75s |\n", title);
        System.out.println("+-----------------------------------------------------------------------------+");
    }
}
