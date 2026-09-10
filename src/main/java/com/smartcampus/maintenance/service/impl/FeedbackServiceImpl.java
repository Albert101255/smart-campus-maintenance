package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.dto.FeedbackRequest;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.Feedback;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.exception.ComplaintNotFoundException;
import com.smartcampus.maintenance.repository.ComplaintRepository;
import com.smartcampus.maintenance.repository.FeedbackRepository;
import com.smartcampus.maintenance.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class FeedbackServiceImpl implements FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Override
    public Feedback submitFeedback(Long complaintId, FeedbackRequest request, User student) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new ComplaintNotFoundException("Complaint not found"));

        if (feedbackRepository.existsByComplaint(complaint)) {
            throw new IllegalArgumentException("Feedback has already been submitted for this complaint");
        }

        Feedback feedback = new Feedback(complaint, student, request.getRating(), request.getComments());
        return feedbackRepository.save(feedback);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Feedback> getFeedbackForComplaint(Complaint complaint) {
        return feedbackRepository.findByComplaint(complaint);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Feedback> getAllFeedback(Pageable pageable) {
        return feedbackRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public double getAverageRating() {
        Double avg = feedbackRepository.getAverageRating();
        return avg != null ? Math.round(avg * 10.0) / 10.0 : 0.0;
    }
}
