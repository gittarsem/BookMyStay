# 🏗️ BookMyStay — System Architecture

> **Scope:** This document explains how the BookMyStay system is structured, how requests move through the backend layers, and how the core application connects to supporting infrastructure.
>
> Detailed implementation flows are intentionally kept in their respective documents. See the links at the end of this file.

---

## 1. Architecture at a Glance

BookMyStay follows a **layered Spring Boot architecture** inside the main backend, with selected supporting services and infrastructure separated around it.

The main request path is:

```text
React Frontend
      │
      ▼
Spring Security + JWT
      │
      ▼
Controller
      │
      ▼
Service
      │
      ▼
Repository
      │
      ▼
Entity / PostgreSQL
```

The service layer can also communicate with infrastructure that serves specialized purposes:

```text
                         ┌──► Redis
                         │
                         ├──► Elasticsearch
                         │
Service Layer ───────────┼──► Razorpay
                         │
                         ├──► Cloudinary
                         │
                         └──► Kafka
                                  │
                                  ▼
                            Email Service
```

This separation keeps **business logic inside the application layer** while specialized infrastructure handles caching, search, payments, media, and asynchronous communication.

---

# 2. High-Level System Architecture

```mermaid
flowchart TB

    USER["👤 Users<br/>Guest / Owner / Admin"]

    FRONTEND["🌐 React + TypeScript<br/>Frontend"]

    USER --> FRONTEND

    FRONTEND -->|"HTTPS / REST"| SECURITY

    subgraph CORE["☕ BookMyStay Core — Spring Boot"]

        SECURITY["🔐 Spring Security + JWT"]

        CONTROLLER["🎯 Controller Layer"]

        SERVICE["⚙️ Service Layer"]

        REPOSITORY["🗃️ Repository Layer"]

        ENTITY["📦 Entity / Persistence Layer"]

        SECURITY --> CONTROLLER
        CONTROLLER --> SERVICE
        SERVICE --> REPOSITORY
        REPOSITORY --> ENTITY

        SCHEDULER["⏰ Scheduler"]
        SCHEDULER --> SERVICE

        PRODUCER["📨 Kafka Producer"]
        SERVICE --> PRODUCER

    end

    ENTITY -->|"JPA / Hibernate"| POSTGRES["🗄️ Supabase PostgreSQL"]

    SERVICE --> REDIS["⚡ Upstash Redis"]

    SERVICE --> SEARCH["🔎 Elasticsearch"]

    SERVICE --> RAZORPAY["💳 Razorpay"]

    SERVICE --> CLOUDINARY["☁️ Cloudinary"]

    PRODUCER --> KAFKA["📨 Aiven Kafka"]

    KAFKA --> EMAIL["📧 Email Service"]

    EMAIL --> SMTP["✉️ SMTP Provider"]
```

### Architectural boundary

The diagram can be divided into three logical areas:

| Area | Responsibility |
|---|---|
| **Client** | User interaction and presentation |
| **BookMyStay Core** | Authentication, authorization, business rules and persistence orchestration |
| **Supporting infrastructure** | Specialized capabilities such as caching, search, payments, messaging and email |

---

# 3. Backend Layered Architecture

The core backend follows a conventional layered architecture.

```mermaid
flowchart TD

    REQUEST["Incoming HTTP Request"]

    SECURITY["Security Layer<br/>Spring Security + JWT"]

    CONTROLLER["Controller Layer<br/>HTTP / REST"]

    SERVICE["Service Layer<br/>Business Logic"]

    REPOSITORY["Repository Layer<br/>Data Access"]

    ENTITY["Entity Layer<br/>JPA Domain Model"]

    DATABASE["PostgreSQL"]

    RESPONSE["HTTP Response"]

    REQUEST --> SECURITY
    SECURITY --> CONTROLLER
    CONTROLLER --> SERVICE
    SERVICE --> REPOSITORY
    REPOSITORY --> ENTITY
    ENTITY --> DATABASE

    DATABASE --> ENTITY
    ENTITY --> REPOSITORY
    REPOSITORY --> SERVICE
    SERVICE --> CONTROLLER
    CONTROLLER --> RESPONSE
```

## Responsibility of each layer

### Security Layer

Responsible for:

- JWT authentication
- Request authentication
- Role-based authorization
- Protecting secured endpoints

The frontend does not directly determine whether an operation is authorized. The backend enforces authorization.

### Controller Layer

Responsible for:

- Receiving HTTP requests
- Validating request-level input
- Calling the appropriate service
- Returning HTTP responses

Controllers should not contain the application's core business rules.

### Service Layer

Responsible for:

- Business rules
- Booking logic
- Inventory operations
- Payment orchestration
- Owner/admin operations
- Review rules
- Coordination between repositories and external services

This is the primary application/business layer.

### Repository Layer

Responsible for:

- Database access
- JPA queries
- Persistence operations
- Retrieving domain data

### Entity Layer

Represents the persistent domain model used by the application and Hibernate/JPA.

---

# 4. Request Lifecycle

A normal synchronous API request follows this path:

```mermaid
sequenceDiagram

    participant C as Client
    participant S as Spring Security
    participant CT as Controller
    participant SV as Service
    participant RP as Repository
    participant DB as PostgreSQL

    C->>S: HTTP Request + JWT
    S->>S: Authenticate + Authorize
    S->>CT: Authorized Request

    CT->>SV: Method Call
    SV->>RP: Data Access
    RP->>DB: Query
    DB-->>RP: Result
    RP-->>SV: Domain Data
    SV-->>CT: Business Result
    CT-->>C: HTTP Response
```

This is the **default application flow**.

External infrastructure is invoked by the service layer only when the use case requires it.

---

# 5. Application Package Structure

The current backend separates responsibilities into packages such as:

```text
com.tarsem.BookMyStay
│
├── Config/
├── Controller/
├── Entity/
├── Enums/
├── Exceptions/
├── Repositroy/
├── Scheduler/
├── Security/
├── Service/
├── Strategy/
├── Utils/
├── constants/
├── document/
├── dto/
└── producer/
```

Conceptually:

```mermaid
flowchart LR

    CONTROLLER["Controller"]
    DTO["DTO"]
    SERVICE["Service"]
    STRATEGY["Strategy"]
    REPOSITORY["Repositroy"]
    ENTITY["Entity"]
    SECURITY["Security"]
    PRODUCER["Producer"]
    SCHEDULER["Scheduler"]
    DOCUMENT["Elasticsearch Document"]

    CONTROLLER --> DTO
    CONTROLLER --> SERVICE

    SERVICE --> STRATEGY
    SERVICE --> REPOSITORY
    SERVICE --> PRODUCER

    REPOSITORY --> ENTITY

    SECURITY -. "cross-cutting concern" .-> CONTROLLER
    SCHEDULER --> SERVICE
    SERVICE --> DOCUMENT
```

> Package names above follow the current repository structure. `Repositroy` is retained because that is the package name currently present in the project.

---

# 6. Controller → Service → Repository Flow

A typical business operation follows:

```text
HTTP Request
     │
     ▼
Controller
     │
     │  request DTO
     ▼
Service
     │
     ├── validate business rules
     ├── coordinate operations
     ├── call external services when required
     │
     ▼
Repository
     │
     ▼
PostgreSQL
```

The important design rule is:

> **Controllers handle HTTP concerns; services handle business concerns; repositories handle persistence concerns.**

This makes individual responsibilities easier to test, maintain and extend.

---

# 7. Infrastructure Integration

The service layer acts as the application boundary for specialized infrastructure.

```mermaid
flowchart LR

    SERVICE["⚙️ Service Layer"]

    REDIS["Redis<br/>Caching"]
    ES["Elasticsearch<br/>Search"]
    RAZORPAY["Razorpay<br/>Payments"]
    CLOUDINARY["Cloudinary<br/>Media"]
    KAFKA["Kafka<br/>Events"]
    POSTGRES["PostgreSQL<br/>Transactions"]

    SERVICE --> POSTGRES
    SERVICE --> REDIS
    SERVICE --> ES
    SERVICE --> RAZORPAY
    SERVICE --> CLOUDINARY
    SERVICE --> KAFKA
```

Each system has one primary responsibility:

| Component | Architectural Responsibility |
|---|---|
| PostgreSQL | Transactional persistence |
| Redis | Cache |
| Elasticsearch | Search index |
| Razorpay | Payment gateway |
| Cloudinary | Media storage |
| Kafka | Asynchronous event transport |

Detailed infrastructure and hosting information belongs in [`DEPLOYMENT.md`](./DEPLOYMENT.md).

---

# 8. PostgreSQL — System of Record

PostgreSQL is the primary persistence layer.

```mermaid
flowchart LR

    SERVICE["Service Layer"]

    REPOSITORY["Repository Layer"]

    JPA["JPA / Hibernate"]

    DB["Supabase PostgreSQL"]

    SERVICE --> REPOSITORY
    REPOSITORY --> JPA
    JPA --> DB
```

Transactional application data remains in PostgreSQL.

The architecture therefore treats PostgreSQL as the **source of truth for transactional state**, while other systems provide specialized capabilities.

Database entities and relationships are documented separately in [`DATABASE.md`](./DATABASE.md).

---

# 9. Redis — Cache Layer

Redis sits beside the primary database rather than replacing it.

```mermaid
flowchart LR

    SERVICE["Service Layer"]

    CACHE["Redis"]

    DB["PostgreSQL"]

    SERVICE --> CACHE

    CACHE -->|"Cache Hit"| SERVICE
    CACHE -->|"Cache Miss"| DB
    DB --> SERVICE
```

Conceptually this follows a cache-aside pattern:

```text
Request
   │
   ▼
Check Redis
   │
   ├── Hit ──► Return cached data
   │
   └── Miss
          │
          ▼
      PostgreSQL
          │
          ▼
      Cache result
```

Redis deployment details are documented in [`DEPLOYMENT.md`](./DEPLOYMENT.md).

---

# 10. Elasticsearch — Search Boundary

Elasticsearch is separated from transactional persistence.

```mermaid
flowchart LR

    CLIENT["Frontend"]

    CONTROLLER["Browse / Search Controller"]

    SERVICE["Search / Hotel Service"]

    ES["Elasticsearch"]

    CLIENT --> CONTROLLER
    CONTROLLER --> SERVICE
    SERVICE --> ES
    ES --> SERVICE
    SERVICE --> CONTROLLER
    CONTROLLER --> CLIENT
```

The important architectural distinction is:

```text
PostgreSQL
    │
    └── Transactional application data

Elasticsearch
    │
    └── Search-oriented representation
```

Elasticsearch therefore does not replace PostgreSQL as the transactional database.

Search implementation details belong in the relevant search/database documentation.

---

# 11. Payment Integration Boundary

Razorpay is an external system.

```mermaid
flowchart LR

    CONTROLLER["Payment Controller"]

    SERVICE["Payment Service"]

    RAZORPAY["Razorpay"]

    DB["PostgreSQL"]

    CONTROLLER --> SERVICE

    SERVICE -->|"Create / Verify"| RAZORPAY

    SERVICE --> DB

    RAZORPAY --> SERVICE
```

The application owns the booking/payment state, while Razorpay handles payment processing.

The detailed payment lifecycle is documented in [`PAYMENT-FLOW.md`](./PAYMENT-FLOW.md).

---

# 12. Event-Driven Boundary

Kafka is used where the operation can be processed asynchronously.

```mermaid
flowchart LR

    SERVICE["BookMyStay Service"]

    PRODUCER["KafkaProducerService"]

    KAFKA["Aiven Kafka"]

    EMAIL["Email Service"]

    SMTP["SMTP"]

    SERVICE --> PRODUCER
    PRODUCER --> KAFKA
    KAFKA --> EMAIL
    EMAIL --> SMTP
```

The architectural flow is:

```text
Business Operation
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
SMTP
```

This keeps email delivery outside the synchronous request path.

The complete event architecture is documented in [`EVENT-DRIVEN-ARCHITECTURE.md`](./EVENT-DRIVEN-ARCHITECTURE.md).

---

# 13. Email Service Boundary

The Email Service is a separate Spring Boot application.

```mermaid
flowchart LR

    CORE["BookMyStay Core"]

    KAFKA["Kafka"]

    CONSUMER["Email Service<br/>Kafka Consumer"]

    MAIL["JavaMailSender"]

    SMTP["SMTP Provider"]

    CORE --> KAFKA
    KAFKA --> CONSUMER
    CONSUMER --> MAIL
    MAIL --> SMTP
```

The core application does not need to perform SMTP delivery during the main API request.

This creates a clear boundary:

```text
BookMyStay Core
      │
      │ event
      ▼
    Kafka
      │
      ▼
 Email Service
      │
      ▼
    SMTP
```

See [`EVENT-DRIVEN-ARCHITECTURE.md`](./EVENT-DRIVEN-ARCHITECTURE.md) for topics, consumers and event flows.

---

# 14. Scheduler Boundary

Scheduled processing is part of the application layer.

```mermaid
flowchart LR

    SCHEDULER["Scheduler"]

    SERVICE["Service Layer"]

    DB["PostgreSQL"]

    SCHEDULER --> SERVICE
    SERVICE --> DB
```

The scheduler triggers application operations rather than directly manipulating the database.

For example, expiration-related processing can follow:

```text
Scheduler
   │
   ▼
Service
   │
   ├── identify expired state
   ├── update booking state
   └── release related inventory
```

The detailed booking lifecycle is documented in [`BOOKING-FLOW.md`](./BOOKING-FLOW.md).

---

# 15. Cross-Cutting Concerns

Some components do not belong to a single request layer.

```mermaid
flowchart LR

    SECURITY["Security"]
    CONFIG["Configuration"]
    EXCEPTION["Exception Handling"]
    UTILS["Utilities"]

    CONTROLLER["Controller"]
    SERVICE["Service"]
    REPOSITORY["Repository"]

    SECURITY -.-> CONTROLLER
    SECURITY -.-> SERVICE

    CONFIG -.-> CONTROLLER
    CONFIG -.-> SERVICE
    CONFIG -.-> REPOSITORY

    EXCEPTION -.-> CONTROLLER
    EXCEPTION -.-> SERVICE
    EXCEPTION -.-> REPOSITORY

    UTILS -.-> SERVICE
    UTILS -.-> REPOSITORY
```

These concerns support the application without becoming part of the primary business-flow chain.

---

# 16. End-to-End Architecture Flow

This is the complete conceptual flow without exposing implementation details that belong in other documents.

```mermaid
flowchart TD

    USER["👤 User"]

    FRONTEND["React Frontend"]

    SECURITY["Spring Security + JWT"]

    CONTROLLER["Controller"]

    SERVICE["Service / Business Logic"]

    REPOSITORY["Repository"]

    POSTGRES["PostgreSQL"]

    REDIS["Redis"]

    ELASTIC["Elasticsearch"]

    RAZORPAY["Razorpay"]

    CLOUDINARY["Cloudinary"]

    KAFKA["Kafka"]

    EMAIL["Email Service"]

    SMTP["SMTP"]

    USER --> FRONTEND
    FRONTEND --> SECURITY
    SECURITY --> CONTROLLER
    CONTROLLER --> SERVICE

    SERVICE --> REPOSITORY
    REPOSITORY --> POSTGRES

    SERVICE --> REDIS
    SERVICE --> ELASTIC
    SERVICE --> RAZORPAY
    SERVICE --> CLOUDINARY
    SERVICE --> KAFKA

    KAFKA --> EMAIL
    EMAIL --> SMTP
```

---

# 17. Architectural Communication Rules

The main communication boundaries can be summarized as:

```text
Frontend
   │
   │ REST / HTTPS
   ▼
Controller
   │
   │ method calls
   ▼
Service
   │
   ├──► Repository ──► PostgreSQL
   │
   ├──► Redis
   │
   ├──► Elasticsearch
   │
   ├──► Razorpay
   │
   ├──► Cloudinary
   │
   └──► Kafka ──► Email Service ──► SMTP
```

### Key rule

**Business logic stays in the Service layer.**

The surrounding systems are specialized dependencies rather than replacements for the service layer.

---

# 18. Architecture Decisions

| Decision | Reason |
|---|---|
| Layered backend | Separates HTTP, business and persistence responsibilities |
| PostgreSQL for transactions | Reliable relational persistence |
| Redis beside PostgreSQL | Fast access without replacing persistent storage |
| Elasticsearch for search | Separates search workloads from transactional queries |
| Kafka for asynchronous events | Decouples background consumers from API requests |
| Separate Email Service | Isolates notification delivery |
| JWT + Spring Security | Stateless API authentication and role-based authorization |
| Dockerized services | Consistent application packaging |
| Managed infrastructure | Separates application deployment from infrastructure operations |

Deployment-specific provider choices are documented in [`DEPLOYMENT.md`](./DEPLOYMENT.md).

---

# 19. Where to Read Next

This document intentionally does **not** duplicate detailed implementation flows.

| Topic | Documentation |
|---|---|
| Database entities and relationships | [`DATABASE.md`](./DATABASE.md) |
| Booking lifecycle | [`BOOKING-FLOW.md`](./BOOKING-FLOW.md) |
| Razorpay payment lifecycle | [`PAYMENT-FLOW.md`](./PAYMENT-FLOW.md) |
| Kafka and Email Service | [`EVENT-DRIVEN-ARCHITECTURE.md`](./EVENT-DRIVEN-ARCHITECTURE.md) |
| Production infrastructure and hosting | [`DEPLOYMENT.md`](./DEPLOYMENT.md) |
| Application pages/features | [`PAGES.md`](./PAGES.md) |
| Demo accounts | [`DEMO.md`](./DEMO.md) |

---

## 20. Interview Summary

A concise explanation of the architecture is:

> **BookMyStay uses a layered Spring Boot architecture. Requests enter through Spring Security and the controller layer, business rules are handled by services, and persistence is handled through Spring Data repositories and PostgreSQL. Redis provides caching and Elasticsearch handles search workloads. External integrations such as Razorpay and Cloudinary are accessed from the application layer. Kafka is used for asynchronous events, with a separate Email Service consuming those events and delivering notifications through SMTP.**

The architecture can therefore be remembered as:

```text
              ┌───────────────┐
              │ React Frontend│
              └───────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │ Security/JWT  │
              └───────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │  Controller   │
              └───────┬───────┘
                      │
                      ▼
              ┌───────────────┐
              │    Service    │
              └───────┬───────┘
                      │
             ┌────────┼────────┐
             ▼        ▼        ▼
        Repository  Redis   Search
             │               │
             ▼               ▼
        PostgreSQL      Elasticsearch

                 Service
                    │
          ┌─────────┼─────────┐
          ▼         ▼         ▼
      Razorpay  Cloudinary  Kafka
                              │
                              ▼
                        Email Service
                              │
                              ▼
                             SMTP
```
