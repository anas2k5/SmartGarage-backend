# 🚗 Smart Garage – Backend Service

Backend service for the **Smart Garage Vehicle Repair & Service Booking System** built using **Spring Boot**.

This service handles:
- Authentication & authorization
- Booking lifecycle management
- Payments & refunds
- Invoice generation
- Email notifications

---

## 📌 Project Overview

Smart Garage is a vehicle service booking platform where:

- Customers can book garage services
- Garage owners manage bookings and mechanics
- Payments, refunds, invoices, and notifications are handled automatically

---

## 🧱 Tech Stack

| Layer | Technology |
|------|-----------|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security + JWT |
| Database | PostgreSQL |
| ORM | Hibernate / JPA |
| Build Tool | Maven |
| Mail | Spring Mail (Gmail SMTP) |
| API Style | REST |

---

## ⚙️ Local Setup Instructions

### 1️⃣ Prerequisites

Make sure the following are installed:

- Java 17+
- Maven
- PostgreSQL
- Postman (for API testing)

---

### 2️⃣ Database Setup

Create the PostgreSQL database:

```sql
CREATE DATABASE smart_garage;
```

---

### 3️⃣ Application Configuration

Create `application.yml` inside `src/main/resources`.

> ⚠️ **Do NOT commit real credentials to GitHub**

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smart_garage
    username: postgres
    password: postgres

  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true

  mail:
    host: smtp.gmail.com
    port: 587
    username: your_email@gmail.com
    password: your_app_password
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true

jwt:
  secret: your_jwt_secret_key
  expiration: 86400000
```

---

### 4️⃣ Run Backend

Build and start the application:

```bash
mvn clean install
mvn spring-boot:run
```

Backend will start at:

```text
http://localhost:8080
```

---

## 👥 Test Accounts (Optional)

> Depends on seed data / database state

| Role | Email | Password |
|------|-------|----------|
| Customer | customer@test.com | 123456 |
| Owner | owner@test.com | 123456 |
| Admin | admin@test.com | 123456 |

---

## 📦 Core Features Implemented

### ✅ Authentication

- Login / Register
- JWT token generation
- Role-based access control

---

### ✅ Booking Management

- Create booking
- Accept / Cancel booking
- Assign mechanic
- Update booking status

**Booking Lifecycle:**

```
PENDING → ACCEPTED → IN_PROGRESS → COMPLETED
```

---

### ✅ Cost Management

- Update estimated cost
- Update final cost
- Owner / Admin access only

---

### ✅ Email Notifications

- Booking creation email
- Booking status update email
- Payment confirmation email
- Invoice email with PDF attachment

---

### ✅ Payments

- Payment initiation
- Payment confirmation
- Invoice generation (PDF)
- Auto booking completion after payment

---

### ✅ Refunds

- Refund processing
- Refund validation
- Refund status tracking

---

### ✅ Booking Status History

Tracks every booking status change:

- Old status
- New status
- Changed by user
- Timestamp

---

## 📡 API Overview

### 🔑 Authentication

```http
POST /api/auth/login
POST /api/auth/register
```

---

### 📅 Bookings

```http
POST   /api/bookings
GET    /api/bookings/{id}
GET    /api/bookings/customer/{id}
PUT    /api/bookings/{id}/status
PUT    /api/bookings/{id}/assign
PUT    /api/bookings/{id}/estimate
PUT    /api/bookings/{id}/final-cost
```

---

### 💳 Payments

```http
POST /api/payments/{bookingId}/initiate
POST /api/payments/{bookingId}/confirm
GET  /api/payments/{bookingId}
```

---

### 🔁 Refunds

```http
POST /api/refunds/{bookingId}
```

---

## 🧪 Testing Instructions (Frontend Team)

1. Login to get JWT token
2. Copy the token
3. Add header in requests:

```http
Authorization: Bearer <JWT_TOKEN>
```

4. Call APIs via Postman or frontend app

---

## 🚨 Important Notes

- Backend logic is final and stable
- Frontend team should not modify backend code
- API contracts must be followed strictly
- Any changes must be discussed first

---

## 🧠 Developer Notes

- DTO-based responses (no entity leakage)
- Global exception handling implemented
- Role-based authorization enforced
- Designed for scalability and clarity

---

## 📤 Handover Checklist

- ✅ Backend completed
- ✅ APIs tested
- ✅ Payment & refund flows verified
- ✅ Email notifications working
- ✅ README provided
- ✅ Ready for frontend integration

---

## 👨‍💻 Backend Developer

**Anas Syed**  
Final Year CSE  
**Smart Garage Project**
