package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.entity.Comment;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.repository.CommentRepository;
import com.smartcampus.maintenance.repository.ComplaintRepository;
import com.smartcampus.maintenance.service.CommentService;
import com.smartcampus.maintenance.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CommentServiceImpl implements CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public Comment addComment(Long complaintId, CommentRequest request, User author) {
        Complaint complaint = complaintRepository.findById(complaintId)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found with ID: " + complaintId));

        Comment comment = new Comment(complaint, author, request.getContent(), request.isInternal());
        Comment saved = commentRepository.save(comment);

        // Notify relevant party if not internal
        if (!request.isInternal()) {
            if (author.getRole() == Role.STUDENT) {
                if (complaint.getAssignedStaff() != null) {
                    notificationService.sendNotification(complaint.getAssignedStaff(), "New Comment on Task",
                            author.getName() + " commented on complaint " + complaint.getComplaintNumber(),
                            "/staff/complaints/" + complaint.getId());
                }
            } else {
                notificationService.sendNotification(complaint.getStudent(), "New Comment on Complaint",
                        author.getName() + " commented on your complaint " + complaint.getComplaintNumber(),
                        "/student/complaints/" + complaint.getId());
            }
        }

        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Comment> getCommentsForComplaint(Complaint complaint, User currentUser) {
        if (currentUser.getRole() == Role.STUDENT) {
            return commentRepository.findByComplaintAndIsInternalFalseOrderByTimestampAsc(complaint);
        }
        return commentRepository.findByComplaintOrderByTimestampAsc(complaint);
    }
}
