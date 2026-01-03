# 🚗 Smart Garage – Backend Service

This repository contains the **backend implementation** for the **Smart Garage Booking System**, responsible for authentication, booking lifecycle management, payments, refunds, invoices, and notifications.

---

## 📌 Project Overview

Smart Garage is a vehicle service booking platform where:

- Customers can book garage services  
- Garage owners manage bookings & mechanics  
- Payments, refunds, invoices, and notifications are handled automatically  

---

## 🧱 Tech Stack

| Layer | Technology |
|-----|-----------|
| Language | Java 17 |
| Framework | Spring Boot |
| Security | Spring Security + JWT |
| Database | PostgreSQL |
| ORM | Hibernate / JPA |
| Mail | Spring Mail (Gmail SMTP) |
| Build Tool | Maven |
| API Style | REST |

---

## 🔐 Authentication & Authorization

- JWT-based authentication  
- Roles supported:
  - `CUSTOMER`
  - `OWNER`
  - `ADMIN`
- All secured endpoints require:

```http
Authorization: Bearer <JWT_TOKEN>
