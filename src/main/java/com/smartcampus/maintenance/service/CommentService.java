package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.entity.Comment;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;

import java.util.List;

public interface CommentService {
    Comment addComment(Long complaintId, CommentRequest request, User author);
    List<Comment> getCommentsForComplaint(Complaint complaint, User currentUser);
}
