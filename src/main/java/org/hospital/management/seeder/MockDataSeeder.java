package org.hospital.management.seeder;

import org.hospital.management.model.Doctor;
import org.hospital.management.model.InsurancePolicy;
import org.hospital.management.model.Patient;
import org.hospital.management.service.AppointmentScheduler;

import java.time.LocalDateTime;

public class MockDataSeeder {

    public static void seed(AppointmentScheduler scheduler) {
        System.out.println("Seeding mock data into the system...");

        // Create Doctors
        Doctor d1 = new Doctor("D001", "Dr. Alice Smith", "Cardiology", 150.0);
        Doctor d2 = new Doctor("D002", "Dr. Bob Jones", "Dermatology", 120.0);
        Doctor d3 = new Doctor("D003", "Dr. Carol White", "Neurology", 200.0);

        // Add some active time slots for today
        LocalDateTime today = LocalDateTime.now().withHour(10).withMinute(0).withSecond(0).withNano(0);

        scheduler.registerDoctor(d1);
        scheduler.registerDoctor(d2);
        scheduler.registerDoctor(d3);

        // Create Patients
        InsurancePolicy policy1 = new InsurancePolicy("BlueCross", "BC-98765", 0.80); // 80% coverage
        Patient p1 = new Patient("P001", "John Doe", 45, "555-1234", policy1);
        
        Patient p2 = new Patient("P002", "Jane Roe", 30, "555-5678", null); // Uninsured

        scheduler.registerPatient(p1);
        scheduler.registerPatient(p2);

        System.out.println("Seeding complete. 3 Doctors and 2 Patients registered.");
    }
}
