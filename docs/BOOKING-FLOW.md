# 🔄 BookMyStay — Booking Flow

## 📌 Overview

The booking flow describes how a user moves from selecting a hotel and room type to creating a booking, reserving inventory, adding guests, and completing payment.

---

## 🧭 Complete Booking Flow

```mermaid
sequenceDiagram

    participant U as User
    participant F as Frontend
    participant B as Backend
    participant DB as PostgreSQL
    participant R as Razorpay

    U->>F: Search Hotel
    F->>B: Search Request
    B-->>F: Available Hotels

    U->>F: Select Hotel
    U->>F: Select Room Type

    F->>B: Create Booking Request
    B->>DB: Validate Inventory
    DB-->>B: Availability

    B->>DB: Create Booking
    B->>DB: Reserve Inventory
    DB-->>B: Booking Created

    B-->>F: Booking Details

    U->>F: Add Guest Details
    F->>B: Guest Information
    B->>DB: Save Guests

    U->>F: Start Payment
    F->>B: Create Payment Order
    B->>R: Create Razorpay Order
    R-->>B: Razorpay Order
    B-->>F: Payment Order

    F->>R: Open Checkout
    R-->>F: Payment Response

    F->>B: Verify Payment
    B->>R: Verify Payment
    R-->>B: Payment Status

    B->>DB: Update Payment
    B->>DB: Confirm Booking

    B-->>F: Booking Confirmed
```

---

## 🔎 1. Hotel Selection

The user searches for available hotels using:

```text
City
Check-in Date
Check-out Date
Guests
```

The frontend sends the search request to the backend and receives the available hotel results.

The user then selects:

```text
Hotel
Room Type
Number of Guests
```

---

## 📦 2. Inventory Validation

Before creating the booking, the backend checks the inventory for the requested room type and dates.

Availability is calculated as:

```text
Available
=
Total Count
-
Book Count
-
Reserved Count
```

The booking can proceed only when the requested inventory is available.

---

## 📝 3. Booking Creation

After successful inventory validation:

```text
Booking Request
       │
       ▼
Inventory Validation
       │
       ▼
Booking Created
       │
       ▼
Inventory Reserved
       │
       ▼
Payment Pending
```

The booking is initially maintained in a payment-pending state while the reserved inventory is held.

---

## 👥 4. Guest Information

Guest details are associated with the booking.

```mermaid
flowchart LR

    BOOKING["Booking"]

    GUEST["Guest Details"]

    BOOKING --> GUEST
```

The guest information is saved against the booking.

---

## 💳 5. Payment

Once the booking has been created, the frontend starts the payment process.

```mermaid
sequenceDiagram

    participant F as Frontend
    participant B as Backend
    participant R as Razorpay

    F->>B: Create Payment Order
    B->>R: Create Razorpay Order
    R-->>B: Razorpay Order
    B-->>F: Payment Order

    F->>R: Open Checkout
    R-->>F: Payment Response

    F->>B: Payment Verification
    B->>R: Verify Payment
    R-->>B: Payment Status
```

---

## ✅ 6. Successful Booking

After successful payment verification:

```text
Payment
pending
   │
   ▼
completed

Booking
payment_pending
   │
   ▼
Booked
```

The reserved inventory becomes booked inventory.

---

## ❌ 7. Failed Payment

If payment verification fails:

```text
Payment Pending
       │
       ▼
Payment Failed
       │
       ▼
Booking Cancelled / Expired
       │
       ▼
Reserved Inventory Released
```

---

## ⏱️ 8. Booking Expiration

Payment-pending bookings are checked for expiration.

```mermaid
flowchart TD

    PENDING["Payment Pending Booking"]

    CHECK["Expiration Check"]

    EXPIRED{"Expired?"}

    RELEASE["Release Reserved Inventory"]

    PENDING --> CHECK
    CHECK --> EXPIRED

    EXPIRED -->|No| PENDING
    EXPIRED -->|Yes| RELEASE
```

---

## 📦 9. Inventory Lifecycle

The booking-related inventory lifecycle is:

```mermaid
stateDiagram-v2

    [*] --> Available

    Available --> Reserved: Booking Created

    Reserved --> Booked: Payment Successful

    Reserved --> Available: Payment Failed

    Reserved --> Available: Booking Expired

    Booked --> [*]
```

---

## 🔄 Booking State Lifecycle

```mermaid
stateDiagram-v2

    [*] --> payment_pending

    payment_pending --> Booked: Payment Successful

    payment_pending --> Cancelled: Payment Failed

    payment_pending --> Cancelled: Booking Expired

    Booked --> Cancelled: Cancellation
```

---

## 🔗 Related Documentation

- [Database](DATABASE.md) — Booking, inventory and payment data model
- [Payment Flow](PAYMENT-FLOW.md) — Detailed Razorpay payment verification
- [Architecture](ARCHITECTURE.md) — Overall system and backend architecture
