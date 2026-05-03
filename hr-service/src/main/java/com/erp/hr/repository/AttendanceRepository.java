package com.erp.hr.repository;

import com.erp.hr.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    Optional<Attendance> findByEmployeeIdAndAttendanceDate(UUID employeeId, LocalDate date);
    Page<Attendance> findByEmployeeId(UUID employeeId, Pageable pageable);
    List<Attendance> findByEmployeeIdAndAttendanceDateBetween(UUID employeeId, LocalDate start, LocalDate end);
    Page<Attendance> findByAttendanceDate(LocalDate date, Pageable pageable);
}
