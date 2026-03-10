# MyPolicy – API Contracts

This document provides **full API contracts** for the MyPolicy project:

- **External (QA / frontend / automation)**: BFF APIs (recommended entrypoint)
- **Internal (service-to-service)**: customer-service, policy-service, data-pipeline-service
- **Platform**: config-service (Spring Cloud Config), discovery-service (Eureka)

All paths below are relative to the service base URL.

---

## Environments & Base URLs

### Local (Maven)

| Service | Base URL |
|---|---|
| BFF | `http://localhost:8090` |
| Customer | `http://localhost:8081` |
| Policy | `http://localhost:8085` |
| Data-pipeline | `http://localhost:8082` |
| Eureka | `http://localhost:8761` |
| Config | `http://localhost:8888` |

### Docker Compose (host)

| Service | Base URL |
|---|---|
| Frontend (nginx / Flutter web) | `http://localhost:8080` |
| BFF | `http://localhost:8090` |
| Customer | `http://localhost:8081` |
| Policy | `http://localhost:8085` |
| Data-pipeline | `http://localhost:8082` |
| Eureka | `http://localhost:8761` |
| Config (host-mapped) | `http://localhost:8889` |

---

## Authentication

### JWT (Bearer)

After login, the API returns a `token`. For protected endpoints, send:

```text
Authorization: Bearer <token>
```

---

## 1. BFF Service (`bff-service`) — Recommended for QA automation

The BFF is the single entrypoint for the frontend and for QA automation. It orchestrates calls to downstream services.

### 1.1 `POST /api/bff/auth/login`

**Purpose**: Login using `Full Name + PAN`, returning JWT token and customer summary.

**Request (JSON)**

```json
{
  "customerIdOrUserId": "Amit Ramesh Kulkarni",
  "password": "AKCPK1123L"
}
```

**Response `200 OK` (shape)**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "customer": {
    "customerId": 901120934,
    "fullName": "Amit Ramesh Kulkarni",
    "mobile": "919876543210",
    "email": "amit.kulkarni@gmail.com"
  }
}
```

**Errors**

- `400 Bad Request`: validation errors from downstream services
- `401 Unauthorized`: invalid credentials

---

### 1.2 `GET /api/bff/portfolio/{customerId}`

**Purpose**: Unified portfolio for the given customer:

- customer details via `customer-service`
- policies via `data-pipeline-service` unified portfolio
- totals calculated by BFF

**Headers**

```text
Authorization: Bearer <token>
```

**Response `200 OK` (shape)**

```json
{
  "customer": {
    "customerId": 901120934,
    "fullName": "Amit Ramesh Kulkarni",
    "mobile": "919876543210",
    "email": "amit.kulkarni@gmail.com",
    "pan": "AKCPK1123L"
  },
  "policies": [
    {
      "id": "AUPOL-S001",
      "policyNumber": "AUPOL-S001",
      "policyType": "auto_insurance",
      "insurerId": "HDFC Life",
      "premiumAmount": 18500.0,
      "sumAssured": 500000.0,
      "startDate": "2024-01-01",
      "endDate": "2025-12-31",
      "status": "ACTIVE"
    }
  ],
  "totalPolicies": 4,
  "totalPremium": 62000.0,
  "totalCoverage": 2500000.0
}
```

---

### 1.3 `GET /api/bff/advisory/{customerId}`

**Purpose**: Coverage advisory/gap analysis proxied from `data-pipeline-service`.

**Headers**

```text
Authorization: Bearer <token>
```

---

### 1.4 `GET /api/bff/insights/{customerId}`

**Purpose**: Coverage insights & recommendations computed in BFF.

**Headers**

```text
Authorization: Bearer <token>
```

---

### 1.5 Upload APIs (BFF → ingestion)

#### 1.5.1 `POST /api/bff/upload`

**Purpose**: Upload an insurer file for ingestion.

**Consumes**: `multipart/form-data`

**Form fields**

- `file`: binary
- `uploadedBy`: string
- `insurerId`: string

#### 1.5.2 `GET /api/bff/upload/status/{jobId}`

**Purpose**: Get ingestion job status.

---

### 1.6 Health / sanity endpoints

- `GET /api/bff/health` → JSON status object
- `GET /api/bff/ping` → `"pong"`
- `GET /actuator/health` → Spring actuator health

---

## 2. Data Pipeline Service (`data-pipeline-service`)

Port: **8082**

### 2.1 Portfolio APIs

- `GET /api/portfolio/{customerId}`
- `GET /api/advisory/{customerId}`

### 2.2 Pipeline execution APIs

- `POST /api/pipeline/run`
- `POST /api/pipeline/upload` (multipart `file`, `collectionName`)

### 2.3 Insurer portal (HTML)

- `GET /insurer-portal`

### 2.4 Health

- `GET /actuator/health`

---

## 3. Customer Service (`customer-service`)

Port: **8081**

### 3.1 Customer APIs

- `POST /api/v1/customers/register`
- `POST /api/v1/customers/login`
- `GET /api/v1/customers/{customerId}`
- `GET /api/v1/customers/details/{customerId}`
- `PUT /api/v1/customers/{customerId}`
- `GET /api/v1/customers/search/mobile/{mobile}`
- `GET /api/v1/customers/search/email/{email}`
- `GET /api/v1/customers/search/pan/{pan}`

### 3.2 Login request (JSON)

```json
{
  "customerIdOrUserId": "Amit Ramesh Kulkarni",
  "password": "AKCPK1123L"
}
```

### 3.3 Health / ping

- `GET /api/v1/health`
- `GET /api/v1/ping`
- `GET /` or `GET /health` or `GET /api/health`
- `GET /actuator/health` (may be secured)

---

## 4. Policy Service (`policy-service`)

Port: **8085**

### 4.1 Policy APIs

- `POST /api/v1/policies`
- `GET /api/v1/policies`
- `GET /api/v1/policies/{id}`
- `GET /api/v1/policies/customer/{customerId}`
- `PATCH /api/v1/policies/{id}/status?status=...`
- `DELETE /api/v1/policies/{id}`

### 4.2 Health / ping

- `GET /api/v1/health`
- `GET /api/v1/ping`

---

## 5. Config Service (`config-service`)

Container port **8888** (host-mapped to **8889** in Docker)

- `GET /{app-name}/{profile}` (Spring Cloud Config)
  - Example: `GET /customer-service/default`
- `GET /actuator/health`

---

## 6. Discovery Service (`discovery-service`)

Port: **8761**

- `GET /` (Eureka dashboard)
- `GET /actuator/health`

---

## 7. Frontend (Flutter Web) – API usage

The Flutter app calls **only the BFF**:

- `POST /api/bff/auth/login`
- `GET /api/bff/portfolio/{customerId}`

---

## Generating HTML + PDF

Run:

```bash
python generate_api_contracts_html_and_pdf.py
```

