package org.hospital.management.seeder;

import org.hospital.management.model.Doctor;
import org.hospital.management.model.InsurancePolicy;
import org.hospital.management.model.Patient;

import java.util.Arrays;
import java.util.List;

public class DatabaseSeeder {

    public static List<Doctor> getMockDoctors() {
        return Arrays.asList(
                new Doctor("DOC-001", "Dr. Alice Smith", "Cardiology", 200.0),
                new Doctor("DOC-002", "Dr. Bob Jones", "Pediatrics", 150.0),
                new Doctor("DOC-003", "Dr. Charlie Brown", "Dermatology", 180.0),
                new Doctor("DOC-004", "Dr. Diana Prince", "General Medicine", 100.0),
                new Doctor("DOC-005", "Dr. Evan Wright", "Cardiology", 220.0)
        );
    }

    public static List<Patient> getMockPatients() {
        return Arrays.asList(
                new Patient("PAT-001", "John Doe", 45, "555-0101", new InsurancePolicy("Cigna", "CG-1001", 0.80)),
                new Patient("PAT-002", "Jane Roe", 32, "555-0102", new InsurancePolicy("Blue Shield", "BS-2002", 0.90)),
                new Patient("PAT-003", "Sam Smith", 28, "555-0103", null), // Uninsured
                new Patient("PAT-004", "Lucy Liu", 50, "555-0104", new InsurancePolicy("Aetna", "AT-3004", 0.70)),
                new Patient("PAT-005", "Tom Hanks", 65, "555-0105", new InsurancePolicy("Medicare", "MD-4005", 0.85)),
                new Patient("PAT-006", "Emma Watson", 24, "555-0106", null), // Uninsured
                new Patient("PAT-007", "Chris Evans", 39, "555-0107", new InsurancePolicy("UnitedHealth", "UH-5007", 0.50)),
                new Patient("PAT-008", "Scarlett Johansson", 35, "555-0108", new InsurancePolicy("Cigna", "CG-6008", 0.75)),
                new Patient("PAT-009", "Robert Downey", 55, "555-0109", null), // Uninsured
                new Patient("PAT-010", "Mark Ruffalo", 48, "555-0110", new InsurancePolicy("Blue Shield", "BS-7010", 0.60))
        );
    }
}
