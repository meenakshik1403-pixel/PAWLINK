# PawLink Pet Care Management System - Comprehensive Test Matrix (QA Lead: P8)

This document specifies the end-to-end Quality Assurance test suite and validation criteria covering all platform REST API endpoints, domain models, and dynamic polymorphic business logic.

---

## 1. Test Execution Summary

- **Total Documented Test Cases:** 20 Test Scenarios
- **Runtime Environment:** Java 25 (JDK 25 LTS) with `--release 21` compilation target.
- **Automated Test Suite:** **75 passing unit and integration tests** across all services, repositories, and REST controllers.
- **Modules Covered:** Authentication, Pets, Medical & Vaccines, Appointments, Reminders, Sitter Bookings, Check-Ins, Payments & Escrow, Admin Approvals & System Analytics.
- **Architectural Scope:** Spring Boot 3, Spring `JdbcTemplate` (pure SQL, no ORM), BCrypt Password Security, Custom Exception Mappings (400, 401, 404, 409).

---

## 2. API & Domain Test Matrix

| TC ID | Vertical / Domain | Endpoint / Component | HTTP Method | Sample Request Payload / Parameters | Expected Status Code | Validation Criteria |
| :--- | :--- | :--- | :--- | :--- | :--- | :--- |
| **TC-01** | User Auth | `/api/auth/register` | `POST` | `{"name":"John Vargese","email":"owner.john@pawlink.com","password":"password123","phone":"+919876543301","role":"OWNER"}` | `201 Created` | Returns `{ id, name, email, role: "OWNER" }`. User persisted with BCrypt password hash. |
| **TC-01b** | User Auth | `/api/auth/register` | `POST` | `{"name":"Attempted Admin","email":"admin.hack@pawlink.com","password":"password123","phone":"555-0301","role":"ADMIN"}` | `400 Bad Request` | Admin registration forbidden. Admin credentials must be configured via `.env` or `DatabaseSeeder`. |
| **TC-02** | User Auth | `/api/auth/login` | `POST` | `{"email":"owner.john@pawlink.com","password":"password123"}` | `200 OK` | Returns User DTO. Validates BCrypt hash comparison; returns 401 Unauthorized on bad credentials. |
| **TC-03** | Pet Management | `/api/pets` | `POST` | `{"ownerId":1,"name":"Bruno","species":"Dog","dob":"2021-03-15"}` | `201 Created` | Returns pet with polymorphic `careGuidelines: "Daily exercise, annual rabies vaccine, dental checkups."` |
| **TC-04** | Pet Management | `/api/pets` | `GET` | Query Param: `?ownerId=1` | `200 OK` | Returns JSON Array of pets owned by ID 1. Validates ownership isolation. |
| **TC-05** | Medical Records | `/api/medical/records` | `POST` | `{"petId":1,"vetId":2,"recordDate":"2026-10-20","diagnosis":"Ear Infection","prescription":"Antibiotic drops"}` | `201 Created` | Persists medical consultation visit in `medical_records` table linked to treating doctor. |
| **TC-06** | Vaccination | `/api/medical/vaccinations` | `POST` | `{"petId":1,"vaccineName":"Rabies Annual","givenDate":"2026-10-20","nextDueDate":"2027-10-20"}` | `201 Created` | Automatically schedules a future `VACCINATION` reminder in `reminders` table due on `nextDueDate`. |
| **TC-07** | Appointments | `/api/appointments` | `POST` | `{"vetId":2,"petId":1,"ownerId":1,"startTime":"2026-11-01T10:00:00","endTime":"2026-11-01T11:00:00"}` | `201 Created` | Successfully schedules slot with status `PENDING` when no time conflict exists. |
| **TC-07b** | Appointments | `/api/appointments` | `POST` | `{"vetId":2,"petId":1,"ownerId":1,"startTime":"2020-01-01T10:00:00","endTime":"2020-01-01T11:00:00"}` | `400 Bad Request` | Client & server validation: blocks past appointment dates/times on today or earlier dates. |
| **TC-08** | Appointments | `/api/appointments` | `POST` | `{"vetId":2,"petId":2,"ownerId":1,"startTime":"2026-11-01T10:30:00","endTime":"2026-11-01T11:30:00"}` | `409 Conflict` | Conflict detection via `Schedulable`: detects interval overlap ($10:30 < 11:00 \land 11:30 > 10:00$) and returns `SlotUnavailableException`. |
| **TC-09** | Appointments | `/api/appointments/{id}/status` | `PATCH` | `{"status":"CONFIRMED"}` | `200 OK` | Transitions appointment status to `CONFIRMED`. |
| **TC-10** | Reminders | `/api/reminders/run` | `POST` | None | `200 OK` | Observer engine sweeps due reminders and dispatches simulated alerts via `EmailAdapter` and `SmsAdapter`. |
| **TC-11** | Admin Approval | `/api/admin/sitters/{id}/approve` | `PATCH` | None | `204 No Content` | Sets `verified = TRUE` in `sitters` table for sitter ID, unlocking booking eligibility. |
| **TC-12** | Sitter Booking | `/api/sitter-bookings` | `POST` | `{"petId":1,"sitterId":3,"ownerId":1,"startTime":"2026-11-05T09:00:00","endTime":"2026-11-05T13:00:00","paymentMethod":"UPI"}` | `201 Created` | Enforces Domain Invariant: verified sitter booked, fee calculated ($4 \text{ hrs} \times ₹250 = ₹1,000$), and payment placed in escrow (`HELD`). |
| **TC-12b** | Sitter Booking | `/api/sitter-bookings` | `POST` | `{"petId":1,"sitterId":3,"ownerId":1,"startTime":"2020-01-01T09:00:00","endTime":"2020-01-01T11:00:00","paymentMethod":"UPI"}` | `400 Bad Request` | Validation: sitting start time cannot be in the past; end time must strictly succeed start time by at least 30 minutes. |
| **TC-13** | Sitter Booking | `/api/sitter-bookings` | `POST` | `{"petId":1,"sitterId":4,"ownerId":1,"startTime":"2026-11-05T09:00:00","endTime":"2026-11-05T13:00:00","paymentMethod":"CARD"}` | `400 Bad Request` | Fails with `UnverifiedSitterException` when attempting to book unverified sitter (sitter ID 4). |
| **TC-14** | Care Check-In | `/api/sitter-bookings/{id}/checkins` | `POST` | `{"note":"Fed Bruno lunch and completed 30-min walk."}` | `201 Created` | Logs timestamped check-in note visible to pet parent in real-time timeline. |
| **TC-15** | System Stats | `/api/admin/stats` | `GET` | None | `200 OK` | Returns `SystemStatsDTO` aggregating users by role, appointments per vet, and bookings per sitter via SQL `GROUP BY`. |
| **TC-16** | Early Session Finish | `/api/sitter-bookings/{id}/complete` | `POST` | Session completed prior to scheduled end time | `200 OK` | Updates status to `COMPLETED`, transitions escrow to `RELEASED`, and truncates `end_time` to `now`, freeing remaining slot for other bookings. |
| **TC-17** | Dynamic Polymorphism | `Payment.process()` | Java Unit Test | `CardPayment`, `UpiPayment`, `WalletPayment` | Pass | Verifies runtime dynamic polymorphism across all payment subclasses with polymorphic dispatch. |
| **TC-18** | Polymorphic Pet Model | `Pet.getCareGuidelines()` | Java Unit Test | `Dog`, `Cat`, `Bird`, `Reptile` | Pass | Demonstrates class inheritance and dynamic method overriding returning distinct care instructions. |

---

## 3. Automated Test Suite Execution

To execute the entire 75-test automated test suite across all controllers, services, and DAOs:

```powershell
mvn clean test -f backend/pom.xml
```

**Expected Result:**
```
[INFO] Results:
[INFO] Tests run: 75, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```
