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

    @Test
    void testSlaCalculationAndPriorityAssignment() {
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Critical Power Outage in Server Room");
        req.setCategoryId(testCategory.getId());
        req.setBuildingId(testBuilding.getId());
        req.setFloor("Floor G");
        req.setRoomNumber("Server Room 01");
        req.setDescription("Main UPS tripped, servers running on backup battery");
        req.setPriority(PriorityLevel.LOW);

        Complaint complaint = complaintService.createComplaint(req, testStudent);
        assertEquals(PriorityLevel.LOW, complaint.getPriority());
        assertEquals(48, complaint.getPriority().getSlaHours());
        assertFalse(complaint.isOverdue());
        assertTrue(complaint.getSlaRemainingHours() <= 48 && complaint.getSlaRemainingHours() >= 47);

        // Escalate priority to CRITICAL during admin assignment
        Complaint assigned = complaintService.assignStaff(
                complaint.getId(), testStaff.getId(), PriorityLevel.CRITICAL, "Escalate to Critical SLA", testAdmin);
        assertEquals(PriorityLevel.CRITICAL, assigned.getPriority());
        assertEquals(4, assigned.getPriority().getSlaHours());

        // Simulate elapsed time exceeding CRITICAL SLA (4 hours)
        assigned.setCreatedAt(java.time.LocalDateTime.now().minusHours(6));
        assertTrue(assigned.isOverdue());
        assertEquals(0, assigned.getSlaRemainingHours());

        // Resolved complaints are never overdue
        complaintService.startWork(assigned.getId(), testStaff);
        Complaint resolved = complaintService.completeWork(assigned.getId(), null, "Restored main breaker", testStaff);
        resolved.setCreatedAt(java.time.LocalDateTime.now().minusHours(10));
        assertFalse(resolved.isOverdue());
    }

    @Test
    void testUnauthorizedWorkflowTransitions() {
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Water Cooler Leaking");
        req.setCategoryId(testCategory.getId());
        req.setBuildingId(testBuilding.getId());
        req.setFloor("Floor 2");
        req.setRoomNumber("Corridor");
        req.setDescription("Water pooling near electrical socket");
        req.setPriority(PriorityLevel.MEDIUM);

        Complaint complaint = complaintService.createComplaint(req, testStudent);

        // Assigning a non-staff user (student) as technician must fail
        assertThrows(IllegalArgumentException.class, () ->
                complaintService.assignStaff(complaint.getId(), testStudent.getId(), PriorityLevel.MEDIUM, "Invalid staff", testAdmin)
        );

        // Unassigned staff cannot start work
        assertThrows(com.smartcampus.maintenance.exception.UnauthorizedOperationException.class, () ->
                complaintService.startWork(complaint.getId(), testStaff)
        );

        // Another user cannot confirm or reopen student's complaint
        assertThrows(com.smartcampus.maintenance.exception.UnauthorizedOperationException.class, () ->
                complaintService.confirmResolution(complaint.getId(), testAdmin)
        );
        assertThrows(com.smartcampus.maintenance.exception.UnauthorizedOperationException.class, () ->
                complaintService.reopenComplaint(complaint.getId(), "Still leaking", testAdmin)
        );
    }

    @Test
    void testReopenAndCancelWorkflowTransitions() {
        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle("Projector Lamp Dim");
        req.setCategoryId(testCategory.getId());
        req.setBuildingId(testBuilding.getId());
        req.setFloor("Floor 3");
        req.setRoomNumber("Room 302");
        req.setDescription("Lamp is very dim even in dark room");
        req.setPriority(PriorityLevel.MEDIUM);

        Complaint complaint = complaintService.createComplaint(req, testStudent);
        complaintService.assignStaff(complaint.getId(), testStaff.getId(), PriorityLevel.MEDIUM, "Check bulb", testAdmin);
        complaintService.startWork(complaint.getId(), testStaff);
        complaintService.completeWork(complaint.getId(), null, "Cleaned lens", testStaff);

        // Student reopens because issue persists
        Complaint reopened = complaintService.reopenComplaint(complaint.getId(), "Lamp still dim after cleaning", testStudent);
        assertEquals(ComplaintStatus.REOPENED, reopened.getStatus());

        // Student cancels complaint
        Complaint cancelled = complaintService.cancelComplaint(complaint.getId(), "Room no longer in use", testStudent);
        assertEquals(ComplaintStatus.CANCELLED, cancelled.getStatus());
        assertFalse(cancelled.isOverdue());
    }
}

