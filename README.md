# PawLink — Pet Care Management System
## Team Architecture & Person 1 (P1 - Auth & Core) Implementation

**Course:** PBCST304 · Object Oriented Programming  
**Branch:** `feature/P1-auth-core`  
**Role:** Person 1 (Auth, Project Core Architecture, Swing Shell, Web Portal Gateway)

---

## 1. Project Overview & Architecture

PawLink is an 8-person multi-module pet care management system built with:
- **Java 17 & Spring Boot 3**
- **Spring JdbcTemplate** (Plain SQL with `RowMapper<T>`, **Strictly NO JPA / Hibernate**)
- **PostgreSQL on Supabase** (Shared database pooler with HikariCP)
- **Java Swing Desktop Application** (Modern Look & Feel with FlatLaf, role-adaptive shell)
- **Web Portal Gateway** (Spring Boot + RestClient on port 8081 forwarding `X-User-Id` to backend)

### Monorepo Structure

```
pawlink/
├── pom.xml                                   # Root Maven parent reactor
├── db/
│   ├── schema.sql                            # Core DDL: users + teammate vertical tables
│   └── seed.sql                              # Pre-populated users with BCrypt hashes & test records
├── backend/                                  # Spring Boot 3 REST API (Port 8080)
│   ├── pom.xml
│   ├── src/main/resources/
│   │   ├── application.properties
│   │   └── application-local.properties.example
│   └── src/main/java/com/pawlink/
│       ├── PawLinkApplication.java           # Entrypoint (P1 managed)
│       ├── config/AppConfig.java             # BCrypt bean, DBConnectionManager initialization
│       ├── common/
│       │   ├── db/DBConnectionManager.java   # Singleton pattern wrapping DataSource
│       │   └── exception/                    # Custom exceptions + GlobalExceptionHandler
│       └── user/                             # P1 Vertical
│           ├── User.java                     # Abstract base entity
│           ├── PetOwner.java                 # Subclass (role = 'OWNER')
│           ├── Veterinarian.java             # Subclass (role = 'VET')
│           ├── PetSitter.java                # Subclass (role = 'SITTER')
│           ├── Admin.java                    # Subclass (role = 'ADMIN')
│           ├── UserFactory.java              # Factory pattern instantiation
│           ├── UserDAO.java                  # JdbcTemplate parameterized queries
│           ├── UserService.java              # BCrypt hashing & business logic
│           ├── AuthController.java           # REST Endpoints
│           └── dto/                          # AuthResponse, LoginRequest, RegisterRequest
├── swing-vet/                                # Unified Java Swing Desktop Client
│   ├── pom.xml
│   └── src/main/java/com/pawlink/vet/
│       ├── VetApp.java                       # Desktop launcher
│       ├── api/
│       │   ├── ApiClient.java                # Java 11+ HttpClient & Jackson; appends X-User-Id
│       │   ├── Session.java                  # Thread-safe Singleton session context
│       │   └── ApiException.java             # HTTP error mapping
│       ├── base/
│       │   ├── BasePanel.java                # Abstract base panel with branded header
│       │   ├── BaseTableModel.java           # Generic parameterized TableModel<T>
│       │   └── Dialogs.java                  # JOptionPane utility helpers
│       ├── model/                            # Client DTOs
│       └── view/
│           ├── LoginFrame.java               # Authentication with quick-fill demo buttons
│           ├── RegisterDialog.java           # Account registration modal
│           └── MainFrame.java                # Centralized JTabbedPane shell (P1 managed)
└── web-portal/                               # Web Portal Gateway (Port 8081)
    ├── pom.xml
    └── src/main/
        ├── java/com/pawlink/web/
        │   ├── WebPortalApplication.java
        │   ├── api/ApiGateway.java           # Spring 6 RestClient forwarding X-User-Id
        │   ├── config/RoleGuardInterceptor.java
        │   └── controller/AuthWebController.java
        └── resources/templates/
            ├── layout.html                   # Shared Bootstrap 5 navbar fragment (P1 managed)
            └── auth/                         # login.html, register.html
```

---

## 2. API Contract & Auth Architecture

PawLink adopts a **simplified session auth model without JWT**:
1. **Login:** `POST /api/auth/login` verifies credentials and returns:
   ```json
   {
     "id": 1,
     "name": "Dr. Sarah Jenkins",
     "email": "vet.sarah@pawlink.com",
     "role": "VET"
   }
   ```
2. **Subsequent Calls:** All authenticated requests forward the `X-User-Id: <id>` HTTP header.
   - In **Swing**: `ApiClient` automatically extracts `Session.getInstance().getUserId()` and attaches `X-User-Id`.
   - In **Web Portal**: `ApiGateway` extracts the user from `HttpSession` and forwards `X-User-Id`.
3. **Password Security:** Hashed and verified using Spring Security's `BCryptPasswordEncoder`.

### Core Endpoints Implemented

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/auth/register` | Registers user (`OWNER`, `VET`, `SITTER`, `ADMIN`) with BCrypt password hashing |
| `POST` | `/api/auth/login` | Authenticates email & password, returns user info |
| `GET` | `/api/vets` | Returns all registered Veterinarians (`role = 'VET'`) |
| `GET` | `/api/auth/me` | Validates session via `X-User-Id` header |

---

## 3. How to Run

### Step 1: Database Setup
Execute the DDL and Seed scripts in PostgreSQL / Supabase SQL Editor:
```bash
# 1. Run Schema
psql -U postgres -d pawlink -f db/schema.sql

# 2. Run Seed Data
psql -U postgres -d pawlink -f db/seed.sql
```

### Step 2: Build All Modules
```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21" # Or JDK 17+
mvn clean test
```

### Step 3: Run Backend REST API (Port 8080)
```powershell
cd backend
mvn spring-boot:run
```

### Step 4: Run Swing Desktop Application
```powershell
cd swing-vet
mvn compile exec:java -Dexec.mainClass="com.pawlink.vet.VetApp"
```
*Note: `LoginFrame` includes one-click **Quick Demo Login** buttons for Vet, Owner, Sitter, and Admin roles!*

### Step 5: (Optional) Run Web Portal (Port 8081)
```powershell
cd web-portal
mvn spring-boot:run
```

---

## 4. Viva / Teach-Back Guide (Person 1)

When presenting to evaluators, focus on these four core competencies:

### 1. Object-Oriented Programming (OOP)
* **Inheritance:** The abstract base class `User` defines common attributes (`userId`, `name`, `email`, `phone`, `passwordHash`, `createdAt`). Four concrete subclasses (`PetOwner`, `Veterinarian`, `PetSitter`, `Admin`) extend `User` and provide specific role behavior.
* **Encapsulation:** All fields in `User` are `private`, preventing direct external tampering. State is read or modified through public getters and setters. Subclasses cannot violate parent invariants.
* **Polymorphism:** The abstract method `public abstract String getRole()` is implemented uniquely by each subclass. Callers can treat any user polymorphically (`User user = userDAO.findByEmail(...)`) while obtaining the correct role without `instanceof` checks.

### 2. Design Patterns
* **Singleton Pattern (`DBConnectionManager` & `Session`):**
  - Thread-safe implementation utilizing **Double-Checked Locking** (`volatile` instance + synchronized block).
  - Eliminates overhead of synchronized access after the instance is initialized.
  - Centralizes access to the underlying `DataSource` across the application.
* **Factory Pattern (`UserFactory`):**
  - `UserFactory.createUser(role, ...)` acts as a centralized factory.
  - Decouples client code (such as Spring's `RowMapper` and `UserService`) from concrete subclass constructors.
  - Adding a new user role in the future requires editing only `UserFactory` and extending `User`, adhering to the Open/Closed Principle.

### 3. Database & SQL Best Practices
* **Parameterized Query:** Role-based lookups use SQL parameters (`?`):
  ```sql
  SELECT id, name, email, phone, password_hash, role, created_at FROM users WHERE email = ?
  ```
* **Security:** Values are passed via JDBC `PreparedStatement` to prevent SQL injection.
* **Pure JdbcTemplate:** Implemented without JPA/Hibernate annotations (`@Entity`, `@Table`), maintaining direct control over database transactions and performance.

### 4. System Architecture
* **Centralized API Client / Gateway:**
  - `ApiClient` in Swing and `ApiGateway` in Web Portal centralize all HTTP networking.
  - Automatically appends the required `X-User-Id` header to every outgoing request.
  - Avoids duplicating networking and JSON error handling logic across feature panels.
