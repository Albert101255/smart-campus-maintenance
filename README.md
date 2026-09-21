# Smart Campus Complaint & Maintenance Management System

A Java/Spring Boot application for reporting campus infrastructure problems, assigning maintenance staff, tracking complaint status and SLA deadlines, recording audit history, and collecting student feedback.

## Why I built it

Campus maintenance requests are often scattered across informal channels. This project models a single workflow where students report issues, administrators assign and monitor them, and maintenance staff update the repair process.

## Core workflow

```text
Student submits complaint
        |
        v
      OPEN
        |
        v
Admin reviews + assigns technician
        |
        v
    ASSIGNED
        |
        v
Staff starts work
        |
        v
   IN_PROGRESS
        |
        v
Staff resolves issue
        |
        v
    RESOLVED
        |
        v
Student verifies + leaves feedback
        |
        v
      CLOSED
```

## Features

### Student
- Submit complaints with location and photo information
- Track complaint status
- Review resolution
- Leave feedback/rating

### Administrator
- Review and prioritize complaints
- Assign maintenance staff
- Track SLA/overdue complaints
- Manage users, buildings and categories
- Export CSV reports
- View dashboard analytics

### Maintenance staff
- View assigned tasks
- Start and update work
- Add progress notes and completion evidence
- Mark issues resolved

## Technology

- Java 21
- Spring Boot 3.3.4
- Spring MVC
- Spring Security
- Spring Data JPA / Hibernate
- Thymeleaf
- Bootstrap 5
- Chart.js
- MySQL 8
- H2 for local/demo execution

## Architecture

The application uses a layered Spring structure:

```text
Controller
   |
Service
   |
Repository
   |
Database
```

Domain objects include users, complaints, complaint history, buildings, categories, comments, notifications and feedback.

## Run locally

### Requirements

- JDK 21+
- Maven 3.9+
- MySQL 8 optional; H2 can be used for local/demo execution

```bash
git clone https://github.com/Albert101255/smart-campus-maintenance.git
cd smart-campus-maintenance

mvn clean package
mvn spring-boot:run
```

Then open:

```text
http://localhost:8080
```

## Development data

The project includes local development accounts/data to make the workflow easier to demonstrate. These credentials are for local development only and should never be reused on a public deployment.

## SLA model

Priority levels map to target resolution windows:

- CRITICAL — 4 hours
- HIGH — 12 hours
- MEDIUM — 24 hours
- LOW — 48 hours

The application uses these values to identify overdue work.

## Project status

This is an academic/portfolio project, not a claim of production deployment. It is intended to demonstrate Java full-stack development, Spring Security, role-based workflows, relational data modelling and maintenance-process automation.

## Next improvements

- Automated tests for service and controller layers
- Screenshots / short demo video
- CI workflow
- Email/SMS notifications
- QR-based building/room selection
- AI-assisted complaint categorization as an optional future feature
