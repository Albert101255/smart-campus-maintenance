package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Building;
import com.smartcampus.maintenance.entity.Category;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    Optional<Complaint> findByComplaintNumber(String complaintNumber);

    List<Complaint> findByStudentOrderByCreatedAtDesc(User student);

    Page<Complaint> findByStudent(User student, Pageable pageable);

    List<Complaint> findByAssignedStaffOrderByCreatedAtDesc(User staff);

    Page<Complaint> findByAssignedStaff(User staff, Pageable pageable);

    Page<Complaint> findByAssignedStaffAndStatus(User staff, ComplaintStatus status, Pageable pageable);

    long countByStatus(ComplaintStatus status);

    long countByPriority(PriorityLevel priority);

    long countByStudent(User student);

    long countByStudentAndStatus(User student, ComplaintStatus status);

    long countByAssignedStaff(User staff);

    long countByAssignedStaffAndStatus(User staff, ComplaintStatus status);

    @Query("SELECT c FROM Complaint c WHERE " +
           "(:status IS NULL OR c.status = :status) AND " +
           "(:priority IS NULL OR c.priority = :priority) AND " +
           "(:categoryId IS NULL OR c.category.id = :categoryId) AND " +
           "(:buildingId IS NULL OR c.building.id = :buildingId) AND " +
           "(:staffId IS NULL OR (c.assignedStaff IS NOT NULL AND c.assignedStaff.id = :staffId)) AND " +
           "(:studentId IS NULL OR c.student.id = :studentId) AND " +
           "(:keyword IS NULL OR :keyword = '' OR " +
           " LOWER(c.complaintNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.description) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.roomNumber) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.student.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(c.student.registerNumber) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Complaint> filterComplaints(@Param("status") ComplaintStatus status,
                                     @Param("priority") PriorityLevel priority,
                                     @Param("categoryId") Long categoryId,
                                     @Param("buildingId") Long buildingId,
                                     @Param("staffId") Long staffId,
                                     @Param("studentId") Long studentId,
                                     @Param("keyword") String keyword,
                                     Pageable pageable);

    @Query("SELECT c.category.name, COUNT(c) FROM Complaint c GROUP BY c.category.name")
    List<Object[]> countComplaintsByCategory();

    @Query("SELECT c.status, COUNT(c) FROM Complaint c GROUP BY c.status")
    List<Object[]> countComplaintsByStatus();

    @Query("SELECT c.priority, COUNT(c) FROM Complaint c GROUP BY c.priority")
    List<Object[]> countComplaintsByPriority();

    @Query("SELECT c.building.name, COUNT(c) FROM Complaint c GROUP BY c.building.name")
    List<Object[]> countComplaintsByBuilding();

    List<Complaint> findTop5ByOrderByCreatedAtDesc();

    List<Complaint> findTop5ByStudentOrderByCreatedAtDesc(User student);

    List<Complaint> findTop5ByAssignedStaffOrderByCreatedAtDesc(User staff);
}
