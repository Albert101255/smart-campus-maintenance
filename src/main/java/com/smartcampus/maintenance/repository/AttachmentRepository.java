package com.smartcampus.maintenance.repository;

import com.smartcampus.maintenance.entity.Attachment;
import com.smartcampus.maintenance.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByComplaintOrderByUploadedAtDesc(Complaint complaint);
}
