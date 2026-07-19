# 🏨 BookMyStay

A production-ready **Hotel Booking System** built with **Java 21**, **Spring Boot 3**, and **PostgreSQL**. BookMyStay allows users to search hotels, reserve rooms, manage bookings, and securely complete payments using **Razorpay**.

---

## 🚀 Features

### 👤 Authentication & Authorization
- JWT-based Authentication
- Role-based Authorization (User/Admin)
- Secure Password Encryption (BCrypt)
- Spring Security Integration

### 🏨 Hotel Management
- Add, Update, Delete Hotels
- Manage Hotel Details
- Room Management
- Room Availability Tracking

### 📅 Booking Management
- Create Bookings
- Guest Details Management
- Booking Status Tracking
- Booking Expiration Support

### 💳 Razorpay Payment Integration
- Create Razorpay Orders
- Secure Payment Verification
- HMAC Signature Validation
- Payment Retry Support
- Payment Status Tracking
- Booking Confirmation After Successful Payment

### 📖 API Documentation
- Swagger / OpenAPI Documentation

---

# 🛠 Tech Stack

| Technology | Version |
|------------|---------|
| Java | 21 |
| Spring Boot | 3.x |
| Spring Security | Latest |
| PostgreSQL | Latest |
| Spring Data JPA | Latest |
| Maven | Latest |
| Razorpay Java SDK | Latest |
| Lombok | Latest |
| Swagger OpenAPI | Latest |

---

# 📂 Project Structure

```
src
├── config
├── controller
├── dto
│   ├── auth
│   ├── booking
│   ├── hotel
│   ├── payment
│   └── room
├── entity
├── enums
├── exception
├── repository
├── security
├── service
│   ├── interfaces
│   └── impl
├── util
└── BookMyStayApplication.java
```

---

# 📌 Booking Flow

```
User
   │
   ▼
Search Hotel
   │
   ▼
Select Room
   │
   ▼
Create Booking
   │
   ▼
Add Guests
   │
   ▼
Payment Pending
   │
   ▼
Create Razorpay Order
   │
   ▼
Complete Payment
   │
   ▼
Verify Payment
   │
   ▼
Booking Confirmed
```

---

# 💳 Razorpay Payment Flow

```
Frontend
     │
     ▼
Create Order API
     │
     ▼
Razorpay Checkout
     │
     ▼
Payment Success
     │
     ▼
Receive:
- orderId
- paymentId
- signature
     │
     ▼
Backend Verification
     │
     ├── Verify Signature
     ├── Fetch Payment
     ├── Validate Amount
     └── Validate Order
     │
     ▼
Payment SUCCESS
     │
     ▼
Booking CONFIRMED
```

---

# 📊 Booking Status

| Status | Description |
|---------|-------------|
| RESERVED | Room reserved |
| GUESTS_ADDED | Guest details added |
| PAYMENTS_PENDING | Waiting for payment |
| CONFIRMED | Payment successful |
| CANCELLED | Booking cancelled |
| EXPIRED | Reservation expired |

---

# 💰 Payment Status

| Status | Description |
|---------|-------------|
| CREATED | Razorpay order created |
| PENDING | Payment pending |
| SUCCESS | Payment successful |
| FAILED | Payment failed |
| REFUNDED | Payment refunded |

---

# 🔐 Payment Security

BookMyStay uses multiple layers of payment verification:

- Razorpay Order Creation
- HMAC SHA256 Signature Verification
- Payment Fetch API Validation
- Amount Verification
- Order ID Verification
- Retry Payment Support
- Transactional Database Updates

---

# ⚙️ Installation

## Clone Repository

```bash
git clone https://github.com/<your-username>/BookMyStay.git
cd BookMyStay
```

---

## Configure Database

Create PostgreSQL Database

```sql
CREATE DATABASE bookmystay;
```

---

## Configure application.properties

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/bookmystay
spring.datasource.username=YOUR_USERNAME
spring.datasource.password=YOUR_PASSWORD

spring.jpa.hibernate.ddl-auto=update

jwt.secret=YOUR_SECRET

razorpay.key-id=YOUR_KEY_ID
razorpay.key-secret=YOUR_KEY_SECRET
razorpay.webhook-secret=YOUR_WEBHOOK_SECRET
```

---

## Run Project

```bash
mvn clean install
mvn spring-boot:run
```

---

# 📖 Swagger

After starting the application:

```
http://localhost:8080/swagger-ui/index.html
```

---

# 📌 Main APIs

## Authentication

```
POST /api/auth/register
POST /api/auth/login
```

## Hotels

```
GET /api/hotels
GET /api/hotels/{id}
POST /api/hotels
PUT /api/hotels/{id}
DELETE /api/hotels/{id}
```

## Rooms

```
POST /api/rooms
PUT /api/rooms/{id}
DELETE /api/rooms/{id}
```

## Bookings

```
POST /api/bookings
GET /api/bookings/{id}
```

## Payments

```
POST /api/payments/create-order
POST /api/payments/verify
```

---

# 🚧 Upcoming Features

- Razorpay Webhooks
- Refund API
- Booking Cancellation
- Automatic Booking Expiration
- Room Inventory Locking
- Email Notifications
- Booking History
- Hotel Search & Filters
- Admin Dashboard

---

# 👨‍💻 Author

**Tarsem Gulab**

- B.Tech CSE, IIIT Una
- Backend Developer (Java & Spring Boot)

---

# ⭐ If you found this project useful, consider giving it a star on GitHub!
