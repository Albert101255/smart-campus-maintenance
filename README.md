# Smart Campus Facilities & Maintenance Management System

> **Live Interactive Preview:** [https://albert101255.github.io/smart-campus-maintenance/](https://albert101255.github.io/smart-campus-maintenance/)

An enterprise-grade facilities maintenance and incident management platform built with **Spring Boot 3**, **Spring Security**, **Spring Data JPA / Hibernate**, and a responsive modern dashboard with dark/light themes, real-time KPI metrics, printable dispatch slips, visual defect photo evidence, and strict **Role-Based Access Control (RBAC)**.

---

## 🏛️ System Overview & Architecture

Campus maintenance operations frequently suffer from lost reports, untracked turnaround times, and lack of accountability. This system provides a centralized institutional lifecycle for logging, prioritizing, assigning, resolving, and rating facility issues across campus buildings.

```
       [ Student ]                [ Administrator ]              [ Technician ]
  (Lodge / Track Issues)       (Dispatch / Audit / SLA)      (Repair / Update State)
            |                             |                             |
            +-----------------------------+-----------------------------+
                                          |
                              [ REST API / Thymeleaf ]
                               (Spring Security RBAC)
                                          |
                              [ Service Business Logic ]
                                (SLA / Audit / Events)
                                          |
                             [ JPA Entities & Repos ]
                                          |
                         [ Relational Database (H2 / MySQL) ]
```

---

## 🔑 Dedicated Role Credentials & Access Controls

The portal enforces strict role boundaries across both backend API endpoints (`403 Forbidden` checks) and dynamic frontend navigation:

| Role | Default Credentials | Permitted Capabilities | Strict Restrictions |
| :--- | :--- | :--- | :--- |
| **Student** | `student@smartcampus.com`<br>`Student@123` | • Lodge new maintenance work orders with optional defect photo<br>• View own complaints & add student comments<br>• Star satisfaction rating (1–5 ★) upon resolution | ❌ **No Audit Log**<br>❌ **No Dispatch/Assign Staff**<br>❌ **No Global Work Orders**<br>❌ **No Buildings Directory** |
| **Administrator** | `admin@smartcampus.com`<br>`Admin@123` | • Executive KPI dashboard & SLA monitoring<br>• Global work order tracking & filtering<br>• Dispatch technicians & configure priority<br>• Campus buildings & facilities directory<br>• System audit log inspection & CSV export | ❌ **No Raise/Submit Complaint**<br>*(Admins oversee & monitor operations)* |
| **Technician / Staff** | `staff@smartcampus.com`<br>`Staff@123` | • Assigned work order queue with defect photo evidence<br>• State transition (`IN_PROGRESS`, `ON_HOLD`, `RESOLVED`)<br>• Printable field technician dispatch slip with photo<br>• Add diagnostic notes | ❌ **No Submit Complaint**<br>❌ **No Audit Log**<br>❌ **No Reassigning Technicians**<br>❌ **No Buildings Directory** |

---

## 🔄 Core Work Order Lifecycle & SLA Model

```text
Student Submits Request (OPEN)
         |
         v
Admin Reviews & Assigns Staff (ASSIGNED)
         |
         v
Technician Begins Field Inspection (IN_PROGRESS)
         |
    +----+---------------------------+
    |                                |
    v (Awaiting Parts)               v
(ON_HOLD)                   Technician Completes Repair (RESOLVED)
    |                                |
    +---> (IN_PROGRESS)              v
                            Student Rates & Closes Ticket (CLOSED)
```

### SLA Resolution Targets
- **CRITICAL:** 4 Hours (Campus emergency / safety hazards)
- **HIGH:** 12 Hours (Academic & lecture hall interruptions)
- **MEDIUM:** 24 Hours (General equipment defects)
- **LOW:** 48 Hours (Cosmetic / minor maintenance)

---

## 🚀 REST API Endpoints

All endpoints are guarded by session authentication and role validation:

| Method | Endpoint | Access Role | Description |
| :--- | :--- | :--- | :--- |
| `GET` | `/api/health` | Public | System status, database health, complaint count |
| `POST` | `/api/auth/login` | Public | Session authentication via email & password |
| `GET` | `/api/auth/me` | Authenticated | Current authenticated user profile & permissions |
| `POST` | `/api/auth/logout` | Authenticated | Invalidates user session |
| `GET` | `/api/dashboard/stats` | Authenticated | Aggregated metrics, resolution rate, SLA breaches |
| `GET` | `/api/complaints` | Role-Filtered | Students see own; Staff see assigned; Admin sees all |
| `POST` | `/api/complaints` | `STUDENT` | Lodge new maintenance complaint (403 for Admin) |
| `GET` | `/api/complaints/{id}` | Authenticated | Detailed ticket view with timeline and comment logs |
| `POST` | `/api/complaints/{id}/assign` | `ADMIN` | Dispatch technician & set priority (403 for others) |
| `POST` | `/api/complaints/{id}/status` | `STAFF`, `ADMIN`| Transition repair status |
| `POST` | `/api/complaints/{id}/comments`| Authenticated | Add diagnostic or follow-up note |
| `POST` | `/api/complaints/{id}/feedback`| `STUDENT` | Submit 1–5 star rating on resolved tickets |
| `GET` | `/api/audit` | `ADMIN` | System-wide immutable audit trail (403 for others) |
| `GET` | `/api/categories` | Authenticated | Active facility categories list |
| `GET` | `/api/buildings` | Authenticated | Campus buildings and facility blocks |
| `GET` | `/api/staff` | Authenticated | Maintenance trade technicians directory |

---

## 🛠️ Technology Stack & Capabilities

- **Backend:** Java 21 / 27, Spring Boot 3.5.x, Spring MVC, Spring Security, Spring Data JPA, Hibernate
- **Database:** H2 In-Memory DB (MySQL compatibility mode for zero-setup demo) / MySQL 8 for production
- **Frontend:** HTML5, CSS3 Variables, Bootstrap 5.3, FontAwesome 6, native client-side CSV generator, print styles
- **Design System:** Dark / Light theme toggle (`Ctrl + D`), responsive mobile drawer navigation, quick status tabs
- **Defect Photo Evidence:** Pre-inspection photo attachment support (locked to mobile camera formats: JPG, PNG, WebP, HEIC) delivered as Base64 Data URI for modal and print slip inspection
- **Session Continuity:** Auto-preserves authenticated user context across page reloads without session drops

---

## 💻 Running Locally

### Prerequisites
- Java JDK 21+
- Maven 3.9+

### Build and Launch
```bash
# Clone the repository
git clone https://github.com/Albert101255/smart-campus-maintenance.git
cd smart-campus-maintenance

# Package the application
mvn clean package -DskipTests

# Run the Spring Boot JAR
java -jar target/smart-campus-maintenance-1.0.0-SNAPSHOT.jar
```

Once running, navigate to:
```
http://localhost:8080/
```

### Sign In
Use any of the institutional accounts:
- **Student:** `student@smartcampus.com` / `Student@123`
- **Admin:** `admin@smartcampus.com` / `Admin@123`
- **Technician:** `staff@smartcampus.com` / `Staff@123`
