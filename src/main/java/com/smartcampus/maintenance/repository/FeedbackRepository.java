package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.Feedback;
import com.smartcampus.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByComplaint(Complaint complaint);
    boolean existsByComplaint(Complaint complaint);
    Page<Feedback> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT AVG(f.rating) FROM Feedback f")
    Double getAverageRating();
}
