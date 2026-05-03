package com.erp.hr.repository;

import com.erp.hr.entity.LeaveRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, UUID> {
    Page<LeaveRequest> findByEmployeeId(UUID employeeId, Pageable pageable);
    Page<LeaveRequest> findByStatus(LeaveRequest.LeaveStatus status, Pageable pageable);
    long countByStatus(LeaveRequest.LeaveStatus status);

    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.status = 'APPROVED' AND " +
           ":date BETWEEN lr.startDate AND lr.endDate")
    List<LeaveRequest> findApprovedForDate(LocalDate date);
}
