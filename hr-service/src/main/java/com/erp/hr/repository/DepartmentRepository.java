package com.erp.hr.repository;

import com.erp.hr.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByIsActiveTrue();
    List<Department> findByParentId(UUID parentId);
}
