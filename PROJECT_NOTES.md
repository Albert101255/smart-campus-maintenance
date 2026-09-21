# Project Recall — Smart Campus Maintenance

## One-line explanation
Spring Boot system for student complaint reporting, administrator assignment, technician updates, SLA tracking and feedback.

## Core workflow
Student -> complaint -> admin review -> technician assignment -> work in progress -> resolved -> student confirmation/feedback.

## Main technical pieces
- Spring MVC controllers
- Service layer
- Spring Data JPA repositories
- Spring Security role separation
- Thymeleaf frontend
- MySQL/H2
- Complaint history/audit records
- SLA timing by priority

## Interview questions
1. Why use controller/service/repository layers?
2. How is role-based access enforced?
3. How would you prevent unauthorized status changes?
4. How do complaint history and current complaint state differ?
5. How would you make SLA alerts reliable in production?

## Next improvements
- Automated service/controller tests
- CI
- Notification service
- Screenshots/demo
