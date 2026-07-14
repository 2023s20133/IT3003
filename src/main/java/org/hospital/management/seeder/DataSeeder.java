package org.hospital.management.seeder;

import org.hospital.management.model.Doctor;
import org.hospital.management.model.InsurancePolicy;
import org.hospital.management.model.Patient;

import java.util.ArrayList;
import java.util.List;

public class DataSeeder {

    public static List<Doctor> getMockDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        String[] specializations = {"Cardiology", "Pediatrics", "Dermatology", "General Medicine"};
        
        for (int i = 1; i <= 20; i++) {
            String spec = specializations[i % 4];
            double fee = 100.0 + (i * 5);
            doctors.add(new Doctor("DOC-" + String.format("%03d", i), "Doctor " + i, spec, fee));
        }
        return doctors;
    }

    public static List<Patient> getMockPatients() {
        List<Patient> patients = new ArrayList<>();
        String[] providers = {"Cigna", "Blue Shield", "Aetna", "Medicare", "UnitedHealth"};
        
        for (int i = 1; i <= 50; i++) {
            InsurancePolicy policy = null;
            // Roughly 70% of patients will have insurance
            if (i % 10 > 2) { 
                String provider = providers[i % 5];
                double coverage = 0.5 + ((i % 5) * 0.1); // Range 0.5 to 0.9
                policy = new InsurancePolicy(provider, "POL-" + i, coverage);
            }
            patients.add(new Patient("PAT-" + String.format("%03d", i), "Patient " + i, 20 + (i % 60), "555-" + String.format("%04d", i), policy));
        }
        return patients;
    }
}
