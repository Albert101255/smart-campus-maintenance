package com.smartcampus.maintenance.service.impl;

import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.repository.ComplaintRepository;
import com.smartcampus.maintenance.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Override
    public Map<String, Long> getComplaintsByCategory() {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Object[]> list = complaintRepository.countComplaintsByCategory();
        for (Object[] obj : list) {
            if (obj[0] != null) {
                map.put((String) obj[0], (Long) obj[1]);
            }
        }
        return map;
    }

    @Override
    public Map<String, Long> getComplaintsByStatus() {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Object[]> list = complaintRepository.countComplaintsByStatus();
        for (Object[] obj : list) {
            if (obj[0] != null) {
                ComplaintStatus status = (ComplaintStatus) obj[0];
                map.put(status.getDisplayName(), (Long) obj[1]);
            }
        }
        return map;
    }

    @Override
    public Map<String, Long> getComplaintsByPriority() {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Object[]> list = complaintRepository.countComplaintsByPriority();
        for (Object[] obj : list) {
            if (obj[0] != null) {
                PriorityLevel priority = (PriorityLevel) obj[0];
                map.put(priority.getDisplayName(), (Long) obj[1]);
            }
        }
        return map;
    }

    @Override
    public Map<String, Long> getComplaintsByBuilding() {
        Map<String, Long> map = new LinkedHashMap<>();
        List<Object[]> list = complaintRepository.countComplaintsByBuilding();
        for (Object[] obj : list) {
            if (obj[0] != null) {
                map.put((String) obj[0], (Long) obj[1]);
            }
        }
        return map;
    }
}
