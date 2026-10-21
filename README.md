# PawLink — Pet Care Management Platform

PawLink is a comprehensive, enterprise-grade Pet Care Management Platform designed to streamline interactions between **Pet Owners**, **Veterinarians**, **Pet Sitters**, and **Platform Administrators**.

Built using **Java 25 (JDK 25 LTS)**, **Spring Boot 3**, and a modern **Java Swing Desktop Client (FlatLaf)**, PawLink features pure SQL database interactions via **Spring `JdbcTemplate`** (**strictly zero JPA / Hibernate**), role-based workflows, secure credential verification, and complete financial transactions in Indian Rupees (`₹` INR).

---

## 1. System Architecture

PawLink is structured as a modular Maven multi-project repository:

```
PAWLINK/
├── pom.xml                                   # Root Maven parent reactor (JDK 25 LTS runtime, release 21 bytecode target)
├── .devcontainer/                            # Isolated Linux DevContainer environment (JDK 25 LTS, noVNC Swing GUI)
│   ├── devcontainer.json                     # Container definition & LAN port forwarding (8080 API, 6080 noVNC)
│   ├── Dockerfile                            # Debian Bookworm container with Java 25 & X11 desktop-lite
│   ├── docker-compose.yml                    # Persistent Maven dependency volume & service config
│   ├── setup.sh                              # Automatic build, compile & pre-caching entrypoint
│   └── README.md                             # DevContainer documentation & LAN access guide
├── .env.example                              # Template for environment configuration & admin credentials
├── scripts/                                  # Cross-platform execution scripts
│   ├── run-backend.bat                       # Launch Spring Boot REST API (Windows)
│   ├── run-backend.sh                        # Launch Spring Boot REST API (Linux / macOS)
│   ├── run-swing-client.bat                  # Launch Swing GUI silently via javaw (Windows)
│   ├── run-swing-debug.bat                   # Launch Swing GUI with console debug logging (Windows)
│   ├── run-swing.sh                          # Launch Swing GUI (Linux / macOS / noVNC)
│   └── supabase-cli-helper.sh                # Supabase database synchronization utility
├── db/
│   ├── schema.sql                            # Complete PostgreSQL DDL schema definition
│   └── seed.sql                              # Seed records with BCrypt passwords & test data
├── backend/                                  # Spring Boot 3 REST API (Port 8080)
│   ├── pom.xml
│   ├── uploads/                              # Secure server storage for verification PDFs (git-ignored)
│   ├── src/main/resources/
│   │   ├── application.properties            # Active Spring configuration
│   │   ├── schema.sql                        # Fallback / automated test schema
│   │   └── seed.sql                          # Database bootstrap data
│   └── src/main/java/com/pawlink/
│       ├── PawLinkApplication.java           # Backend server entrypoint
│       ├── admin/                            # Administrator controller, DAO, service & analytics
│       ├── appointment/                      # Clinic appointment scheduling & management
│       ├── common/                           # DBConnectionManager, global exception handling
│       ├── config/                           # DatabaseSeeder, BCrypt security beans
│       ├── medical/                          # Medical history records & vaccination tracking
│       ├── payment/                          # Payment processing, wallet transactions & audit log
│       ├── pet/                              # Pet profiles & breed care guidelines
│       ├── reminder/                         # Preventive health reminders & scheduler
│       ├── sitter/                           # Pet sitter directory, bookings & live check-ins
│       └── user/                             # Authentication, role models, document uploads
└── swing-vet/                                # Unified Java Swing Desktop Application
    ├── pom.xml
    └── src/main/java/com/pawlink/vet/
        ├── VetApp.java                       # Swing client entrypoint
        ├── api/                              # HTTP client, session state & role headers
        ├── base/                             # PawDateTimePicker, PawDatePicker, PawComponents, PawIcons, PawTheme
        ├── model/                            # Data Transfer Objects (DTOs)
        └── view/                             # Role-based panels, dialogs & navigation shell
```

---

## 2. Core Verticals & Role Capabilities

PawLink delivers a cohesive experience tailored to each participant:

### 🐶 Pet Owners
* **Pet Registry & Profiles:** Register and manage multiple pets (dogs, cats, birds, etc.) with age, breed, gender, and weight metrics. Guaranteed duplicate-free list views.
* **Breed-Specific Care:** Instant breed care recommendations and preventive guidelines.
* **Veterinary Appointments:** Browse available licensed veterinarians, schedule clinic consultations with dynamic slot filtering (automatically omitting passed time slots for today), and review upcoming/past appointments.
* **Verified Pet Sitter Directory:** Discover vetted sitters filtered strictly by verified status and view approved hourly rates in `₹/hr`.
* **Tactile Date & Time Selector (`PawDateTimePicker`):** Modern compound date/time picker combining an Android Material calendar modal with 30-minute time intervals, past-time prevention, and one-click duration shortcuts (`+1h`, `+2h`, `+3h`, `+4h`, `+8h`, `Overnight`).
* **Booking & Session Tracking:** Book sitter sessions with automatic escrow rate estimation, inspect live check-in updates and notes from sitters, cancel pending requests, or end active sessions.
* **Automated Payment Settlement:** Ending a sitting session automatically releases payment from the owner's balance to the sitter's wallet with escrow protection.
* **Transaction History:** Real-time billing audit log showing payment methods (Wallet, UPI, Card), reference IDs, and amounts in `₹` INR.

### 🩺 Veterinarians
* **Clinic Appointments:** View upcoming consultations, accept/manage patient visits, and review owner contact details.
* **Medical & Vaccination Timeline:** Maintain medical history records, diagnostic summaries, and vaccination timelines per pet.
* **Clinical Reminders:** Issue preventive health reminders (deworming, vaccination booster dates, annual check-ups) to pet owners with deduplication safeguards.

### 🐾 Pet Sitters
* **Job Dashboard:** View assigned sitting bookings across all lifecycle states (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
* **Live Check-In Feed:** Submit live timestamped check-in notes and activity updates for owners to review in real time. Actions are dynamically disabled and finalized once booking is marked `COMPLETED`.
* **Profile & Earnings:** Manage profile details and track earned income through approved hourly rates (`₹/hr`).

### 🛡️ Platform Administrators
* **Sitter Approval Workflow:** Dedicated tab to review pending sitter registrations, inspect uploaded government ID proofs and qualification certificates (PDF), and confirm or adjust their approved hourly rate (`₹/hr`) prior to activation.
* **Veterinarian Verification:** Dedicated tab to inspect submitted medical qualifications, license certificates, and identity documents before granting clinical access.
* **Aggregated System Analytics:** Live metrics dashboard calculating total users by role, clinic consultations per veterinarian, bookings per sitter, and overall platform volume.

### 🎨 Visual & UI Polish
* **Color-Coded Header Strips:** Consistent branded header accent bar across every view and modal dialog for all four roles.
* **Pure Vector Icons:** Java2D antialiased vector iconography with crisp rendering at any display DPI.

---

## 3. Security, Compliance & Data Policies

1. **Role Registration Controls:**
   * Public registration allows only `Pet Owner`, `Pet Sitter`, and `Veterinarian` roles.
   * `ADMIN` role creation via the public API or GUI is strictly prohibited. Admin credentials are provisioned exclusively through environment variables (`.env`) and seeded at startup.
2. **Document Upload & Server Storage Security:**
   * Sitters must upload: **Government ID Proof** and **Experience / Qualification Certificate** (PDF up to 10MB each).
   * Vets must upload: **Government ID Proof** and **Medical Qualification Certificate** (PDF up to 10MB each).
   * Files are stored in a dedicated, secure server directory (`backend/uploads/`) and **strictly excluded from Git tracking** via `.gitignore`.
   * Document viewing and downloads (`/api/admin/documents/*`) are protected by server-side authorization checks requiring administrator privileges.
3. **Account Approval Gate:**
   * Newly registered Sitters and Vets are marked `verified = FALSE` and cannot log in until approved by an administrator. Login attempts display a clear message indicating pending review.
4. **Phone Number Standards:**
   * Strictly enforces Indian telephone numbering: `+91` country code prefix followed by exactly 10 digits (`+91XXXXXXXXXX`).
5. **Currency Uniformity:**
   * All monetary figures across backend entities, DTOs, invoices, tables, and dialogs use Indian Rupee (`₹` INR).
6. **Pure JDBC Architecture:**
   * 100% of database interactions are written in plain, optimized SQL using Spring's `JdbcTemplate` with parameterized queries and explicit `RowMapper<T>` implementations.
   * **Strictly NO JPA / Hibernate ORM** to ensure absolute predictability and optimal performance.
7. **Credential Protection:**
   * Passwords are encrypted using Spring Security's `BCryptPasswordEncoder` with salt rounds before database persistence.

---

## 4. REST API Overview

| Method | Endpoint | Description | Access |
|---|---|---|---|
| `POST` | `/api/auth/register` | Register new Owner, Sitter, or Vet (supports multipart PDFs) | Public |
| `POST` | `/api/auth/login` | Authenticate user; blocks unverified Sitters/Vets | Public |
| `GET` | `/api/auth/me` | Retrieve profile of the currently authenticated user | Authenticated |
| `GET` | `/api/pets` | List pets owned by the requesting owner | Owner |
| `POST` | `/api/pets` | Register a new pet profile | Owner |
| `GET` | `/api/appointments` | List appointments for user (filtered by owner or vet) | Owner / Vet |
| `POST` | `/api/appointments` | Book a consultation appointment with a veterinarian | Owner |
| `GET` | `/api/sitters` | Directory of verified pet sitters with hourly rates | Owner / Admin |
| `GET` | `/api/sitter-bookings` | List sitter bookings and jobs | Owner / Sitter |
| `POST` | `/api/sitter-bookings` | Book a pet sitting engagement | Owner |
| `PATCH` | `/api/sitter-bookings/{id}/end` | End sitting session and trigger payment release | Owner |
| `PATCH` | `/api/sitter-bookings/{id}/cancel` | Cancel a sitting booking | Owner / Sitter |
| `POST` | `/api/sitter-bookings/{id}/checkin` | Submit live check-in note with timestamp | Sitter |
| `GET` | `/api/medical-records/pet/{petId}` | Retrieve chronological medical timeline for a pet | Owner / Vet |
| `POST` | `/api/medical-records` | Add a diagnosis or medical treatment record | Vet |
| `GET` | `/api/vaccinations/pet/{petId}` | Retrieve vaccination records for a pet | Owner / Vet |
| `POST` | `/api/vaccinations` | Record a administered vaccination | Vet |
| `GET` | `/api/reminders` | Retrieve preventive care reminders | Owner / Vet |
| `POST` | `/api/reminders` | Create clinical reminder for patient | Vet |
| `GET` | `/api/payments/user/{userId}` | Retrieve full transaction and billing history in ₹ INR | Owner |
| `GET` | `/api/admin/sitters` | List all sitters with verification statuses | Admin |
| `PATCH` | `/api/admin/sitters/{id}/approve` | Approve sitter and set approved hourly rate (`₹/hr`) | Admin |
| `GET` | `/api/admin/vets` | List all veterinarians with verification statuses | Admin |
| `PATCH` | `/api/admin/vets/{id}/approve` | Approve veterinarian license | Admin |
| `GET` | `/api/admin/documents/{userId}` | List uploaded verification documents for applicant | Admin |
| `GET` | `/api/admin/documents/download/{docId}` | Download or view applicant verification PDF | Admin |
| `GET` | `/api/admin/stats` | Retrieve aggregated platform metrics and statistics | Admin |

---

## 5. Getting Started

### Prerequisites
* **Java Development Kit:** JDK 25 LTS (tested with build 25.0.5+9-LTS; bytecode compiler targets `--release 21` for maximum framework stability)
* **Maven:** Apache Maven 3.9+
* **Database:** PostgreSQL 15+ (local or Supabase) or automated fallback to in-memory H2

### Step 1: Environment Configuration
Copy `.env.example` to `.env` in the repository root and customize credentials:
```bash
cp .env.example .env
```
Ensure your `.env` contains:
```properties
# Database Connection
DB_URL=jdbc:postgresql://localhost:5432/pawlink
DB_USER=postgres
DB_PASSWORD=your_secure_password

# Platform Administrator Credentials (Strictly provisioned here)
ADMIN_NAME=Platform Administrator
ADMIN_EMAIL=admin@pawlink.com
ADMIN_PHONE=+919876543210
ADMIN_PASSWORD=AdminSecurePass2026!
```

### Step 2: Database Initialization (PostgreSQL)
Run the DDL schema and seed scripts using `psql` or your database manager:
```bash
psql -U postgres -d pawlink -f db/schema.sql
psql -U postgres -d pawlink -f db/seed.sql
```

### Step 3: Build the Project
Compile and execute test suites across all modules:
```powershell
mvn clean test
```

### Step 4: Run the Application

#### Option A: Quick Scripts (Recommended)
* **Windows:**
  - Launch Backend: `scripts\run-backend.bat`
  - Launch Swing GUI: `scripts\run-swing-client.bat` (or `scripts\run-swing-debug.bat` for console logs)
* **Linux / macOS:**
  - Launch Backend: `./scripts/run-backend.sh`
  - Launch Swing GUI: `./scripts/run-swing.sh`

#### Option B: Maven CLI
* **Terminal 1 — Backend REST API (Port 8080):**
  ```bash
  mvn spring-boot:run -pl backend
  ```
* **Terminal 2 — Swing Desktop Application:**
  ```bash
  mvn package -pl swing-vet -DskipTests
  java -jar swing-vet/target/swing-vet-1.0.0.jar
  ```

#### Option C: DevContainer (Isolated Docker Sandbox)
1. Open this repository in VS Code with the **Dev Containers** extension installed.
2. Select **Dev Containers: Reopen in Container** when prompted.
3. Automatically provisions **Java 25 (JDK 25 LTS)**, caches dependencies in a persistent volume, and exposes:
   - Backend API: `http://localhost:8080` (or `http://<LAN_IP>:8080`)
   - Swing Desktop GUI in-browser (noVNC): `http://localhost:6080` (password: `pawlink`)
4. Run inside the container terminal:
   - `./scripts/run-backend.sh`
   - `./scripts/run-swing.sh`

---

## 6. Pre-Seeded Demo Accounts

The application login window provides quick credentials for testing all 4 roles:

| Role | Email | Password | Pre-Configured State |
|---|---|---|---|
| **Admin** | `admin@pawlink.com` | `AdminSecurePass2026!` | Full platform administration, sitter/vet approval tabs & stats |
| **Owner** | `owner.priya@pawlink.com` | `OwnerPass123!` | Registered pets, appointments, wallet balance, sitter bookings |
| **Vet** | `vet.sarah@pawlink.com` | `VetPass123!` | Approved status, clinic consultations, medical & vaccine timeline |
| **Sitter** | `sitter.rahul@pawlink.com` | `SitterPass123!` | Approved status, assigned bookings, live check-ins feed |

---

## 7. Testing & Quality Assurance

PawLink contains comprehensive automated unit and integration tests covering business logic, data access, role restrictions, and REST controller endpoints:

```powershell
mvn test
```
* **Backend:** 73 passing tests covering `AdminController`, `AdminService`, `AuthController`, `UserService`, `AppointmentService`, `SittingBookingService`, `ReminderScheduler`, and `PaymentDAO`.
* **Swing Client:** Shaded standalone distribution verified on JDK 25 LTS.
