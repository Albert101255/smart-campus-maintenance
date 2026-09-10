package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.ComplaintHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintHistoryRepository extends JpaRepository<ComplaintHistory, Long> {
    List<ComplaintHistory> findByComplaintOrderByTimestampAsc(Complaint complaint);
}
