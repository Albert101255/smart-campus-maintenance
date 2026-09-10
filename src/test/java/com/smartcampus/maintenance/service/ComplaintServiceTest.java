package com.smartcampus.maintenance.service;

import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.dto.RegistrationRequest;
import com.smartcampus.maintenance.entity.Building;
import com.smartcampus.maintenance.entity.Category;
import com.smartcampus.maintenance.entity.Complaint;
import com.smartcampus.maintenance.entity.User;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.repository.BuildingRepository;
import com.smartcampus.maintenance.repository.CategoryRepository;
import com.smartcampus.maintenance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class ComplaintServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    private User testStudent;
    private User testAdmin;
    private User testStaff;
    private Category testCategory;
    private Building testBuilding;

    @BeforeEach
    void setUp() {
        testStudent = userRepository.findByEmail("student@smartcampus.com").orElseGet(() -> {
            RegistrationRequest reg = new RegistrationRequest();
            reg.setName("Test Student");
            reg.setEmail("student_test@smartcampus.com");
            reg.setPassword("Password123");
            reg.setConfirmPassword("Password123");
            reg.setRegisterNumber("STUTEST001");
            reg.setDepartment("Computer Science");
            reg.setPhoneNumber("+1 555-9999");
            return userService.registerStudent(reg);
        });

        testAdmin = userRepository.findByEmail("admin@smartcampus.com").orElseThrow();
        testStaff = userRepository.findByEmail("staff@smartcampus.com").orElseThrow();

        testCategory = categoryRepository.findAll().get(0);
        testBuilding = buildingRepository.findAll().get(0);
    }

    @Test
    void testCompleteComplaintWorkflow() {
        // 1. Submit Complaint
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Test Broken Air Conditioner");
        req.setCategoryId(testCategory.getId());
        req.setBuildingId(testBuilding.getId());
        req.setFloor("Floor 1");
        req.setRoomNumber("Room 101");
        req.setDescription("Unit leaking water and making loud rattling noise");
        req.setPriority(PriorityLevel.HIGH);

        Complaint complaint = complaintService.createComplaint(req, testStudent);

        assertNotNull(complaint.getId());
        assertTrue(complaint.getComplaintNumber().startsWith("CMP-"));
        assertEquals(ComplaintStatus.OPEN, complaint.getStatus());
        assertEquals(PriorityLevel.HIGH, complaint.getPriority());

        // 2. Admin Assigns Maintenance Staff
        Complaint assigned = complaintService.assignStaff(complaint.getId(), testStaff.getId(), PriorityLevel.HIGH, "High priority lab issue", testAdmin);
        assertEquals(ComplaintStatus.ASSIGNED, assigned.getStatus());
        assertEquals(testStaff.getId(), assigned.getAssignedStaff().getId());

        // 3. Maintenance Staff Starts Work
        Complaint inProgress = complaintService.startWork(assigned.getId(), testStaff);
        assertEquals(ComplaintStatus.IN_PROGRESS, inProgress.getStatus());

        // 4. Maintenance Staff Completes Work
        Complaint resolved = complaintService.completeWork(inProgress.getId(), null, "Fixed fan belt and refilled refrigerant", testStaff);
        assertEquals(ComplaintStatus.RESOLVED, resolved.getStatus());
        assertNotNull(resolved.getResolvedAt());

        // 5. Student Confirms Resolution
        Complaint closed = complaintService.confirmResolution(resolved.getId(), testStudent);
        assertEquals(ComplaintStatus.CLOSED, closed.getStatus());
        assertNotNull(closed.getClosedAt());

        // 6. Verify Complaint History
        var history = complaintService.getComplaintHistory(closed);
        assertTrue(history.size() >= 5);
    }
}
