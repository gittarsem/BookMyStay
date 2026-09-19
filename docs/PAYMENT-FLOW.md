# 💳 BookMyStay — Payment Flow

## 📌 Overview

BookMyStay uses **Razorpay** for online payment processing.

The backend creates payment orders and validates payment results before confirming bookings.

## 🔄 Payment Lifecycle

```mermaid
flowchart TD
    BOOKING["Payment Pending Booking"]
    BOOKING --> CREATE["Create Razorpay Order"]
    CREATE --> CHECKOUT["Razorpay Checkout"]
    CHECKOUT --> RESULT["Payment Result"]
    RESULT --> VERIFY["Backend Verification"]
    VERIFY --> VALIDATE{"Validation Successful?"}
    VALIDATE -->|Yes| SUCCESS["Payment Completed"]
    SUCCESS --> CONFIRM["Booking Confirmed"]
    VALIDATE -->|No| FAILED["Payment Failed"]
    FAILED --> RELEASE["Release Inventory"]
```

## 1. Create Payment Order

```text
Frontend
   │
   ▼
Backend
   │
   ▼
Validate Booking
   │
   ▼
Razorpay
```

## 2. Razorpay Order

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as Backend
    participant R as Razorpay

    F->>B: Create Payment Order
    B->>B: Validate Booking
    B->>R: Create Order
    R-->>B: Razorpay Order
    B-->>F: Order Details
```

## 3. Checkout

```text
User
  │
  ▼
Frontend
  │
  ▼
Razorpay Checkout
  │
  ▼
Payment Result
```

## 4. Payment Verification

The frontend sends payment information to the backend.

The verification process validates:

```text
Payment ID
Order ID
Signature
Amount
Payment Status
```

## 🔐 Verification Flow

```mermaid
sequenceDiagram
    participant F as Frontend
    participant B as Backend
    participant R as Razorpay
    participant DB as PostgreSQL

    F->>B: Payment ID + Order ID + Signature
    B->>R: Verify Payment
    R-->>B: Payment Details
    B->>B: Validate Signature
    B->>B: Validate Order
    B->>B: Validate Amount
    B->>B: Validate Status
    B->>DB: Update Payment
    B-->>F: Verification Result
```

## ✅ Successful Payment

```text
Payment
pending
   ↓
completed

Booking
payment_pending
   ↓
Booked
```

## ❌ Failed Payment

```text
Payment
pending
   ↓
failed
```

## ⏳ Payment Expiration

Supported payment state:

```text
expired
```

## 💰 Refund State

Supported payment state:

```text
refund
```

## 🧾 Payment States

```mermaid
stateDiagram-v2
    [*] --> pending
    pending --> completed
    pending --> failed
    pending --> expired
    completed --> refund
```

## 🔗 Related Documentation

- [Booking Flow](BOOKING-FLOW.md)
- [Architecture](ARCHITECTURE.md)
- [Database](DATABASE.md)
- [Deployment](DEPLOYMENT.md)
