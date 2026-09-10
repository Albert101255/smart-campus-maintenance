package com.smartcampus.maintenance.service;

import java.util.Map;

public interface ReportService {
    Map<String, Long> getComplaintsByCategory();
    Map<String, Long> getComplaintsByStatus();
    Map<String, Long> getComplaintsByPriority();
    Map<String, Long> getComplaintsByBuilding();
}
