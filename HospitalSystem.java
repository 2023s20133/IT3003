import java.util.Date;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDateTime;

public class HospitalSystem {
    public static void main(String[] args) {
        System.out.println("Starting Hospital System...\n");

        // Create Insurance Policy
        InsurancePolicy policy = new InsurancePolicy("POL-123", "HealthCare Plus", 80.0);

        // Create Patient
        Patient patient = new Patient("P-001", "John Doe", "123-456-7890", "No major history");
        patient.setInsurancePolicy(policy);
        
        // UC_ManagePatient: Update Profile
        System.out.println("--- Patient Profile Update ---");
        patient.updateProfile("098-765-4321", "Allergic to peanuts");
        
        // Create Doctor
        Doctor doctor = new Doctor("D-001", "Alice Smith", "111-222-3333", "E-101", "Cardiology", "Cardiologist");
        Doctor newDoctor = new Doctor("D-002", "Bob Jones", "444-555-6666", "E-102", "Neurology", "Neurologist");

        // UC_VerifyDoc: Check Availability
        System.out.println("\n--- Doctor Availability ---");
        boolean isAvailable = doctor.checkAvailability(new Date());

        // Create Appointment
        System.out.println("\n--- Appointment Scheduling ---");
        Appointment appointment = new Appointment("APP-001", LocalDateTime.now().plusDays(2), patient, doctor);
        
        if (isAvailable) {
            appointment.schedule();
        }

        // UC_ProcessBilling
        System.out.println("\n--- Billing Process ---");
        Invoice invoice = appointment.getInvoice();
        invoice.processBilling(patient);

        // UC_AutoReassign
        System.out.println("\n--- Doctor Reassignment ---");
        appointment.reassignDoctor(newDoctor);

        // UC_CancelRefund
        System.out.println("\n--- Appointment Cancellation ---");
        appointment.cancel();

        System.out.println("\nTest Completed successfully.");
    }
}

// ==========================================
// Base User Class (Inheritance Root)
// ==========================================
abstract class User {
    protected String userId;
    protected String name;
    protected String contactInfo;

    public User(String userId, String name, String contactInfo) {
        this.userId = userId;
        this.name = name;
        this.contactInfo = contactInfo;
    }
}

// ==========================================
// Patient Class
// ==========================================
class Patient extends User {
    private String medicalHistory;
    private InsurancePolicy insurancePolicy; // 0..1 relationship
    private List<Appointment> appointments; // 0..* relationship

    public Patient(String userId, String name, String contactInfo, String medicalHistory) {
        super(userId, name, contactInfo);
        this.medicalHistory = medicalHistory;
        this.appointments = new ArrayList<>();
    }

    public void setInsurancePolicy(InsurancePolicy policy) {
        this.insurancePolicy = policy;
    }

    public InsurancePolicy getInsurancePolicy() {
        return this.insurancePolicy;
    }

    // Maps to UC_ManagePatient
    public void updateProfile(String newContactInfo, String newMedicalHistory) {
        this.contactInfo = newContactInfo;
        this.medicalHistory = newMedicalHistory;
        System.out.println("Patient profile updated for: " + this.name);
        
        // <<extend>> Validate Insurance Policy (If Insured)
        if (this.insurancePolicy != null) {
            boolean isValid = this.insurancePolicy.validatePolicy();
            System.out.println("Insurance Validation Status: " + isValid);
        }
    }
}

// ==========================================
// Staff and Doctor Classes
// ==========================================
abstract class Staff extends User {
    protected String employeeId;
    protected String department;

    public Staff(String userId, String name, String contactInfo, String employeeId, String department) {
        super(userId, name, contactInfo);
        this.employeeId = employeeId;
        this.department = department;
    }
}

class Doctor extends Staff {
    private String specialization;
    private Boolean isAvailable;
    private List<Appointment> assignedAppointments; // 0..* relationship

    public Doctor(String userId, String name, String contactInfo, String employeeId, String department, String specialization) {
        super(userId, name, contactInfo, employeeId, department);
        this.specialization = specialization;
        this.isAvailable = true;
        this.assignedAppointments = new ArrayList<>();
    }

    // Maps to UC_VerifyDoc
    public Boolean checkAvailability(Date date) {
        // Logic to check against schedule conflicts
        System.out.println("Checking availability for Dr. " + this.name + " on " + date);
        return this.isAvailable; 
    }
}

// ==========================================
// Insurance Policy Class
// ==========================================
class InsurancePolicy {
    private String policyId;
    private String providerName;
    private Double coveragePercent;

    public InsurancePolicy(String policyId, String providerName, Double coveragePercent) {
        this.policyId = policyId;
        this.providerName = providerName;
        this.coveragePercent = coveragePercent;
    }

    // Maps to UC_ValidateInsurance interacting with External Insurance System
    public Boolean validatePolicy() {
        System.out.println("Validating policy " + policyId + " with external system: " + providerName);
        return true; // Assuming valid for demonstration
    }
    
    public Double getCoveragePercent() {
        return coveragePercent;
    }
}

// ==========================================
// Appointment Class
// ==========================================
class Appointment {
    private String appointmentId;
    private LocalDateTime dateTime;
    private String status;
    
    private Patient patient; // 1 relationship
    private Doctor doctor; // 1 relationship
    private Invoice invoice; // 1 relationship (Composition - solid diamond)

    public Appointment(String appointmentId, LocalDateTime dateTime, Patient patient, Doctor doctor) {
        this.appointmentId = appointmentId;
        this.dateTime = dateTime;
        this.status = "Scheduled";
        this.patient = patient;
        this.doctor = doctor;
        
        // Composition: Invoice is generated directly upon Appointment creation
        this.invoice = new Invoice("INV-" + appointmentId, 150.00); 
    }

    // Maps to UC_ScheduleAppt
    public void schedule() {
        System.out.println("Appointment " + appointmentId + " scheduled.");
        // <<include>> Verify Doctor Availability is expected to happen before this succeeds
    }

    // Maps to UC_CancelRefund
    public void cancel() {
        this.status = "Cancelled";
        System.out.println("Appointment " + appointmentId + " has been cancelled.");
    }

    // Maps to UC_AutoReassign (Executed by Medical Director handling emergency absence)
    public void reassignDoctor(Doctor newDoc) {
        System.out.println("Reassigning appointment from Dr. " + this.doctor.name + " to Dr. " + newDoc.name);
        this.doctor = newDoc;
    }

    public Invoice getInvoice() {
        return this.invoice;
    }
}

// ==========================================
// Invoice Class
// ==========================================
class Invoice {
    private String invoiceId;
    private Double totalAmount;
    private Double copayAmount;

    public Invoice(String invoiceId, Double totalAmount) {
        this.invoiceId = invoiceId;
        this.totalAmount = totalAmount;
        this.copayAmount = totalAmount; // Default copay is full amount unless insured
    }

    // Maps to UC_ProcessBilling
    public void processBilling(Patient patient) {
        System.out.println("Processing billing for Invoice: " + invoiceId);
        
        // <<extend>> Apply Dynamic Insurance Co-pay (If Insured)
        if (patient.getInsurancePolicy() != null) {
            applyCopay(patient.getInsurancePolicy());
        }
        
        // <<include>> Generate Invoice & Receipt
        generateReceipt();
    }

    // Maps to UC_ApplyCopay
    public void applyCopay(InsurancePolicy policy) {
        Double coverage = policy.getCoveragePercent();
        this.copayAmount = this.totalAmount - (this.totalAmount * (coverage / 100));
        System.out.println("Insurance applied. New Co-pay amount: $" + this.copayAmount);
    }

    // Maps to UC_GenInvoice
    public void generateReceipt() {
        System.out.println("Generating Receipt for Invoice " + invoiceId + ". Amount due: $" + copayAmount);
    }
}
