package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.FeedbackRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.Feedback;
import com.smartcampus.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface FeedbackService {
    Feedback submitFeedback(Long complaintId, FeedbackRequest request, User student);
    Optional<Feedback> getFeedbackForComplaint(Complaint complaint);
    Page<Feedback> getAllFeedback(Pageable pageable);
    double getAverageRating();
}
