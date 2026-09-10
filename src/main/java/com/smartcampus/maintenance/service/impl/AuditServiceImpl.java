package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.entity.AuditLog;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.repository.AuditLogRepository;
import com.smartcampus.maintenance.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditServiceImpl implements AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Override
    public void log(User user, String action, String entityName, Long entityId, String details) {
        AuditLog log = new AuditLog(user, action, entityName, entityId, details, "127.0.0.1");
        auditLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLog> getAuditLogs(Pageable pageable) {
        return auditLogRepository.findAllByOrderByTimestampDesc(pageable);
    }
}
