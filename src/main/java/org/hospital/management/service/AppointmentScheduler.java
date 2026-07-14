package org.hospital.management.service;

import org.hospital.management.exception.AppointmentConflictException;
import org.hospital.management.exception.DoctorNotFoundException;
import org.hospital.management.exception.HospitalException;
import org.hospital.management.exception.PatientNotFoundException;
import org.hospital.management.model.Appointment;
import org.hospital.management.model.AppointmentStatus;
import org.hospital.management.model.Doctor;
import org.hospital.management.model.Patient;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AppointmentScheduler {
    private final Map<String, Doctor> doctors;
    private final Map<String, Patient> patients;
    private final Map<String, Appointment> appointments;
    private final BillingEngine billingEngine;

    public AppointmentScheduler(BillingEngine billingEngine) {
        this.doctors = new ConcurrentHashMap<>();
        this.patients = new ConcurrentHashMap<>();
        this.appointments = new ConcurrentHashMap<>();
        this.billingEngine = billingEngine;
    }

    public void registerDoctor(Doctor doctor) {
        doctors.put(doctor.getDoctorId(), doctor);
    }

    public void registerPatient(Patient patient) {
        patients.put(patient.getPatientId(), patient);
    }

    public Doctor getDoctor(String id) {
        Doctor doc = doctors.get(id);
        if (doc == null) {
            throw new DoctorNotFoundException("Doctor with ID " + id + " not found.");
        }
        return doc;
    }

    public Patient getPatient(String id) {
        Patient pat = patients.get(id);
        if (pat == null) {
            throw new PatientNotFoundException("Patient with ID " + id + " not found.");
        }
        return pat;
    }

    public Appointment getAppointment(String id) {
        Appointment appt = appointments.get(id);
        if (appt == null) {
            throw new HospitalException("Appointment with ID " + id + " not found.");
        }
        return appt;
    }
    
    public Collection<Doctor> getAllDoctors() {
        return doctors.values();
    }
    
    public Collection<Patient> getAllPatients() {
        return patients.values();
    }
    
    public Collection<Appointment> getAllAppointments() {
        return appointments.values();
    }

    public synchronized Appointment schedule(String patientId, String doctorId, LocalDateTime slot) {
        Patient patient = getPatient(patientId);
        Doctor doctor = getDoctor(doctorId);

        doctor.bookSlot(slot); // Will throw DoctorUnavailableException if booked

        String appointmentId = "APT-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Appointment appointment = new Appointment(appointmentId, patient, doctor, slot);
        
        // Calculate billing immediately based on insurance
        double billingAmount = billingEngine.calculatePatientBill(appointment);
        appointment.setBillingAmount(billingAmount);

        appointments.put(appointmentId, appointment);

        return appointment;
    }

    public void completeAppointment(String appointmentId) {
        Appointment appt = getAppointment(appointmentId);
        appt.completeAppointment(appt.getBillingAmount());
    }

    public synchronized void cancelAppointment(String appointmentId) {
        Appointment appt = getAppointment(appointmentId);
        appt.cancelAppointment();
    }
}
