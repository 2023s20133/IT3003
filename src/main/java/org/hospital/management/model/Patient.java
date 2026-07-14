package org.hospital.management.model;

/**
 * Represents a patient in the hospital.
 */
public class Patient {
    private String patientId;
    private String name;
    private int age;
    private String contactNumber;
    private InsurancePolicy insurancePolicy;

    public Patient(String patientId, String name, int age, String contactNumber, InsurancePolicy insurancePolicy) {
        this.patientId = patientId;
        this.name = name;
        this.age = age;
        this.contactNumber = contactNumber;
        this.insurancePolicy = insurancePolicy;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public InsurancePolicy getInsurancePolicy() {
        return insurancePolicy;
    }

    public void setInsurancePolicy(InsurancePolicy insurancePolicy) {
        this.insurancePolicy = insurancePolicy;
    }

    /**
     * Checks if the patient has a valid insurance policy.
     * @return true if insurance policy is present, false otherwise
     */
    public boolean hasInsurance() {
        return insurancePolicy != null;
    }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", contactNumber='" + contactNumber + '\'' +
                ", insurancePolicy=" + (insurancePolicy != null ? insurancePolicy.getProviderName() : "None") +
                '}';
    }
}
