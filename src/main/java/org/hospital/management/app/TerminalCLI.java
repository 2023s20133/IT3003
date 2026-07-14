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
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.CopyOnWriteArrayList;

public class TerminalCLI {

    private static final List<Doctor> globalDoctors = DataSeeder.getMockDoctors();
    private static final List<Patient> globalPatients = DataSeeder.getMockPatients();
    private static final List<Appointment> globalAppointments = new CopyOnWriteArrayList<>();
    
    private static final AppointmentSchedulerService scheduler = new AppointmentSchedulerService();
    private static final BillingEngine billingEngine = new BillingEngine();
    private static final EmergencyReassignmentService reassignmentService = new EmergencyReassignmentService();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        System.out.println("==========================================");
        System.out.println(" Welcome to the Hospital Management CLI ");
        System.out.println("==========================================");

        while (running) {
            System.out.println("\n--- MAIN MENU ---");
            System.out.println("1. View All Doctors");
            System.out.println("2. View All Patients");
            System.out.println("3. Book an Appointment");
            System.out.println("4. View Scheduled Appointments");
            System.out.println("5. Complete an Appointment (Generate Bill)");
            System.out.println("6. Cancel an Appointment");
            System.out.println("7. Trigger Emergency Doctor Reassignment");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine();

            try {
                switch (choice) {
                    case "1":
                        viewDoctors();
                        break;
                    case "2":
                        viewPatients();
                        break;
                    case "3":
                        bookAppointment(scanner);
                        break;
                    case "4":
                        viewAppointments();
                        break;
                    case "5":
                        completeAppointment(scanner);
                        break;
                    case "6":
                        cancelAppointment(scanner);
                        break;
                    case "7":
                        triggerEmergency(scanner);
                        break;
                    case "8":
                        running = false;
                        System.out.println("Exiting the system. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (Exception e) {
                System.out.println("\n[ERROR] An unexpected error occurred: " + e.getMessage());
            }
        }
        scanner.close();
    }

    private static void viewDoctors() {
        System.out.println("\n--- Registered Doctors ---");
        for (Doctor d : globalDoctors) {
            System.out.println(d.getDoctorId() + " | " + d.getName() + " | " + d.getSpecialization() + " | Fee: $" + d.getBaseConsultationFee());
        }
    }

    private static void viewPatients() {
        System.out.println("\n--- Registered Patients ---");
        for (Patient p : globalPatients) {
            String ins = p.hasInsurance() ? p.getInsurancePolicy().getProviderName() : "Uninsured";
            System.out.println(p.getPatientId() + " | " + p.getName() + " | " + ins);
        }
    }

    private static void bookAppointment(Scanner scanner) {
        System.out.println("\n--- Book an Appointment ---");
        System.out.print("Enter Patient ID (e.g., PAT-001): ");
        String patientId = scanner.nextLine();
        
        Optional<Patient> pOpt = globalPatients.stream().filter(p -> p.getPatientId().equalsIgnoreCase(patientId)).findFirst();
        if (pOpt.isEmpty()) {
            System.out.println("[ERROR] Patient not found!");
            return;
        }

        System.out.print("Enter Doctor ID (e.g., DOC-001): ");
        String doctorId = scanner.nextLine();
        
        Optional<Doctor> dOpt = globalDoctors.stream().filter(d -> d.getDoctorId().equalsIgnoreCase(doctorId)).findFirst();
        if (dOpt.isEmpty()) {
            System.out.println("[ERROR] Doctor not found!");
            return;
        }

        System.out.print("Enter Date and Time (YYYY-MM-DD HH:MM): ");
        String dtStr = scanner.nextLine();
        LocalDateTime dt;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            dt = LocalDateTime.parse(dtStr, formatter);
        } catch (DateTimeParseException e) {
            System.out.println("[ERROR] Invalid date format. Please use YYYY-MM-DD HH:MM");
            return;
        }

        try {
            Appointment appt = scheduler.scheduleAppointment(pOpt.get(), dOpt.get(), dt, globalAppointments);
            System.out.println("\n[SUCCESS] Appointment Booked!");
            System.out.println("ID: " + appt.getAppointmentId());
            System.out.println("Status: " + appt.getStatus());
        } catch (DoctorUnavailableException | InvalidAppointmentException e) {
            System.out.println("\n[BOOKING FAILED] " + e.getMessage());
        }
    }

    private static void viewAppointments() {
        System.out.println("\n--- All Appointments ---");
        if (globalAppointments.isEmpty()) {
            System.out.println("No appointments scheduled.");
            return;
        }
        for (Appointment a : globalAppointments) {
            System.out.println(a.getAppointmentId() + " | " + a.getPatient().getName() + " with " + a.getDoctor().getName() + " | " + a.getDateTime() + " | Status: " + a.getStatus());
        }
    }

    private static void completeAppointment(Scanner scanner) {
        System.out.println("\n--- Complete Appointment & Bill ---");
        System.out.print("Enter Appointment ID: ");
        String apptId = scanner.nextLine();

        Optional<Appointment> aOpt = globalAppointments.stream().filter(a -> a.getAppointmentId().equalsIgnoreCase(apptId)).findFirst();
        if (aOpt.isEmpty()) {
            System.out.println("[ERROR] Appointment not found.");
            return;
        }

        Appointment appt = aOpt.get();
        if (appt.getStatus() != org.hospital.management.model.AppointmentStatus.SCHEDULED) {
            System.out.println("[ERROR] Only SCHEDULED appointments can be completed. Current status: " + appt.getStatus());
            return;
        }

        double bill = billingEngine.calculatePatientBill(appt);
        appt.completeAppointment(bill);
        
        System.out.println("\n[SUCCESS] Appointment Completed.");
        System.out.println("Final Bill for " + appt.getPatient().getName() + ": $" + bill);
    }

    private static void cancelAppointment(Scanner scanner) {
        System.out.println("\n--- Cancel Appointment ---");
        System.out.print("Enter Appointment ID: ");
        String apptId = scanner.nextLine();

        Optional<Appointment> aOpt = globalAppointments.stream().filter(a -> a.getAppointmentId().equalsIgnoreCase(apptId)).findFirst();
        if (aOpt.isEmpty()) {
            System.out.println("[ERROR] Appointment not found.");
            return;
        }

        Appointment appt = aOpt.get();
        if (appt.getStatus() == org.hospital.management.model.AppointmentStatus.CANCELLED) {
            System.out.println("Appointment is already cancelled.");
            return;
        }
        
        try {
            appt.cancelAppointment();
            System.out.println("\n[SUCCESS] Appointment Cancelled.");
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    private static void triggerEmergency(Scanner scanner) {
        System.out.println("\n--- Trigger Emergency Reassignment ---");
        System.out.print("Enter the ID of the Doctor who is absent (e.g., DOC-001): ");
        String doctorId = scanner.nextLine();

        Optional<Doctor> dOpt = globalDoctors.stream().filter(d -> d.getDoctorId().equalsIgnoreCase(doctorId)).findFirst();
        if (dOpt.isEmpty()) {
            System.out.println("[ERROR] Doctor not found!");
            return;
        }

        System.out.print("Enter Date (YYYY-MM-DD): ");
        String dStr = scanner.nextLine();
        LocalDateTime targetDate;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            targetDate = LocalDateTime.parse(dStr + " 00:00", formatter);
        } catch (DateTimeParseException e) {
            System.out.println("[ERROR] Invalid date format. Please use YYYY-MM-DD");
            return;
        }

        System.out.println("\nProcessing Reassignments for " + dOpt.get().getName() + " on " + targetDate.toLocalDate() + "...");
        reassignmentService.handleEmergencyDoctorAbsence(dOpt.get(), targetDate, globalDoctors, globalAppointments);
        System.out.println("Reassignment process finished.");
    }
}
