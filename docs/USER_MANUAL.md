# PawLink Pet Care Management System - User Manual & Operating Guide

**System Name:** PawLink Pet Care Management System  
**Document Type:** Official Software User Manual & Operational Instructions  
**Target Audience:** Students, Instructors, Evaluators, and System Users  
**Version:** 1.0.0 (Production Release)  

---

## 1. System Overview & Minimum Requirements

PawLink is a unified pet healthcare, clinical appointment scheduling, and escrow-backed pet sitting platform. It comprises:
1. **Spring Boot REST API (`backend`):** High-throughput microservice using pure Spring `JdbcTemplate` (zero JPA overhead) and BCrypt security.
2. **Swing Desktop Client (`swing-client`):** Shaded standalone desktop client featuring FlatLaf modern UI, custom Java2D vector graphics, and role-adaptive dashboards.
3. **Relational Database:** PostgreSQL (or Supabase Cloud) relational schema with 9 tables enforcing strict transactional integrity.

### Minimum System Requirements
- **Java Runtime Environment (JRE/JDK):** Java 21 or Java 25 LTS (tested with JDK 25.0.5).
- **Build Tool:** Apache Maven 3.9+ (or use pre-bundled wrapper/container).
- **Operating System:** Windows 10/11, macOS (Intel/Apple Silicon), or Linux (Ubuntu 20.04+).
- **Display Resolution:** Minimum $1280 \times 800$ recommended for Swing desktop interface.

---

## 2. Installation & Quick Start

### 2.1 Option A: One-Click Launchers (Recommended for Windows)

The repository provides automated startup scripts inside the `scripts/` directory:

1. **Start the Backend REST API Server:**
   - Double-click [`scripts/run-backend.bat`](file:///k:/Dev/PAWLINK/scripts/run-backend.bat).
   - The Spring Boot server will compile and start on `http://localhost:8080`.
   - Wait until you see: `Tomcat started on port 8080 (http) with context path '/'`.

2. **Start the Swing Desktop Client:**
   - Double-click [`scripts/run-swing-client.bat`](file:///k:/Dev/PAWLINK/scripts/run-swing-client.bat) (silent window) or [`scripts/run-swing-debug.bat`](file:///k:/Dev/PAWLINK/scripts/run-swing-debug.bat) (with console output).
   - The shaded executable JAR `swing-client-1.0.0.jar` will launch the modern login window.

### 2.2 Option B: Cross-Platform Maven Commands (macOS / Linux)

Open two terminal windows in the project root:

```bash
# Terminal 1: Launch Backend REST API
mvn spring-boot:run -pl backend

# Terminal 2: Package and Launch Swing Client
mvn package -pl swing-client -DskipTests
java -jar swing-client/target/swing-client-1.0.0.jar
```

### 2.3 Option C: VS Code DevContainer (Zero-Install Docker Sandbox)

1. Open the repository in Visual Studio Code.
2. When prompted, click **Reopen in Container** (or press `F1` $\rightarrow$ `Dev Containers: Reopen in Container`).
3. The container provisions Java 25 LTS, Maven, and desktop-lite noVNC.
4. Interact with the desktop GUI directly inside your web browser at `http://localhost:6080` (Password: `pawlink`).

---

## 3. Pre-Seeded Demo Accounts & One-Click Login

The PawLink desktop login window features quick demo login pill buttons along the bottom for instant testing without manual typing:

| Role | Name | Email Address | Password | Pre-Configured State |
|---|---|---|---|---|
| **Admin** | Platform Administrator | `admin@pawlink.com` | `AdminSecurePass2026!` | Full platform administration, doctor/sitter approvals, system statistics |
| **Owner** | John Vargese | `owner.john@pawlink.com` | `password123` | Registered pets (`Bruno` Dog, `Jimmy` Cat), medical records, wallet balance |
| **Vet** | Dr. Sreekumar | `vet.sreekumar@pawlink.com` | `password123` | Verified clinic credentials, consultation appointments, vaccination history |
| **Sitter** | Emily Johnson | `sitter.emily@pawlink.com` | `password123` | Verified background check, ₹250/hr rate, assigned bookings, live check-ins |
| **Sitter 2** | Pramod S P | `sitter.pramod@pawlink.com` | `password123` | Pending verification approval (cannot be booked until approved by Admin) |

---

## 4. Role-Based User Manual & Operating Workflows

### 4.1 Pet Owner Workflow

#### 1. Signing In
- Launch client, click the **John (Owner)** quick-login button, or enter `owner.john@pawlink.com` with password `password123`, then click **Sign In**.

#### 2. Managing Pets & Reviewing Care Guidelines
- Click **My Pets** in the left sidebar.
- Click **+ Register Pet**.
- Enter Pet Name (e.g., `"Bruno"`), select Species (`Dog`, `Cat`, `Bird`, or `Reptile`), and pick Date of Birth.
- Click **Save Pet**.
- *System Feedback:* The system polymorphically attaches species-specific care guidance (e.g. Dog receives *"Daily exercise, annual rabies vaccine, dental checkups."*).

#### 3. Booking a Veterinary Clinic Appointment
- Click **Vet Appointments** $\rightarrow$ **+ Book Consultation**.
- Select the target Veterinarian (e.g., `Dr. Sreekumar`).
- Select your registered Pet (`Bruno`).
- Use the **Appointment Date & Time Picker**:
  - The picker automatically filters out past hours for today's date.
  - Pick a date and time slot (e.g., 10:00 AM).
- Click **Confirm Booking**.
- *Collision Check:* If another pet owner booked that doctor for that exact interval, the system immediately displays a conflict alert preventing double-booking.

#### 4. Finding & Booking a Pet Sitter (with Escrow Hold)
- Click **Find Pet Sitter**.
- Browse verified sitters displaying their hourly rate (e.g., `Emily Johnson` @ ₹250.00/hr). Note that unverified sitters cannot be booked (Domain Invariant).
- Click **Book Sitter**.
- Choose Start Time and End Time using the interactive `PawDateTimePicker` or click duration quick chips (`+1 Hour`, `+2 Hours`, `+4 Hours`).
- Select Payment Method:
  - **Credit / Debit Card** (`CardPayment`)
  - **UPI Gateway** (`UpiPayment`)
  - **In-App Wallet** (`WalletPayment`)
- Click **Authorize & Hold Payment**.
- *Polymorphic Processing:* The system dynamically instantiates the chosen payment class, authorizes payment, creates the booking with status `CONFIRMED`, and holds the amount in financial escrow (`HELD`).

#### 5. Tracking Live Sitter Check-Ins
- Click **Booked Pet Sitters**.
- Select the active booking in the table.
- Click **View Timeline** to inspect real-time timestamped care updates posted by the sitter (e.g., food intake, walks, medication).

#### 6. Concluding Session & Releasing Escrow Payment
- Once the sitting session concludes, select the booking and click **End Session**.
- Click **Confirm Completion**.
- *System Actions:*
  - The booking status updates to `COMPLETED`.
  - The escrow payment transitions from `HELD` to `RELEASED` directly to the sitter.
  - **Slot Recovery:** If the session is ended before its booked end time, the system automatically truncates the stored `end_time` to the current timestamp, immediately releasing the remaining time window as an available slot for future bookings!

---

### 4.2 Veterinarian Workflow

#### 1. Signing In
- Click the **Dr. Sreekumar (Vet)** demo pill or enter `vet.sreekumar@pawlink.com` / `password123`.

#### 2. Viewing Daily Consultations
- Navigate to **Appointments**.
- Inspect scheduled appointments with patient names, owner contact details, and scheduled intervals.
- Click **Confirm Appointment** to move a pending booking to `CONFIRMED`.

#### 3. Conducting Visit & Recording Medical Notes
- Select an appointment and click **Clinical Encounter / Records**.
- Enter Clinical Diagnosis (e.g., `"Canine Ear Dermatitis"`) and Prescription (e.g., `"Antibiotic drops 2x daily"`).
- Click **Save Medical Record**.
- The record is persisted in `medical_records` with an immutable link to the treating veterinarian.

#### 4. Administering Vaccinations & Scheduling Reminders
- In the patient dialog, click **+ Add Vaccine Booster**.
- Enter Vaccine Name (e.g., `"Rabies Annual"`) and administered date.
- Specify Next Due Date (e.g., +1 year).
- Click **Record Vaccine**.
- *Automatic Observer Integration:* The system creates a vaccination event and automatically schedules a pending preventive alert in the `reminders` table due on that future date!

---

### 4.3 Pet Sitter Workflow

#### 1. Signing In
- Click **Emily (Sitter)** or enter `sitter.emily@pawlink.com` / `password123`.

#### 2. Viewing Assigned Sitting Jobs
- Navigate to **Assigned Jobs**.
- Inspect confirmed sitting sessions, scheduled time slots, and held escrow payment values.

#### 3. Posting Real-Time Care Check-Ins
- Select the active booking and click **Pet Check-Ins** or **+ Log Check-In**.
- Choose quick activity chips:
  - `"30-min walk completed"`
  - `"Fresh water & meal served"`
  - `"Medication administered"`
- Add custom observations in the text field (e.g., `"Bruno was very energetic in the dog park"`).
- Click **Post Care Note**.
- The update is immediately saved to the `checkins` table and appears on the owner's timeline dialog.

---

### 4.4 Platform Administrator Workflow

#### 1. Signing In
- Click **Admin** or enter `admin@pawlink.com` / `AdminSecurePass2026!`.

#### 2. Approving Pet Sitters
- Navigate to **Sitter Approvals**.
- Review registered sitters, submitted identity proof, phone numbers, and hourly rates.
- Select `Pramod S P` (currently unverified).
- Click **Approve Sitter**.
- *Domain Action:* Updates `verified = TRUE` in the `sitters` table, immediately making the sitter discoverable and bookable by all platform pet owners.

#### 3. Approving Veterinarians
- Navigate to **Vet Approvals**.
- Review veterinary clinic registration numbers and medical credentials.
- Click **Approve Doctor** to grant clinical appointment eligibility.

#### 4. Inspecting Platform Analytics & Live Statistics
- Navigate to **Platform Statistics**.
- Inspect multi-table system metrics computed via SQL aggregations:
  - Total Registered Users by Role (Owners, Vets, Sitters, Admins).
  - Total Consultations Completed.
  - Sitter Bookings Breakdown (`CONFIRMED`, `COMPLETED`, `CANCELLED`).
  - Total Platform Escrow Volume Settled.

---

## 5. Automated Care Reminder Sweep (Observer Pattern)

To manually trigger or verify the background preventive reminder engine:
1. Log in as any Veterinarian or Administrator.
2. Navigate to **Reminders**.
3. Click the **Run Observer Scan** button.
4. The system executes `ReminderScheduler.scanAndDispatchDueReminders()`:
   - Sweeps all records where `due_date <= CURRENT_DATE` and `sent = FALSE`.
   - Iterates through registered observers (`EmailAdapter`, `SmsAdapter`).
   - Dispatches formatted simulated alerts to the pet parents.
   - Marks reminders as `sent = TRUE` to guarantee idempotent execution.

---

## 6. Troubleshooting & Frequently Asked Questions (FAQ)

### Q1: The backend fails to start reporting `Address already in use: bind 8080`.
**Solution:** Another process is utilizing port 8080. Terminate the old process using PowerShell:
```powershell
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8080).OwningProcess -Force
```
Then re-run `scripts/run-backend.bat`.

### Q2: Why does the project compile with `--release 21` when Java 25 is installed?
**Solution:** Spring Boot 3.3.4 uses ASM bytecode inspection libraries that do not yet recognize class file major version 69 (Java 25). Compiling with `--release 21` produces class file version 65 (Java 21 bytecode), allowing the application to execute stably on the Java 25 LTS virtual machine while preserving 100% framework stability.

### Q3: How do I run all automated tests?
**Solution:** Execute the following in the repository root:
```powershell
mvn test -f backend/pom.xml
```
All 75 automated unit and integration tests will execute and pass with 0 failures.

