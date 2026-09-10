# SMART CAMPUS COMPLAINT & MAINTENANCE MANAGEMENT SYSTEM

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-blue.svg)](https://spring.io/projects/spring-security)
[![Bootstrap](https://img.shields.io/badge/Bootstrap-5.3-purple.svg)](https://getbootstrap.com/)

A full-stack, enterprise-grade Java web application designed as a comprehensive academic/production project for managing campus infrastructure problems, maintenance requests, technician assignment, SLA tracking, audit logging, and student feedback.

---

## 1. PROJECT OVERVIEW

The **Smart Campus Complaint & Maintenance Management System** provides a centralized digital portal for students, faculty, administrators, and maintenance staff.

- **Students** can submit infrastructure issues (e.g., broken projectors, Wi-Fi outages, AC leaks, electrical faults) with location details and photos, track progress in real time, confirm resolution, and leave star ratings.
- **Administrators** have an executive dashboard with Chart.js analytics, SLA overdue tracking, automated staff assignment, priority controls, student/staff user management, and CSV report exports.
- **Maintenance Staff** have a dedicated technician workbench to accept assigned tasks, update progress notes, upload repair photos, and mark issues resolved.

---

## 2. TECHNOLOGY STACK

### Backend
- **Java 21** LTS
- **Spring Boot 3.3.4**
- **Spring MVC** (Layered Architecture)
- **Spring Security 6** (Form authentication, Role-Based Access Control, BCrypt password hashing)
- **Spring Data JPA & Hibernate** (ORM, custom repository queries, specifications)
- **Jakarta Validation** (`@NotBlank`, `@Email`, `@Size`, etc.)

### Frontend
- **Thymeleaf** (Server-side Java templating)
- **HTML5 & CSS3**
- **Bootstrap 5.3** & **FontAwesome 6**
- **Chart.js** (Interactive dashboard graphs)
- **Vanilla JavaScript** (Dynamic filters, preview, alerts)

### Database
- **MySQL 8** (Production DBMS: `smart_campus_db`)
- **H2 Database** (In-memory fallback with MySQL compatibility mode for instant out-of-the-box demo execution)

---

## 3. APPLICATION ARCHITECTURE

```text
com.smartcampus.maintenance

├── config                      # Security, Web MVC & Database Initializer
│   ├── DataInitializer.java
│   ├── SecurityConfig.java
│   └── WebConfig.java
├── controller                  # Spring MVC Web Controllers
│   ├── AdminBuildingController.java
│   ├── AdminCategoryController.java
│   ├── AdminComplaintController.java
│   ├── AdminDashboardController.java
│   ├── AdminReportController.java
│   ├── AdminUserController.java
│   ├── HomeController.java
│   ├── ProfileController.java
│   ├── StaffComplaintController.java
│   ├── StaffDashboardController.java
│   ├── StudentComplaintController.java
│   └── StudentDashboardController.java
├── dto                         # Data Transfer Objects
├── entity                      # JPA Database Entities
│   ├── Building.java
│   ├── Category.java
│   ├── Comment.java
│   ├── Complaint.java
│   ├── ComplaintHistory.java
│   ├── Feedback.java
│   ├── Notification.java
│   └── User.java
├── enums                       # Core Domain Enums
│   ├── ComplaintStatus.java    # OPEN, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED, etc.
│   ├── PriorityLevel.java      # LOW (48h), MEDIUM (24h), HIGH (12h), CRITICAL (4h)
│   └── Role.java               # STUDENT, ADMIN, MAINTENANCE_STAFF
├── exception                   # Global Exception Handlers
├── repository                  # Spring Data JPA Repositories
├── security                    # Custom UserDetails & Auth Handlers
├── service                     # Business Logic Interfaces & Implementations
│   └── impl/
└── util                        # Complaint ID Generator & CSV Export Utilities
```

---

## 4. DEFAULT DEVELOPMENT ACCOUNTS

| Role | Name | Email | Password |
| :--- | :--- | :--- | :--- |
| **ADMIN** | Chief Administrator | `admin@smartcampus.com` | `Admin@123` |
| **STUDENT** | Alex Morgan | `student@smartcampus.com` | `Student@123` |
| **STUDENT** | Priya Sharma | `student2@smartcampus.com` | `Student@123` |
| **MAINTENANCE STAFF** | Ravi Kumar (Electrical) | `staff@smartcampus.com` | `Staff@123` |
| **MAINTENANCE STAFF** | Anil Verma (IT & AV) | `staff3@smartcampus.com` | `Staff@123` |

---

## 5. INSTALLATION & RUN INSTRUCTIONS

### Prerequisites
- JDK 21+ or OpenJDK 25
- Apache Maven 3.9+
- MySQL 8 (Optional for local MySQL server; H2 in-memory runs out-of-the-box)

### Step 1: Clone Repository
```bash
git clone https://github.com/example/smart-campus-maintenance.git
cd smart-campus-maintenance
```

### Step 2 (Optional): MySQL Database Setup
If using a local MySQL server:
```sql
CREATE DATABASE smart_campus_db;
```
Then update `src/main/resources/application.properties` or run:
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### Step 3: Build & Run Application
```bash
mvn clean package
mvn spring-boot:run
```

Access the application in your browser:
**`http://localhost:8080`**

---

## 6. COMPLETE COMPLAINT WORKFLOW

```text
                      SMART CAMPUS SYSTEM
                               │
            ┌──────────────────┼──────────────────┐
            │                  │                  │
            ▼                  ▼                  ▼
         STUDENT             ADMIN              STAFF
            │                  │                  │
            ▼                  ▼                  ▼
     Submit Complaint      Review Issue      Assigned Tasks
   (Status: OPEN)              │                  │
            │                  ▼                  │
            └──────────────► Assign ──────────────┘
                             Technician
                          (Status: ASSIGNED)
                                 │
                                 ▼
                             Start Work
                         (Status: IN_PROGRESS)
                                 │
                                 ▼
                           Complete Repair
                         (Status: RESOLVED)
                                 │
            ┌────────────────────┘
            ▼
   Student Verification
    & Feedback Rating
   (Status: CLOSED)
```

---

## 7. KEY FEATURES

- **Automatic Complaint Number Generation**: `CMP-2026-00001`
- **SLA & Overdue Alert System**: `CRITICAL` (4h), `HIGH` (12h), `MEDIUM` (24h), `LOW` (48h)
- **Role-Based Security**: Secured `/student/**`, `/admin/**`, `/staff/**` routes
- **File Uploads**: Photo attachment for issue submission and repair completion proof
- **Audit Logging**: Full history of user actions and status updates
- **CSV Reporting**: Admin CSV export for campus maintenance audits
- **Interactive Charts**: Category, Status, Priority, and Building distribution charts via Chart.js

---

## 8. FUTURE ENHANCEMENTS

- AI Automated Complaint Categorization & Priority Suggestion
- Email & SMS Gateway Alerts
- QR Code Scanning for Building & Room Location Selection
