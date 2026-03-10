# MyPolicy Backend

> Enterprise microservices platform for aggregating insurance policies from multiple insurers, with metadata-driven transformation, identity stitching, and a unified portfolio view.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.1.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![MongoDB](https://img.shields.io/badge/MongoDB-Atlas-green.svg)](https://www.mongodb.com/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://docs.docker.com/compose/)

---

## Table of Contents

- [Overview](#overview)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Docker](#docker)
- [Services](#services)
- [APIs](#apis)
- [Frontend & Login](#frontend--login)
- [Technology Stack](#technology-stack)
- [Project Structure](#project-structure)
- [Contributing](#contributing)

---

## Overview

MyPolicy Backend provides:

- **Centralized config** and **Eureka**-based service discovery
- **Customer**, **Policy**, and **Data-pipeline** services (MongoDB Atlas)
- **BFF** that aggregates customer, policies, and portfolio in one place
- **Web UI** for login and portfolio (served by BFF)
- **Metadata-driven** ingestion and stitching (PAN, Mobile+DOB, Email+DOB)
- **Docker Compose** to run the full stack

---

## Architecture

```
                    ┌─────────────────┐
                    │  Config (8888)  │
                    └────────┬────────┘
                             │
                    ┌────────▼────────┐
                    │ Eureka (8761)   │
                    └────────┬────────┘
                             │
    Browser ──► BFF (8090) ───┼──► Customer (8081)
                    │        ├──► Policy (8085)
                    │        └──► Data-pipeline (8082)
                    │                    │
                    └────────────────────┴──► MongoDB Atlas
```

| Service | Port | Role |
|--------|------|------|
| **config-service** | 8888 | Spring Cloud Config (native backend) |
| **discovery-service** | 8761 | Eureka server |
| **customer-service** | 8081 | Customer details, JWT auth |
| **policy-service** | 8085 | Policy storage & retrieval |
| **data-pipeline-service** | 8082 | Ingestion, metadata, processing, matching, portfolio API |
| **bff-service** | 8090 | API gateway, login, portfolio aggregation, static UI |

**Startup order:** Config → Discovery → Customer, Policy, Data-pipeline → BFF.

---

## Prerequisites

- **Java 17+**
- **Maven 3.8+**
- **MongoDB Atlas** (URIs in config; no local MongoDB required)
- **Docker & Docker Compose** (optional, for full stack in containers)

---

## Quick Start

### 1. Start infrastructure and backend (in order)

```powershell
# From repo root: POLICY-AGGRGATE/MyPolicy-Backend

# 1. Config (port 8888)
cd config-service; mvn spring-boot:run

# 2. Discovery (port 8761) — new terminal
cd discovery-service; mvn spring-boot:run

# 3. Customer, Policy, Data-pipeline — one per terminal
cd customer-service; mvn spring-boot:run
cd policy-service; mvn spring-boot:run
cd data-pipeline-service; mvn spring-boot:run

# 4. BFF (port 8090)
cd bff-service; mvn spring-boot:run "-Dspring-boot.run.arguments=--server.port=8090"
```

On Windows PowerShell, use `;` between commands if running in one line, e.g.:

```powershell
Set-Location config-service; mvn spring-boot:run
```

### 2. Verify

```powershell
.\verify-services.ps1
```

### 3. Open the app

- **Login & portfolio UI:** [http://localhost:8090](http://localhost:8090)
- **Eureka dashboard:** [http://localhost:8761](http://localhost:8761)
- **Config server:** [http://localhost:8888](http://localhost:8888) (e.g. `http://localhost:8888/customer-service/default`)

---

## Docker

Run the whole stack with Docker Compose:

```bash
docker compose up --build -d
```

- **Config** is mapped to host port **8889** (to avoid conflict with a local config-service on 8888).  
  From the host: [http://localhost:8889](http://localhost:8889).  
  Inside the network, services still use `config-service:8888`.
- **BFF (UI):** [http://localhost:8090](http://localhost:8090)
- **Eureka:** [http://localhost:8761](http://localhost:8761)

| Service | Host Port | Container Port |
|---------|-----------|----------------|
| Config | 8889 | 8888 |
| Discovery | 8761 | 8761 |
| Customer | 8081 | 8081 |
| Policy | 8085 | 8085 |
| Data-pipeline | 8082 | 8082 |
| BFF | 8090 | 8090 |

Useful commands:

```bash
docker compose logs -f
docker compose down
docker compose ps
```

Override MongoDB (e.g. for another environment) via env in `docker-compose.yml`: `SPRING_DATA_MONGODB_URI`, `MONGODB_URI`.

---

## Services

### config-service (8888)

- Spring Cloud Config Server, native profile.
- Config repo: `config-service/src/main/resources/config-repo/`.
- Other services use `spring.config.import=optional:configserver:http://admin:config123@<config-host>:8888`.

### discovery-service (8761)

- Eureka server; other services register here and resolve each other by name.

### customer-service (8081)

- Customer details and JWT auth.
- APIs under `/api/v1/customers/`; `/api/v1/customers/details/**` allowed for BFF login flow.

### policy-service (8085)

- Policy CRUD and retrieval.
- Health: `GET /api/v1/health`.

### data-pipeline-service (8082)

- Ingestion, metadata-driven processing, matching (PAN → Mobile+DOB → Email+DOB), unified portfolio.
- Key APIs: portfolio `GET /api/portfolio/{customerId}`, ingestion/upload as per config.

### bff-service (8090)

- Serves static login/portfolio UI and exposes BFF APIs (login, portfolio).
- Feign clients call customer, policy, and data-pipeline (by Eureka name or direct URL in config).

---

## APIs

Base URL for BFF: **http://localhost:8090**.

### Login

```http
POST /api/bff/auth/login
Content-Type: application/json

{
  "customerIdOrUserId": "Amit Ramesh Kulkarni",
  "password": "AKCPK1123L"
}
```

- **User ID** = full name (e.g. from `customerFullName`).
- **Password** = PAN (e.g. from `refCustItNum` / PAN in datasets).

### Portfolio (aggregated)

```http
GET /api/bff/portfolio/{customerId}
Authorization: Bearer <JWT>
```

Returns customer info plus all policies and aggregated totals (e.g. total premium, total coverage).

### Health

- BFF: `GET /actuator/health`
- Config: `GET /actuator/health`
- Discovery: `GET /actuator/health`
- Customer: `GET /actuator/health`
- Policy: `GET /api/v1/health`
- Data-pipeline: `GET /actuator/health` or `GET /api/portfolio/1` for a quick check

---

## Frontend & Login

The BFF serves a simple web UI at [http://localhost:8090](http://localhost:8090):

- **Login:** User ID = full name, Password = PAN (from customer_details).
- **Sample test credentials:**

| User ID (Full Name)      | Password (PAN) |
|--------------------------|----------------|
| Amit Ramesh Kulkarni     | AKCPK1123L     |
| Sneha Prakash Patil      | BPQPP2345M     |
| Rahul Sanjay Deshmukh    | CDEPD3456N     |

After login, the dashboard shows portfolio summary and policies.

---

## Technology Stack

- **Java 17**, **Spring Boot 3.1.x**, **Maven**
- **Spring Cloud:** Config, Eureka, OpenFeign
- **Spring Security**, **JWT** (jjwt)
- **MongoDB** (Atlas; no PostgreSQL in current setup)
- **Data-pipeline:** metadata-driven mapping, PII encryption, coverage advisory

---

## Project Structure

```
MyPolicy-Backend/
├── config-service/           # Config server (8888)
│   └── src/main/resources/config-repo/
├── discovery-service/        # Eureka (8761)
├── customer-service/         # Customers & auth (8081)
├── policy-service/          # Policies (8085)
├── data-pipeline-service/   # Ingestion, processing, portfolio (8082)
│   └── Datasets/             # Sample CSVs (optional)
├── bff-service/              # BFF + static UI (8090)
│   └── src/main/resources/static/
├── docker-compose.yml
├── verify-services.ps1
└── README.md
```

---

## Contributing

1. Fork the repository.
2. Create a feature branch (`git checkout -b feature/your-feature`).
3. Commit changes (`git commit -m 'Add your feature'`).
4. Push to the branch (`git push origin feature/your-feature`).
5. Open a Pull Request.

---

## License

This project is licensed under the MIT License.
