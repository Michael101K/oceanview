# 🌊 Ocean View Resort — Online Room Reservation System

![Java](https://img.shields.io/badge/Java-11-orange?logo=java)
![Maven](https://img.shields.io/badge/Maven-3-red?logo=apachemaven)
![Tomcat](https://img.shields.io/badge/Tomcat-9.0-yellow?logo=apachetomcat)
![MySQL](https://img.shields.io/badge/MySQL-8.0-blue?logo=mysql)
![JUnit](https://img.shields.io/badge/JUnit-5.10-green?logo=junit5)
![License](https://img.shields.io/badge/License-Academic-lightgrey)

A full-stack, web-based hotel room reservation system built for **Ocean View Resort**, a beachside hotel in Galle, Sri Lanka. Developed as part of the **CIS6003 Advanced Programming** module at Cardiff Metropolitan University.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [Database Setup](#database-setup)
- [Installation & Running](#installation--running)
- [Running Tests](#running-tests)
- [Design Patterns](#design-patterns)
- [Screenshots](#screenshots)
- [Version History](#version-history)

---

## Overview

Ocean View Resort previously managed all reservations manually, causing booking conflicts and data loss. This system replaces the manual process with a secure, role-based web application that handles the full guest lifecycle — from reservation to check-out and billing.

### Two User Roles
| Role | Access |
|------|--------|
| **Admin** | Full access — reservations, billing, rooms, services, users, reports |
| **Receptionist** | Operational access — reservations, billing, check-in/out, guest history |

---

## Features

| Module | Functionality |
|--------|--------------|
| 🔐 **Authentication** | Login/logout with SHA-256 hashed passwords, session management, role-based UI |
| 📅 **Reservations** | Create, view, search, cancel reservations with auto-generated reservation numbers (OVR-YYYY-XXXX) |
| 🏨 **Check-in / Check-out** | Status-driven workflow — CONFIRMED → CHECKED_IN → CHECKED_OUT |
| 🧾 **Billing** | Auto-calculated bills with 10% VAT, optional discount, multiple payment methods, printable receipts |
| 🛏️ **Room Management** | Full CRUD for rooms and room types, real-time availability tracking |
| 👤 **Guest History** | Search guests by name or NIC, view full booking history and spending stats |
| 🛎️ **Services** | Manage resort services (Spa, Dining, Transport, etc.) by category |
| 📊 **Reports** | Revenue analytics, occupancy rates, booking trends, top guests — with date range filter |
| 👥 **User Management** | Admin-only staff account management with activate/deactivate |
| 🔑 **Password Change** | Self-service password change with live strength meter |
| ❓ **Help & Guide** | 9-section built-in staff guide |

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 11 |
| **Web Framework** | Java Servlets (javax.servlet 4.0.1) |
| **Server** | Apache Tomcat 9.0 |
| **Database** | MySQL 8.0 |
| **DB Connectivity** | JDBC (mysql-connector-java 8.0.33) |
| **Build Tool** | Apache Maven 3 |
| **Testing** | JUnit 5 (junit-jupiter 5.10.0) |
| **Mocking** | Mockito 5.5.0 |
| **Frontend** | HTML5, CSS3, JavaScript (rendered via Servlets) |
| **Architecture** | 3-Tier MVC |

---

## Project Structure

```
OceanViewResort/
├── src/
│   ├── main/
│   │   ├── java/lk/oceanview/
│   │   │   ├── model/          # Domain entities
│   │   │   │   ├── User.java
│   │   │   │   ├── Guest.java
│   │   │   │   ├── Room.java
│   │   │   │   ├── Reservation.java
│   │   │   │   ├── Bill.java
│   │   │   │   └── Service.java
│   │   │   ├── dao/            # Data Access Objects
│   │   │   │   ├── DBConnection.java
│   │   │   │   ├── UserDAO.java
│   │   │   │   ├── GuestDAO.java
│   │   │   │   ├── RoomDAO.java
│   │   │   │   ├── ReservationDAO.java
│   │   │   │   ├── BillDAO.java
│   │   │   │   └── ServiceDAO.java
│   │   │   └── servlet/        # Controllers
│   │   │       ├── LoginServlet.java
│   │   │       ├── LogoutServlet.java
│   │   │       ├── DashboardServlet.java
│   │   │       ├── ReservationServlet.java
│   │   │       ├── GuestServlet.java
│   │   │       ├── RoomServlet.java
│   │   │       ├── BillServlet.java
│   │   │       ├── ServiceServlet.java
│   │   │       ├── UserServlet.java
│   │   │       ├── ReportServlet.java
│   │   │       ├── ProfileServlet.java
│   │   │       └── HelpServlet.java
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       │   └── web.xml
│   │       └── login.html
│   └── test/
│       └── java/lk/oceanview/
│           ├── model/
│           │   ├── BillTest.java        # 15 tests — financial logic
│           │   ├── ReservationTest.java # 20 tests — night calc & status
│           │   ├── UserTest.java        # 16 tests — role logic
│           │   ├── GuestTest.java       # 12 tests — data storage
│           │   ├── RoomTest.java        # 16 tests — status transitions
│           │   └── ServiceTest.java     # 15 tests — availability logic
│           └── AllTests.java            # Full test suite runner
├── pom.xml
└── README.md
```

---

## Database Setup

### 1. Create the database

```sql
CREATE DATABASE ocean_view_resort;
USE ocean_view_resort;
```

### 2. Create tables

```sql
-- ============================================================
--  Ocean View Resort - Room Reservation System
--  Database Schema
--  MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS ocean_view_resort;
USE ocean_view_resort;

-- ------------------------------------------------------------
-- 1. USERS TABLE
--    Stores staff login credentials and roles
-- ------------------------------------------------------------
CREATE TABLE users (
    user_id       INT AUTO_INCREMENT PRIMARY KEY,
    username      VARCHAR(50)  NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,          -- store hashed passwords (SHA-256)
    full_name     VARCHAR(100) NOT NULL,
    role          ENUM('ADMIN', 'RECEPTIONIST') NOT NULL,
    email         VARCHAR(100),
    is_active     BOOLEAN DEFAULT TRUE,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 2. ROOM_TYPES TABLE
--    Defines available room categories and their nightly rates
-- ------------------------------------------------------------
CREATE TABLE room_types (
    room_type_id  INT AUTO_INCREMENT PRIMARY KEY,
    type_name     VARCHAR(50)    NOT NULL UNIQUE,  -- e.g. Single, Double
    description   TEXT,
    rate_per_night DECIMAL(10,2) NOT NULL,
    max_occupancy INT            NOT NULL
);

-- ------------------------------------------------------------
-- 3. ROOMS TABLE
--    Individual physical rooms in the hotel
-- ------------------------------------------------------------
CREATE TABLE rooms (
    room_id       INT AUTO_INCREMENT PRIMARY KEY,
    room_number   VARCHAR(10)  NOT NULL UNIQUE,    -- e.g. 101, 202A
    room_type_id  INT          NOT NULL,
    floor         INT          NOT NULL,
    status        ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE') DEFAULT 'AVAILABLE',
    FOREIGN KEY (room_type_id) REFERENCES room_types(room_type_id)
);

-- ------------------------------------------------------------
-- 4. GUESTS TABLE
--    Stores guest personal information
-- ------------------------------------------------------------
CREATE TABLE guests (
    guest_id      INT AUTO_INCREMENT PRIMARY KEY,
    full_name     VARCHAR(100) NOT NULL,
    address       TEXT         NOT NULL,
    contact_number VARCHAR(20) NOT NULL,
    email         VARCHAR(100),
    nic_number    VARCHAR(20)  UNIQUE,             -- National ID / Passport
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ------------------------------------------------------------
-- 5. RESERVATIONS TABLE
--    Core booking information linking guests and rooms
-- ------------------------------------------------------------
CREATE TABLE reservations (
    reservation_id     INT AUTO_INCREMENT PRIMARY KEY,
    reservation_number VARCHAR(20)  NOT NULL UNIQUE,  -- e.g. OVR-2025-0001
    guest_id           INT          NOT NULL,
    room_id            INT          NOT NULL,
    check_in_date      DATE         NOT NULL,
    check_out_date     DATE         NOT NULL,
    num_nights         INT          GENERATED ALWAYS AS (DATEDIFF(check_out_date, check_in_date)) STORED,
    total_amount       DECIMAL(10,2),
    status             ENUM('CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') DEFAULT 'CONFIRMED',
    special_requests   TEXT,
    created_by         INT,                            -- user_id of staff who made booking
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (guest_id)   REFERENCES guests(guest_id),
    FOREIGN KEY (room_id)    REFERENCES rooms(room_id),
    FOREIGN KEY (created_by) REFERENCES users(user_id)
);

-- ------------------------------------------------------------
-- 6. BILLS TABLE
--    Stores generated bill details for each reservation
-- ------------------------------------------------------------
CREATE TABLE bills (
    bill_id            INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id     INT            NOT NULL,
    room_charges       DECIMAL(10,2)  NOT NULL,
    tax_amount         DECIMAL(10,2)  NOT NULL,       -- e.g. 10% VAT
    discount_amount    DECIMAL(10,2)  DEFAULT 0.00,
    total_amount       DECIMAL(10,2)  NOT NULL,
    payment_status     ENUM('PENDING', 'PAID', 'PARTIALLY_PAID') DEFAULT 'PENDING',
    payment_method     ENUM('CASH', 'CARD', 'ONLINE') DEFAULT 'CASH',
    generated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id)
);

CREATE TABLE services (
    service_id    INT AUTO_INCREMENT PRIMARY KEY,
    service_name  VARCHAR(100) NOT NULL,
    description   TEXT,
    price         DECIMAL(10,2) NOT NULL,
    category      VARCHAR(50),
    is_available  BOOLEAN DEFAULT TRUE
);

CREATE TABLE reservation_services (
    id             INT AUTO_INCREMENT PRIMARY KEY,
    reservation_id INT NOT NULL,
    service_id     INT NOT NULL,
    quantity       INT DEFAULT 1,
    FOREIGN KEY (reservation_id) REFERENCES reservations(reservation_id),
    FOREIGN KEY (service_id)     REFERENCES services(service_id)
);

-- ============================================================
--  SAMPLE DATA
-- ============================================================

-- Default users (password = 'admin123' and 'recep123' - hashed with SHA-256)
INSERT INTO users (username, password, full_name, role, email) VALUES
('admin',       SHA2('admin123', 256), 'System Administrator', 'ADMIN',        'admin@oceanviewresort.lk'),
('receptionist',SHA2('recep123', 256), 'Sarah Fernando',       'RECEPTIONIST', 'sarah@oceanviewresort.lk');

-- Room types with nightly rates (LKR)
INSERT INTO room_types (type_name, description, rate_per_night, max_occupancy) VALUES
('Standard Single', 'Cozy room with a single bed and garden view',        8500.00,  1),
('Standard Double', 'Comfortable room with a double bed and garden view', 12000.00, 2),
('Deluxe Double',   'Spacious room with ocean view and balcony',          18500.00, 2),
('Family Suite',    'Large suite with two bedrooms and living area',      28000.00, 4),
('Honeymoon Suite', 'Luxury suite with jacuzzi and private ocean view',   45000.00, 2);

-- Sample rooms
INSERT INTO rooms (room_number, room_type_id, floor, status) VALUES
('101', 1, 1, 'AVAILABLE'),
('102', 1, 1, 'AVAILABLE'),
('201', 2, 2, 'AVAILABLE'),
('202', 2, 2, 'AVAILABLE'),
('203', 3, 2, 'AVAILABLE'),
('301', 3, 3, 'AVAILABLE'),
('302', 4, 3, 'AVAILABLE'),
('401', 5, 4, 'AVAILABLE'),
('402', 5, 4, 'AVAILABLE');

-- Sample services
INSERT INTO services (service_name, description, price, category, is_available) VALUES
('Breakfast Buffet',   'Full buffet breakfast',     1500.00, 'Dining',       TRUE),
('Airport Transfer',   'Return airport pickup',     3500.00, 'Transport',    TRUE),
('Spa Treatment',      '60 min relaxation massage', 4500.00, 'Spa & Wellness', TRUE),
('Swimming Pool',      'Access to infinity pool',    500.00, 'Recreation',   TRUE),
('Room Service',       '24hr in-room dining',       1000.00, 'Room Service', TRUE);

-- ============================================================
--  USEFUL VIEWS
-- ============================================================

-- View: Full reservation details (used for Display Reservation feature)
CREATE VIEW vw_reservation_details AS
SELECT
    r.reservation_number,
    g.full_name        AS guest_name,
    g.contact_number,
    g.address,
    rm.room_number,
    rt.type_name       AS room_type,
    rt.rate_per_night,
    r.check_in_date,
    r.check_out_date,
    r.num_nights,
    r.total_amount,
    r.status,
    r.special_requests,
    r.created_at
FROM reservations r
JOIN guests       g  ON r.guest_id  = g.guest_id
JOIN rooms        rm ON r.room_id   = rm.room_id
JOIN room_types   rt ON rm.room_type_id = rt.room_type_id;

-- View: Available rooms (used when making a new reservation)
CREATE VIEW vw_available_rooms AS
SELECT
    rm.room_id,
    rm.room_number,
    rt.type_name       AS room_type,
    rt.rate_per_night,
    rt.max_occupancy,
    rm.floor
FROM rooms      rm
JOIN room_types rt ON rm.room_type_id = rt.room_type_id
WHERE rm.status = 'AVAILABLE';
```

### 3. Configure database connection

Edit `src/main/java/lk/oceanview/dao/DBConnection.java`:

```java
private static final String DB_URL      = "jdbc:mysql://localhost:3306/ocean_view_resort";
private static final String DB_USERNAME = "root";
private static final String DB_PASSWORD = "your_password";
```

---

## Installation & Running

### Prerequisites

- Java 11+
- Apache Maven 3+
- Apache Tomcat 9.0
- MySQL 8.0

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/Michael101K/oceanview.git
cd OceanViewResort

# 2. Set up the database (see Database Setup above)

# 3. Build the WAR file
mvn clean package

# 4. Deploy to Tomcat
# Copy target/OceanViewResort.war to Tomcat's webapps/ folder
# Then start Tomcat

# 5. Access the application
# http://localhost:8080/OceanViewResort/login.html
```

### Default Login

| Username | Password | Role |
|----------|----------|------|
| `admin` | `admin123` | Admin |

---

## Running Tests

```bash
# Run all 188 unit tests
mvn test
```

### Test Results

```
Tests run: 188, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

| Test Class | Tests | Coverage |
|-----------|-------|---------|
| `BillTest` | 15 | VAT, discounts, total calculations |
| `ReservationTest` | 20 | Night calculation, status transitions |
| `UserTest` | 16 | Role-based access logic (isAdmin()) |
| `GuestTest` | 12 | Field storage, boundary values |
| `RoomTest` | 16 | Status transitions, rate storage |
| `ServiceTest` | 15 | Availability toggling, field storage |

---

## Design Patterns

| Pattern | Where Used |
|---------|-----------|
| **Singleton** | `DBConnection.java` — single database connection instance |
| **DAO (Data Access Object)** | `*DAO.java` classes — separates SQL from business logic |
| **MVC (Model-View-Controller)** | Models in `/model`, views rendered by Servlets, controllers in `/servlet` |
| **Front Controller** | Each Servlet handles all actions for its domain via `?action=` parameter |
| **Post/Redirect/Get (PRG)** | All POST handlers redirect after success to prevent duplicate submissions |

---

## Screenshots

> *(Add screenshots of the running application here)*

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| v1.0.0 | Initial | Project setup, database schema, Login, Dashboard |
| v1.1.0 | — | Guest and Reservation management (CRUD) |
| v1.2.0 | — | Check-in, Check-out, Cancel workflows |
| v1.3.0 | — | Billing module with VAT and print receipt |
| v1.4.0 | — | Room and Service management |
| v1.5.0 | — | Reports, Guest History, User Management |
| v1.6.0 | — | Password change, Help guide, Profile page |
| v1.7.0 | — | JUnit 5 test suite — 188 tests passing |

---

## Module Information

| Field | Detail |
|-------|--------|
| Module | CIS6003 Advanced Programming |
| Assessment | WRIT1 — Online Room Reservation System |
| University | Cardiff Metropolitan University |
| Academic Year | 2025, Semester 1 |
