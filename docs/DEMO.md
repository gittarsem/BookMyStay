# 🔑 BookMyStay — Demo & Testing

## 📌 Overview

This document is intended for demo accounts and test credentials used to demonstrate BookMyStay.

> ⚠️ Do not place production credentials, API keys or payment secrets in this file.

## 👤 Guest Account

```text
Email: tarsemgulab2006@gmail.com
Password: @tarsem006
Role: USER
```

## 🏨 Owner Account

```text
Email: owner@bookmystay.com
Password: @tarsem006
Role: OWNER
```

## 🛡️ Admin Account

```text
Email: admin.tg@bookmystay.com
Password: admin@tg
Role: ADMIN
```

## 💳 Payment Testing

Use Razorpay's test environment for demonstration.

Do not use real payment credentials for development testing.

## 🧪 Guest Demo Flow

```mermaid
flowchart LR
    LOGIN["Login"]
    SEARCH["Search Hotels"]
    HOTEL["Open Hotel"]
    ROOM["Select Room"]
    BOOKING["Create Booking"]
    GUEST["Add Guests"]
    PAYMENT["Make Payment"]
    CONFIRM["Booking Confirmation"]

    LOGIN --> SEARCH
    SEARCH --> HOTEL
    HOTEL --> ROOM
    ROOM --> BOOKING
    BOOKING --> GUEST
    GUEST --> PAYMENT
    PAYMENT --> CONFIRM
```

## 🏨 Owner Demo Flow

```mermaid
flowchart LR
    LOGIN["Owner Login"]
    DASHBOARD["Owner Dashboard"]
    HOTELS["Manage Hotels"]
    ROOMS["Manage Rooms"]
    INVENTORY["Manage Inventory"]
    BOOKINGS["Manage Bookings"]
    REVENUE["View Revenue"]

    LOGIN --> DASHBOARD
    DASHBOARD --> HOTELS
    DASHBOARD --> ROOMS
    DASHBOARD --> INVENTORY
    DASHBOARD --> BOOKINGS
    DASHBOARD --> REVENUE
```

## 🛡️ Admin Demo Flow

```mermaid
flowchart LR
    LOGIN["Admin Login"]
    DASHBOARD["Admin Dashboard"]
    USERS["Manage Users"]
    HOTELS["Manage Hotels"]
    VERIFY["Verification"]

    LOGIN --> DASHBOARD
    DASHBOARD --> USERS
    DASHBOARD --> HOTELS
    DASHBOARD --> VERIFY
```

## 🔒 Credential Safety

Before publishing:

- Replace placeholders with intentionally created demo accounts.
- Do not use personal accounts.
- Do not publish production passwords.
- Do not publish database credentials.
- Do not publish JWT secrets.
- Do not publish Razorpay secret keys.
- Do not publish SMTP passwords.

## 🔗 Related Documentation

- [Architecture](ARCHITECTURE.md)
- [Booking Flow](BOOKING-FLOW.md)
- [Payment Flow](PAYMENT-FLOW.md)
- [Deployment](DEPLOYMENT.md)
