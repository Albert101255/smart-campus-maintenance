package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.DashboardStatsDto;
import com.smartcampus.maintenance.entity.User;

public interface DashboardService {
    DashboardStatsDto getAdminDashboardStats();
    DashboardStatsDto getStudentDashboardStats(User student);
    DashboardStatsDto getStaffDashboardStats(User staff);
}
