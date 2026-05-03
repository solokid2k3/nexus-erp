package com.erp.hr.repository;

import com.erp.hr.entity.Employee;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);

    Page<Employee> findByDepartmentId(UUID departmentId, Pageable pageable);

    Page<Employee> findByStatus(Employee.EmployeeStatus status, Pageable pageable);

    Page<Employee> findByManagerId(UUID managerId, Pageable pageable);

    @Query("SELECT e FROM Employee e WHERE e.status = 'ACTIVE' AND " +
           "(e.firstName LIKE %:search% OR e.lastName LIKE %:search% OR e.email LIKE %:search%)")
    Page<Employee> searchEmployees(String search, Pageable pageable);

    List<Employee> findByStatusAndDepartmentId(Employee.EmployeeStatus status, UUID departmentId);

    List<Employee> findByStatusAndPayFrequency(Employee.EmployeeStatus status, String payFrequency);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = :status")
    long countByStatus(Employee.EmployeeStatus status);

    @Query("SELECT COUNT(e) FROM Employee e WHERE e.status = 'ACTIVE' AND e.departmentId = :deptId")
    long countActiveByDepartment(UUID deptId);

    @Query("SELECT e.departmentId, COUNT(e) FROM Employee e WHERE e.status = 'ACTIVE' GROUP BY e.departmentId")
    List<Object[]> countByDepartment();

    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(e.employeeNumber, 5, 10) AS integer)), 0) + 1 FROM Employee e")
    Integer getNextEmployeeSequence();
}
