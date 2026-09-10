package com.smartcampus.maintenance.config;

import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.dto.FeedbackRequest;
import com.smartcampus.maintenance.entity.*;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.repository.*;
import com.smartcampus.maintenance.service.ComplaintService;
import com.smartcampus.maintenance.service.FeedbackService;
import com.smartcampus.maintenance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            seedCategories();
        }
        if (buildingRepository.count() == 0) {
            seedBuildings();
        }
        if (userRepository.count() == 0) {
            seedUsers();
            seedSampleComplaints();
        }
    }

    private void seedCategories() {
        List<Category> categories = Arrays.asList(
                new Category("Electrical", "Lighting, switches, power sockets, wiring"),
                new Category("Plumbing", "Pipes, taps, drainage, water supply"),
                new Category("Computer / IT", "Lab PCs, OS issues, keyboards, mice"),
                new Category("Internet / Network", "Wi-Fi access points, LAN connections"),
                new Category("Classroom Equipment", "Blackboards, podiums, markers, dusters"),
                new Category("Furniture", "Chairs, desks, tables, benches"),
                new Category("Cleaning", "Classroom cleanliness, trash bins"),
                new Category("Air Conditioning", "AC cooling, filters, remote controls"),
                new Category("Lift / Elevator", "Campus elevators, door sensors"),
                new Category("Laboratory Equipment", "Oscilloscopes, meters, chemical hoods"),
                new Category("Projector / Smart Board", "Display projectors, HDMI connections, interactive boards"),
                new Category("Water Supply", "Drinking water coolers, purifiers"),
                new Category("Washroom", "Restroom maintenance, hygiene, fittings"),
                new Category("Building Infrastructure", "Walls, doors, windows, ceiling tiles"),
                new Category("Hostel", "Hostel room maintenance, bed frames, lockers"),
                new Category("Library", "Reading tables, lighting, stacks"),
                new Category("Other", "General campus infrastructure problems")
        );
        categoryRepository.saveAll(categories);
    }

    private void seedBuildings() {
        List<Building> buildings = Arrays.asList(
                new Building("Main Block", "MB", "Central Administrative & Academic Area"),
                new Building("CSE Block", "CSE", "Computer Science & Engineering Department"),
                new Building("ECE Block", "ECE", "Electronics & Communication Department"),
                new Building("EEE Block", "EEE", "Electrical & Electronics Department"),
                new Building("Mechanical Block", "MECH", "Mechanical Engineering Workshops"),
                new Building("Science Block", "SCI", "Physics, Chemistry & Basic Sciences"),
                new Building("Library", "LIB", "Central Campus Digital & Print Library"),
                new Building("Central Laboratory", "LAB", "Advanced Research & Computing Labs"),
                new Building("Auditorium", "AUD", "Main Campus Convention Hall"),
                new Building("Hostel Block A", "HSA", "Boys Residence"),
                new Building("Hostel Block B", "HSB", "Girls Residence"),
                new Building("Cafeteria", "CAF", "Student & Faculty Dining Center")
        );
        buildingRepository.saveAll(buildings);
    }

    private void seedUsers() {
        // Admin
        User admin = new User();
        admin.setName("Campus Chief Administrator");
        admin.setEmail("admin@smartcampus.com");
        admin.setPassword(passwordEncoder.encode("Admin@123"));
        admin.setRole(Role.ADMIN);
        admin.setDepartment("Campus Operations & Infrastructure");
        admin.setPhoneNumber("+1 555-0199");
        admin.setEnabled(true);
        userRepository.save(admin);

        // Maintenance Staff Members
        User staff1 = new User();
        staff1.setName("Ravi Kumar");
        staff1.setEmail("staff@smartcampus.com");
        staff1.setPassword(passwordEncoder.encode("Staff@123"));
        staff1.setRole(Role.MAINTENANCE_STAFF);
        staff1.setDepartment("Electrical & Appliances");
        staff1.setPhoneNumber("+1 555-0144");
        staff1.setEnabled(true);
        userRepository.save(staff1);

        User staff2 = new User();
        staff2.setName("Suresh Babu");
        staff2.setEmail("staff2@smartcampus.com");
        staff2.setPassword(passwordEncoder.encode("Staff@123"));
        staff2.setRole(Role.MAINTENANCE_STAFF);
        staff2.setDepartment("Plumbing & Sanitation");
        staff2.setPhoneNumber("+1 555-0155");
        staff2.setEnabled(true);
        userRepository.save(staff2);

        User staff3 = new User();
        staff3.setName("Anil Verma");
        staff3.setEmail("staff3@smartcampus.com");
        staff3.setPassword(passwordEncoder.encode("Staff@123"));
        staff3.setRole(Role.MAINTENANCE_STAFF);
        staff3.setDepartment("IT & Audio-Visual Support");
        staff3.setPhoneNumber("+1 555-0166");
        staff3.setEnabled(true);
        userRepository.save(staff3);

        // Students
        User student1 = new User();
        student1.setName("Alex Morgan");
        student1.setEmail("student@smartcampus.com");
        student1.setPassword(passwordEncoder.encode("Student@123"));
        student1.setRole(Role.STUDENT);
        student1.setRegisterNumber("STU2026001");
        student1.setDepartment("Computer Science & Engineering");
        student1.setPhoneNumber("+1 555-0111");
        student1.setEnabled(true);
        userRepository.save(student1);

        User student2 = new User();
        student2.setName("Priya Sharma");
        student2.setEmail("student2@smartcampus.com");
        student2.setPassword(passwordEncoder.encode("Student@123"));
        student2.setRole(Role.STUDENT);
        student2.setRegisterNumber("STU2026002");
        student2.setDepartment("Electronics & Communication");
        student2.setPhoneNumber("+1 555-0122");
        student2.setEnabled(true);
        userRepository.save(student2);
    }

    private void seedSampleComplaints() {
        User admin = userRepository.findByEmail("admin@smartcampus.com").orElse(null);
        User student1 = userRepository.findByEmail("student@smartcampus.com").orElse(null);
        User student2 = userRepository.findByEmail("student2@smartcampus.com").orElse(null);
        User staff1 = userRepository.findByEmail("staff@smartcampus.com").orElse(null);
        User staff3 = userRepository.findByEmail("staff3@smartcampus.com").orElse(null);

        Category projectorCat = categoryRepository.findByName("Projector / Smart Board").orElse(null);
        Category electricalCat = categoryRepository.findByName("Electrical").orElse(null);
        Category wifiCat = categoryRepository.findByName("Internet / Network").orElse(null);
        Category plumbingCat = categoryRepository.findByName("Plumbing").orElse(null);

        Building cseBlock = buildingRepository.findByName("CSE Block").orElse(null);
        Building mainBlock = buildingRepository.findByName("Main Block").orElse(null);
        Building hostelA = buildingRepository.findByName("Hostel Block A").orElse(null);

        if (student1 != null && projectorCat != null && cseBlock != null) {
            // Complaint 1: In Progress
            ComplaintCreateRequest req1 = new ComplaintCreateRequest();
            req1.setTitle("Projector display flickering in CSE Classroom 204");
            req1.setCategoryId(projectorCat.getId());
            req1.setBuildingId(cseBlock.getId());
            req1.setFloor("Floor 2");
            req1.setRoomNumber("Room 204");
            req1.setLocationDescription("Mounted on ceiling near white board");
            req1.setDescription("The overhead projector power turns off intermittently during lectures. HDMI input is also unstable.");
            req1.setPriority(PriorityLevel.HIGH);
            Complaint c1 = complaintService.createComplaint(req1, student1);

            if (admin != null && staff3 != null) {
                complaintService.assignStaff(c1.getId(), staff3.getId(), PriorityLevel.HIGH, "Please check ceiling mounting and HDMI cable", admin);
                complaintService.startWork(c1.getId(), staff3);
            }
        }

        if (student2 != null && electricalCat != null && mainBlock != null) {
            // Complaint 2: Open
            ComplaintCreateRequest req2 = new ComplaintCreateRequest();
            req2.setTitle("Tube light not working in corridor near Seminar Hall");
            req2.setCategoryId(electricalCat.getId());
            req2.setBuildingId(mainBlock.getId());
            req2.setFloor("Floor 1");
            req2.setRoomNumber("Corridor 102");
            req2.setLocationDescription("Near East entrance");
            req2.setDescription("Main corridor tube light is completely dead, causing poor visibility in the evening.");
            req2.setPriority(PriorityLevel.MEDIUM);
            complaintService.createComplaint(req2, student2);
        }

        if (student1 != null && wifiCat != null && cseBlock != null) {
            // Complaint 3: Resolved & Closed with Rating
            ComplaintCreateRequest req3 = new ComplaintCreateRequest();
            req3.setTitle("Wi-Fi Access Point offline in Programming Lab 3");
            req3.setCategoryId(wifiCat.getId());
            req3.setBuildingId(cseBlock.getId());
            req3.setFloor("Floor 3");
            req3.setRoomNumber("Lab 3");
            req3.setLocationDescription("AP unit 04 above server rack");
            req3.setDescription("Students cannot connect to Campus-5G Wi-Fi network during practical session.");
            req3.setPriority(PriorityLevel.CRITICAL);
            Complaint c3 = complaintService.createComplaint(req3, student1);

            if (admin != null && staff3 != null) {
                complaintService.assignStaff(c3.getId(), staff3.getId(), PriorityLevel.CRITICAL, "Immediate attention needed for active lab", admin);
                complaintService.startWork(c3.getId(), staff3);
                complaintService.completeWork(c3.getId(), null, "Rebooted access point and replaced faulty Ethernet patch cord.", staff3);
                complaintService.confirmResolution(c3.getId(), student1);

                FeedbackRequest fb = new FeedbackRequest();
                fb.setRating(5);
                fb.setComments("Issue was fixed super fast before our next lab exam! Thank you team!");
                feedbackService.submitFeedback(c3.getId(), fb, student1);
            }
        }
    }
}
