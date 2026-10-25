package com.smartcampus.maintenance.controller.api;

import com.smartcampus.maintenance.dto.CommentRequest;
import com.smartcampus.maintenance.dto.ComplaintCreateRequest;
import com.smartcampus.maintenance.dto.DashboardStatsDto;
import com.smartcampus.maintenance.dto.FeedbackRequest;
import com.smartcampus.maintenance.dto.RegistrationRequest;
import com.smartcampus.maintenance.entity.*;
import com.smartcampus.maintenance.enums.ComplaintStatus;
import com.smartcampus.maintenance.enums.PriorityLevel;
import com.smartcampus.maintenance.enums.Role;
import com.smartcampus.maintenance.repository.*;
import com.smartcampus.maintenance.security.CustomUserDetails;
import com.smartcampus.maintenance.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class RestApiController {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BuildingService buildingService;

    @Autowired
    private BuildingRepository buildingRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private FeedbackService feedbackService;

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private AuditService auditService;

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private AuthenticationManager authenticationManager;

    private static final DateTimeFormatter ISO_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // -------------------------------------------------------------------------
    // Health & System Info
    // -------------------------------------------------------------------------
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("status", "UP");
        resp.put("system", "Smart Campus Facilities & Maintenance Management");
        resp.put("version", "1.0.0");
        resp.put("serverTime", LocalDateTime.now().format(ISO_FMT));
        resp.put("database", "Operational (H2/MySQL Mode)");
        resp.put("complaintCount", complaintRepository.count());
        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------------------
    // Authentication & Current User
    // -------------------------------------------------------------------------
    @GetMapping("/auth/me")
    public ResponseEntity<Map<String, Object>> getMe(Authentication auth) {
        User user = resolveUser(auth, false);
        if (user == null) {
            Map<String, Object> resp = new HashMap<>();
            resp.put("authenticated", false);
            return ResponseEntity.ok(resp);
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("authenticated", true);
        resp.put("user", formatUserMap(user));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> creds, HttpServletRequest request) {
        String email = creds.get("email");
        String password = creds.get("password");

        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);
            request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
            User user = userDetails.getUser();

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Authentication successful");
            resp.put("user", formatUserMap(user));
            return ResponseEntity.ok(resp);
        } catch (AuthenticationException ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Invalid email address or password.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        Map<String, Object> resp = new HashMap<>();
        resp.put("success", true);
        resp.put("message", "Logged out successfully");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/auth/demo-switch")
    public ResponseEntity<Map<String, Object>> demoSwitch(@RequestParam(value = "role", defaultValue = "ADMIN") String roleName,
                                                          HttpServletRequest request) {
        String email;
        if ("STUDENT".equalsIgnoreCase(roleName)) {
            email = "student@smartcampus.com";
        } else if ("STAFF".equalsIgnoreCase(roleName) || "MAINTENANCE_STAFF".equalsIgnoreCase(roleName)) {
            email = "staff@smartcampus.com";
        } else {
            email = "admin@smartcampus.com";
        }

        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("success", false, "message", "Seed user not found"));
        }

        CustomUserDetails userDetails = new CustomUserDetails(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        request.getSession(true).setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("user", formatUserMap(user));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/auth/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody RegistrationRequest req) {
        try {
            User registered = userService.registerStudent(req);
            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", true);
            resp.put("message", "Registration successful. You can now log in.");
            resp.put("user", formatUserMap(registered));
            return ResponseEntity.ok(resp);
        } catch (Exception ex) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    // -------------------------------------------------------------------------
    // Dashboard KPIs & Analytics
    // -------------------------------------------------------------------------
    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats(Authentication auth) {
        DashboardStatsDto stats = dashboardService.getAdminDashboardStats();
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("totalComplaints", stats.getTotalComplaints());
        resp.put("openComplaints", stats.getOpenComplaints());
        resp.put("assignedComplaints", stats.getAssignedComplaints());
        resp.put("inProgressComplaints", stats.getInProgressComplaints());
        resp.put("onHoldComplaints", stats.getOnHoldComplaints());
        resp.put("resolvedComplaints", stats.getResolvedComplaints());
        resp.put("closedComplaints", stats.getClosedComplaints());
        resp.put("criticalComplaints", stats.getCriticalComplaints());
        resp.put("overdueComplaints", stats.getOverdueComplaints());
        resp.put("totalStudents", stats.getTotalStudents());
        resp.put("totalStaff", stats.getTotalStaff());
        resp.put("avgRating", Math.round(stats.getAvgRating() * 10.0) / 10.0);

        // Calculate resolution rate
        long resolvedOrClosed = stats.getResolvedComplaints() + stats.getClosedComplaints();
        double resolutionRate = stats.getTotalComplaints() > 0 ? ((double) resolvedOrClosed / stats.getTotalComplaints()) * 100.0 : 0.0;
        resp.put("resolutionRate", Math.round(resolutionRate * 10.0) / 10.0);

        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------------------
    // Complaints Management
    // -------------------------------------------------------------------------
    @GetMapping("/complaints")
    public ResponseEntity<Map<String, Object>> getComplaints(
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) PriorityLevel priority,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long buildingId,
            @RequestParam(required = false) Long staffId,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            Authentication auth
    ) {
        User currentUser = resolveUser(auth, false);
        if (currentUser != null) {
            if (currentUser.getRole() == Role.STUDENT && studentId == null) {
                studentId = currentUser.getId();
            } else if (currentUser.getRole() == Role.MAINTENANCE_STAFF && staffId == null) {
                staffId = currentUser.getId();
            }
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Complaint> complaintPage = complaintService.filterComplaints(
                status, priority, categoryId, buildingId, staffId, studentId, search, pageable
        );

        List<Map<String, Object>> dtoList = complaintPage.getContent().stream()
                .map(this::formatComplaintMap)
                .collect(Collectors.toList());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("complaints", dtoList);
        resp.put("totalElements", complaintPage.getTotalElements());
        resp.put("totalPages", complaintPage.getTotalPages());
        resp.put("currentPage", complaintPage.getNumber());
        resp.put("pageSize", complaintPage.getSize());
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/complaints/{id}")
    public ResponseEntity<?> getComplaintDetail(@PathVariable Long id, Authentication auth) {
        Complaint complaint = complaintService.getComplaintById(id);
        if (complaint == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Work order not found"));
        }

        Map<String, Object> detail = formatComplaintMap(complaint);
        if (complaint.getBeforeImagePath() != null && !complaint.getBeforeImagePath().isBlank()) {
            String dataUri = fileStorageService.loadAsDataUri(complaint.getBeforeImagePath());
            if (dataUri != null) {
                detail.put("imageData", dataUri);
            }
        }

        // Timeline history
        List<ComplaintHistory> histories = complaintService.getComplaintHistory(complaint);
        List<Map<String, Object>> historyList = histories.stream().map(h -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", h.getId());
            m.put("action", h.getAction());
            m.put("oldStatus", h.getOldStatus() != null ? h.getOldStatus().name() : null);
            m.put("newStatus", h.getNewStatus() != null ? h.getNewStatus().name() : null);
            m.put("details", h.getDetails());
            m.put("timestamp", h.getTimestamp() != null ? h.getTimestamp().format(ISO_FMT) : null);
            m.put("performer", h.getUser() != null ? h.getUser().getName() : "System");
            m.put("performerRole", h.getUser() != null ? h.getUser().getRole().name() : "SYSTEM");
            return m;
        }).collect(Collectors.toList());
        detail.put("history", historyList);

        // Comments
        User currentUser = resolveUser(auth, true);
        List<Comment> comments = commentService.getCommentsForComplaint(complaint, currentUser);
        List<Map<String, Object>> commentList = comments.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("content", c.getContent());
            m.put("isInternal", c.isInternal());
            m.put("timestamp", c.getTimestamp() != null ? c.getTimestamp().format(ISO_FMT) : null);
            m.put("authorName", c.getAuthor() != null ? c.getAuthor().getName() : "Unknown");
            m.put("authorRole", c.getAuthor() != null ? c.getAuthor().getRole().name() : "USER");
            return m;
        }).collect(Collectors.toList());
        detail.put("comments", commentList);

        // Feedback
        feedbackService.getFeedbackForComplaint(complaint).ifPresent(fb -> {
            Map<String, Object> fbMap = new LinkedHashMap<>();
            fbMap.put("id", fb.getId());
            fbMap.put("rating", fb.getRating());
            fbMap.put("comments", fb.getComments());
            fbMap.put("createdAt", fb.getCreatedAt() != null ? fb.getCreatedAt().format(ISO_FMT) : null);
            detail.put("feedback", fbMap);
        });

        return ResponseEntity.ok(detail);
    }

    @PostMapping("/complaints")
    public ResponseEntity<?> createComplaint(@RequestBody Map<String, Object> body, Authentication auth) {
        User user = resolveUser(auth, false);
        if (user != null && user.getRole() == Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Administrators monitor and manage operations; complaints must be submitted by students."));
        }
        User student = resolveStudent(auth);

        ComplaintCreateRequest req = new ComplaintCreateRequest();
        req.setTitle((String) body.get("title"));
        req.setDescription((String) body.get("description"));
        req.setFloor((String) body.getOrDefault("floor", "Ground Floor"));
        req.setRoomNumber((String) body.getOrDefault("roomNumber", "General"));
        req.setLocationDescription((String) body.getOrDefault("locationDescription", ""));

        if (body.get("categoryId") != null) {
            req.setCategoryId(Long.valueOf(body.get("categoryId").toString()));
        } else {
            req.setCategoryId(categoryRepository.findAll().get(0).getId());
        }

        if (body.get("buildingId") != null) {
            req.setBuildingId(Long.valueOf(body.get("buildingId").toString()));
        } else {
            req.setBuildingId(buildingRepository.findAll().get(0).getId());
        }

        if (body.get("priority") != null) {
            try {
                req.setPriority(PriorityLevel.valueOf(body.get("priority").toString().toUpperCase()));
            } catch (Exception ignored) {
                req.setPriority(PriorityLevel.MEDIUM);
            }
        }

        Complaint created = complaintService.createComplaint(req, student);

        if (body.get("imageData") != null && !body.get("imageData").toString().isBlank()) {
            String imagePath = fileStorageService.storeBase64Image(body.get("imageData").toString(), "complaints");
            if (imagePath != null) {
                created.setBeforeImagePath(imagePath);
                created = complaintRepository.save(created);
            }
        }

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Work order " + created.getComplaintNumber() + " submitted successfully.");
        Map<String, Object> complaintMap = formatComplaintMap(created);
        if (body.get("imageData") != null && !body.get("imageData").toString().isBlank()) {
            complaintMap.put("imageData", body.get("imageData").toString());
        }
        resp.put("complaint", complaintMap);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/complaints/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body, Authentication auth) {
        User currentUser = resolveUser(auth, true);
        String statusStr = body.get("status");
        String details = body.getOrDefault("details", "Status updated via Facilities Management Console");

        ComplaintStatus newStatus;
        try {
            newStatus = ComplaintStatus.valueOf(statusStr.toUpperCase());
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid status: " + statusStr));
        }

        Complaint updated = complaintService.updateStatus(id, newStatus, details, currentUser);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Work order status transitioned to " + newStatus.name());
        resp.put("complaint", formatComplaintMap(updated));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/complaints/{id}/assign")
    public ResponseEntity<?> assignStaff(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        User user = resolveUser(auth, false);
        if (user != null && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Only campus administrators can assign technicians."));
        }
        User adminUser = resolveAdmin(auth);
        Long staffId = Long.valueOf(body.get("staffId").toString());
        PriorityLevel priority = PriorityLevel.MEDIUM;
        if (body.get("priority") != null) {
            try {
                priority = PriorityLevel.valueOf(body.get("priority").toString().toUpperCase());
            } catch (Exception ignored) {}
        }
        String note = (String) body.getOrDefault("note", "Assigned for on-site inspection");

        Complaint updated = complaintService.assignStaff(id, staffId, priority, note, adminUser);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Assigned technician successfully.");
        resp.put("complaint", formatComplaintMap(updated));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/complaints/{id}/comments")
    public ResponseEntity<?> addComment(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        User currentUser = resolveUser(auth, true);
        String content = (String) body.get("content");
        boolean isInternal = Boolean.TRUE.equals(body.get("isInternal"));

        CommentRequest req = new CommentRequest();
        req.setContent(content);
        req.setInternal(isInternal);

        Comment saved = commentService.addComment(id, req, currentUser);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("commentId", saved.getId());
        resp.put("content", saved.getContent());
        resp.put("authorName", currentUser.getName());
        resp.put("timestamp", LocalDateTime.now().format(ISO_FMT));
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/complaints/{id}/feedback")
    public ResponseEntity<?> submitFeedback(@PathVariable Long id, @RequestBody Map<String, Object> body, Authentication auth) {
        User user = resolveUser(auth, false);
        if (user != null && user.getRole() != Role.STUDENT) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Service satisfaction rating is reserved for students."));
        }
        User student = resolveStudent(auth);
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String comments = (String) body.getOrDefault("comments", "");

        FeedbackRequest req = new FeedbackRequest();
        req.setRating(rating);
        req.setComments(comments);

        Feedback fb = feedbackService.submitFeedback(id, req, student);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("success", true);
        resp.put("message", "Thank you for rating our facilities service!");
        resp.put("feedbackId", fb.getId());
        return ResponseEntity.ok(resp);
    }

    // -------------------------------------------------------------------------
    // Categories, Buildings, Staff, and Audit
    // -------------------------------------------------------------------------
    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        List<Category> list = categoryService.getAllActiveCategories();
        List<Map<String, Object>> dtoList = list.stream().map(c -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("description", c.getDescription());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/buildings")
    public ResponseEntity<List<Map<String, Object>>> getBuildings() {
        List<Building> list = buildingService.getAllActiveBuildings();
        List<Map<String, Object>> dtoList = list.stream().map(b -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", b.getId());
            m.put("name", b.getName());
            m.put("code", b.getCode());
            m.put("location", b.getLocation());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/staff")
    public ResponseEntity<List<Map<String, Object>>> getStaffMembers() {
        List<User> staff = userService.getAllActiveStaffMembers();
        List<Map<String, Object>> dtoList = staff.stream().map(s -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", s.getId());
            m.put("name", s.getName());
            m.put("email", s.getEmail());
            m.put("department", s.getDepartment());
            m.put("phoneNumber", s.getPhoneNumber());
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/audit")
    public ResponseEntity<?> getAuditLogs(Authentication auth, @RequestParam(defaultValue = "20") int limit) {
        User user = resolveUser(auth, false);
        if (user != null && user.getRole() != Role.ADMIN) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("message", "Access denied: Audit log is restricted to campus administrators."));
        }
        Page<AuditLog> page = auditService.getAuditLogs(PageRequest.of(0, limit));
        List<Map<String, Object>> dtoList = page.getContent().stream().map(a -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", a.getId());
            m.put("action", a.getAction());
            m.put("entityName", a.getEntityName());
            m.put("entityId", a.getEntityId());
            m.put("details", a.getDetails());
            m.put("timestamp", a.getTimestamp() != null ? a.getTimestamp().format(ISO_FMT) : null);
            m.put("userName", a.getUser() != null ? a.getUser().getName() : "System");
            m.put("userRole", a.getUser() != null ? a.getUser().getRole().name() : "SYSTEM");
            return m;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    // -------------------------------------------------------------------------
    // Helper Formatters & Resolvers
    // -------------------------------------------------------------------------
    private Map<String, Object> formatUserMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", u.getId());
        m.put("name", u.getName());
        m.put("email", u.getEmail());
        m.put("role", u.getRole().name());
        m.put("department", u.getDepartment() != null ? u.getDepartment() : "");
        m.put("registerNumber", u.getRegisterNumber() != null ? u.getRegisterNumber() : "");
        m.put("phoneNumber", u.getPhoneNumber() != null ? u.getPhoneNumber() : "");
        return m;
    }

    private Map<String, Object> formatComplaintMap(Complaint c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("complaintNumber", c.getComplaintNumber());
        m.put("title", c.getTitle());
        m.put("description", c.getDescription());
        m.put("floor", c.getFloor());
        m.put("roomNumber", c.getRoomNumber());
        m.put("locationDescription", c.getLocationDescription());
        m.put("priority", c.getPriority().name());
        m.put("status", c.getStatus().name());
        m.put("createdAt", c.getCreatedAt() != null ? c.getCreatedAt().format(ISO_FMT) : null);
        m.put("updatedAt", c.getUpdatedAt() != null ? c.getUpdatedAt().format(ISO_FMT) : null);
        m.put("resolvedAt", c.getResolvedAt() != null ? c.getResolvedAt().format(ISO_FMT) : null);
        m.put("beforeImagePath", c.getBeforeImagePath());
        m.put("afterImagePath", c.getAfterImagePath());

        if (c.getCategory() != null) {
            m.put("category", Map.of("id", c.getCategory().getId(), "name", c.getCategory().getName()));
        }
        if (c.getBuilding() != null) {
            m.put("building", Map.of(
                    "id", c.getBuilding().getId(),
                    "name", c.getBuilding().getName(),
                    "code", c.getBuilding().getCode() != null ? c.getBuilding().getCode() : ""
            ));
        }
        if (c.getStudent() != null) {
            m.put("student", Map.of(
                    "id", c.getStudent().getId(),
                    "name", c.getStudent().getName(),
                    "email", c.getStudent().getEmail(),
                    "department", c.getStudent().getDepartment() != null ? c.getStudent().getDepartment() : ""
            ));
        }
        if (c.getAssignedStaff() != null) {
            m.put("assignedStaff", Map.of(
                    "id", c.getAssignedStaff().getId(),
                    "name", c.getAssignedStaff().getName(),
                    "email", c.getAssignedStaff().getEmail(),
                    "department", c.getAssignedStaff().getDepartment() != null ? c.getAssignedStaff().getDepartment() : ""
            ));
        } else {
            m.put("assignedStaff", null);
        }

        feedbackRepository.findByComplaint(c).ifPresent(fb -> {
            m.put("rating", fb.getRating());
        });

        return m;
    }

    private User resolveUser(Authentication auth, boolean fallbackToAdmin) {
        if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof CustomUserDetails) {
            return ((CustomUserDetails) auth.getPrincipal()).getUser();
        }
        if (fallbackToAdmin) {
            return userRepository.findByEmail("admin@smartcampus.com").orElse(null);
        }
        return null;
    }

    private User resolveAdmin(Authentication auth) {
        User u = resolveUser(auth, false);
        if (u != null && u.getRole() == Role.ADMIN) return u;
        return userRepository.findByEmail("admin@smartcampus.com").orElse(null);
    }

    private User resolveStudent(Authentication auth) {
        User u = resolveUser(auth, false);
        if (u != null && u.getRole() == Role.STUDENT) return u;
        return userRepository.findByEmail("student@smartcampus.com").orElse(null);
    }
}
