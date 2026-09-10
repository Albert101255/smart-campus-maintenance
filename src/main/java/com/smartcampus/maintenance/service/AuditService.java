package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.entity.AuditLog;
import com.smartcampus.maintenance.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AuditService {
    void log(User user, String action, String entityName, Long entityId, String details);
    Page<AuditLog> getAuditLogs(Pageable pageable);
}
