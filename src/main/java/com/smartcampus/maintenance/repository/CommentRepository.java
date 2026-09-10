package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Comment;
import com.smartcampus.maintenance.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByComplaintOrderByTimestampAsc(Complaint complaint);
    List<Comment> findByComplaintAndIsInternalFalseOrderByTimestampAsc(Complaint complaint);
}
