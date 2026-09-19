# 📨 BookMyStay — Event-Driven Architecture

## 📌 Overview

BookMyStay uses **Apache Kafka** for asynchronous event-driven communication.

The main documented use case is communication between BookMyStay Core and the dedicated Email Service.

## 🏗️ Event Architecture

```mermaid
flowchart LR
    CORE["BookMyStay Core"]
    EVENT["Application Event"]
    KAFKA{{"Apache Kafka"}}
    EMAIL["Email Service"]
    SMTP["SMTP / Mail Provider"]
    CORE --> EVENT
    EVENT --> KAFKA
    KAFKA --> EMAIL
    EMAIL --> SMTP
```

## 🧩 Shared Event Module

The repository contains a `common-events` module.

```mermaid
flowchart LR
    COMMON["common-events"]
    CORE["BookMyStay Core"]
    EMAIL["Email Service"]
    COMMON --> CORE
    COMMON --> EMAIL
```

## 📧 Email Processing Flow

```mermaid
sequenceDiagram
    participant B as BookMyStay Core
    participant K as Kafka
    participant E as Email Service
    participant S as SMTP Provider
    participant U as User

    B->>K: Publish Event
    K->>E: Deliver Event
    E->>E: Process Event
    E->>S: Send Email
    S-->>U: Email Notification
```

## 🔄 Event Processing

```text
Synchronous Request
       │
       ▼
BookMyStay Core
       │
       ▼
Publish Event
       │
       ▼
Kafka
       │
       ▼
Email Service
       │
       ▼
Email Provider
```

## 🧱 Services

### BookMyStay Core

- Main business logic
- REST APIs
- Authentication
- Booking
- Inventory
- Payment
- Event publishing

### common-events

- Shared event definitions
- Shared event contracts

### Email Service

- Kafka event consumption
- Event processing
- Email creation
- JavaMailSender
- SMTP communication

## 📦 Repository Structure

```text
BookMyStay/
├── bookmystay-core/
├── common-events/
└── email-service/
    ├── src/
    ├── Dockerfile
    └── pom.xml
```

## 🐳 Service Deployment

```mermaid
flowchart TB
    CORE["BookMyStay Core Container"]
    KAFKA["Kafka"]
    EMAIL["Email Service Container"]
    SMTP["SMTP Provider"]
    CORE --> KAFKA
    KAFKA --> EMAIL
    EMAIL --> SMTP
```

## 🔐 Configuration

Kafka and SMTP configuration should be supplied through environment-specific configuration.

Never commit:

```text
SMTP passwords
SMTP API keys
Kafka credentials
Production secrets
```

## 🔗 Related Documentation

- [Architecture](ARCHITECTURE.md)
- [Deployment](DEPLOYMENT.md)
- [Database](DATABASE.md)
- [Booking Flow](BOOKING-FLOW.md)
