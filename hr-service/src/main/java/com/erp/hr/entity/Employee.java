package com.erp.hr.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "employees", schema = "hr")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "employee_number", unique = true, nullable = false)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(unique = true, nullable = false)
    private String email;

    private String phone;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    private String gender;

    @Column(name = "national_id")
    private String nationalId;

    @Column(name = "tax_id")
    private String taxId;

    private String street;
    private String city;
    private String state;
    @Column(name = "postal_code")
    private String postalCode;
    private String country;

    @Column(name = "department_id")
    private UUID departmentId;

    @Column(name = "position_title")
    private String positionTitle;

    @Column(name = "job_grade")
    private String jobGrade;

    @Column(name = "manager_id")
    private UUID managerId;

    @Column(name = "employment_type")
    @Enumerated(EnumType.STRING)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;

    @Enumerated(EnumType.STRING)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(name = "termination_date")
    private LocalDate terminationDate;

    @Column(name = "termination_reason")
    private String terminationReason;

    @Column(name = "base_salary_cents")
    private Long baseSalaryCents = 0L;

    private String currency = "USD";

    @Column(name = "pay_frequency")
    private String payFrequency = "MONTHLY";

    @Column(name = "bank_account")
    private String bankAccount;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "emergency_name")
    private String emergencyName;
    @Column(name = "emergency_relationship")
    private String emergencyRelationship;
    @Column(name = "emergency_phone")
    private String emergencyPhone;
    @Column(name = "emergency_email")
    private String emergencyEmail;

    @Column(name = "created_by")
    private String createdBy;
    @Column(name = "created_at")
    private Instant createdAt;
    @Column(name = "updated_by")
    private String updatedBy;
    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        updatedAt = Instant.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public enum EmploymentType {
        FULL_TIME, PART_TIME, CONTRACT, INTERN, TEMPORARY
    }

    public enum EmployeeStatus {
        ACTIVE, ON_LEAVE, SUSPENDED, TERMINATED, RETIRED
    }
}
