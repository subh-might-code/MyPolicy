# MyPolicy – API Contracts (Backend & Frontend Usage)

> This document describes the main HTTP APIs used in the MyPolicy project – both
> the public APIs exposed to the frontend and the internal service‑to‑service
> contracts.  
> All paths are relative to the service base URL (or host/port in Docker/Dev).

---

## 1. BFF Service (`bff-service`)

The BFF is the primary entrypoint for the frontend.  
Port: **8090** (dev), behind Docker: `bff-service:8090`.

### 1.1 `POST /api/bff/auth/login`

**Purpose**: Authenticate a customer using `full name + PAN` and return a token and customer summary.

**Request (JSON)**

```json
{
  "customerIdOrUserId": "Amit Ramesh Kulkarni",
  "password": "AKCPK1123L"
}
```

**Response `200 OK`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "customerId": "901120934",
  "fullName": "Amit Ramesh Kulkarni",
  "customer": {
    "customerId": 901120934,
    "fullName": "Amit Ramesh Kulkarni",
    "mobile": "919876543210",
    "email": "amit.kulkarni@gmail.com"
  }
}
```

**Error `401 / 400`**

```json
{
  "timestamp": "2026-03-09T14:01:23.456+05:30",
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid credentials",
  "path": "/api/bff/auth/login"
}
```

Used by **Flutter**: `ApiClient.login(...)`.

---

### 1.2 `GET /api/bff/portfolio/{customerId}`

**Purpose**: Get unified portfolio for a given customer:

- Customer details from `customer-service`
- Unified policies from `data-pipeline-service`
- Aggregated totals (premium, coverage)

`customerId` is the numeric ID from `Customer_data.csv` / `customer_details`.

**Example URL**

```text
GET /api/bff/portfolio/901120934
```

**Response `200 OK`**

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
      "customerId": "901120934",
      "insurerId": "HDFC Life",
      "policyNumber": "AUPOL-S001",
      "policyType": "auto_insurance",
      "planName": null,
      "premiumAmount": 18500.0,
      "sumAssured": 500000.0,
      "startDate": "2024-01-01",
      "endDate": "2025-12-31",
      "status": "ACTIVE"
    },
    {
      "id": "LIPOL-S001",
      "customerId": "901120934",
      "insurerId": "SBI Life",
      "policyNumber": "LIPOL-S001",
      "policyType": "life_insurance",
      "planName": null,
      "premiumAmount": 12500.0,
      "sumAssured": 1000000.0,
      "startDate": "2023-01-01",
      "endDate": "2042-12-31",
      "status": "ACTIVE"
    }
  ],
  "totalPolicies": 4,
  "totalPremium": 62000.0,
  "totalCoverage": 2500000.0
}
```

Used by **Flutter**:

- `DashboardScreen` (list of policies + top summary cards).
- `AnalyticsDashboard` (donut charts and KPIs).
- `PolicyDetailScreen` (detail view for a selected policy).

---

### 1.3 `GET /api/bff/advisory/{customerId}` (used internally / future UI)

**Purpose**: Fetch coverage advisory & gap analysis from `data-pipeline-service`.

**Example URL**

```text
GET /api/bff/advisory/901120934
```

**Response `200 OK` (shape simplified)**

```json
{
  "customerId": 901120934,
  "overallScore": {
    "score": 72,
    "rating": "GOOD"
  },
  "gaps": [
    {
      "policyType": "TERM_LIFE",
      "gapAmount": 5000000.0,
      "severity": "HIGH",
      "advisory": "Your current life cover is below recommended levels."
    }
  ],
  "recommendations": [
    {
      "title": "Increase Life Coverage",
      "priority": "CRITICAL",
      "suggestedCoverage": 10000000.0,
      "estimatedPremium": 55000.0
    }
  ]
}
```

This is currently consumed by the BFF service and is ready for future frontend “Insights” pages.

---

### 1.4 `GET /api/bff/health`

**Purpose**: Simple health endpoint for BFF.

**Response**

```json
{ "status": "UP" }
```

Used by `verify-services.ps1`.

---

## 2. Data Pipeline Service (`data-pipeline-service`)

Port: **8082**.

### 2.1 `GET /api/portfolio/{customerId}`

**Purpose**: Return unified portfolio records from `unified_portfolio` for a given customer.

**Example**

```text
GET /api/portfolio/901120934
```

**Response `200 OK`**

```json
{
  "customerId": 901120934,
  "policies": [
    {
      "policyId": "AUPOL-S001",
      "insurer": "HDFC Life",
      "sourceCollection": "auto_insurance",
      "premium": 18500.0,
      "sumAssured": 500000,
      "startDate": 20240101,
      "policyEnd": 20251231,
      "matchMethod": "PAN_MATCH"
    },
    {
      "policyId": "LIPOL-S001",
      "insurer": "SBI Life",
      "sourceCollection": "life_insurance",
      "premium": 12500.0,
      "sumAssured": 1000000,
      "startDate": 20230101,
      "policyEnd": 20421231,
      "matchMethod": "PAN_MATCH"
    }
  ],
  "totalPolicies": 4
}
```

Used by **BFF** via `DataPipelineClient.getPortfolio(...)`.

---

### 2.2 `GET /api/advisory/{customerId}`

**Purpose**: Compute coverage advisory and gaps for a given customer’s unified portfolio.

**Example**

```text
GET /api/advisory/901120934
```

**Response `200 OK` (shape simplified)**

```json
{
  "customerId": 901120934,
  "lifeCoverage": {
    "current": 5000000.0,
    "recommended": 10000000.0,
    "gap": 5000000.0,
    "severity": "HIGH"
  },
  "healthCoverage": {
    "current": 400000.0,
    "recommended": 3000000.0,
    "gap": 2600000.0,
    "severity": "HIGH"
  },
  "autoCoverage": {
    "current": 450000.0,
    "recommended": 1000000.0,
    "gap": 550000.0,
    "severity": "MEDIUM"
  },
  "overallScore": {
    "score": 72,
    "rating": "GOOD"
  },
  "notes": [
    "Increase term life cover to at least 10x annual income.",
    "Consider higher health coverage for the family."
  ]
}
```

Used by **BFF** via `DataPipelineClient.getAdvisory(...)`.

---

## 3. Customer Service (`customer-service`)

Port: **8081**.

### 3.1 `GET /api/v1/customers/details/{customerId}`

**Purpose**: Fetch customer details by numeric `customerId`.

**Example**

```text
GET /api/v1/customers/details/901120934
```

**Response `200 OK`**

```json
{
  "customerId": 901120934,
  "fullName": "Amit Ramesh Kulkarni",
  "mobile": "919876543210",
  "email": "amit.kulkarni@gmail.com",
  "pan": "AKCPK1123L",
  "status": "ACTIVE"
}
```

Used by **BFF** via `CustomerClient.getCustomerDetails(...)`.

---

### 3.2 `POST /api/v1/customers/login` (internal)

> The BFF currently performs login logic itself using `full name + PAN`.  
> Customer-service also supports email+password style login for future extensions.

**Request**

```json
{
  "customerIdOrEmail": "amit.kulkarni@gmail.com",
  "password": "SomePassword123"
}
```

**Response `200 OK`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "customer": {
    "customerId": 901120934,
    "fullName": "Amit Ramesh Kulkarni",
    "email": "amit.kulkarni@gmail.com"
  }
}
```

---

### 3.3 `GET /api/v1/health`

Simple health endpoint used by `verify-services.ps1`.

**Response**

```json
{ "status": "UP", "service": "customer-service" }
```

---

## 4. Policy Service (`policy-service`)

Port: **8085**.

### 4.1 `GET /api/v1/policies/{policyId}` (shape)

Used for CRUD on raw policy storage.

**Example**

```text
GET /api/v1/policies/AUPOL-S001
```

**Response `200 OK` (simplified)**

```json
{
  "policyId": "AUPOL-S001",
  "customerId": 901120934,
  "insurerId": "HDFC Life",
  "policyNumber": "AUPOL-S001",
  "policyType": "auto_insurance",
  "premiumAmount": 18500.0,
  "sumAssured": 500000.0,
  "status": "ACTIVE"
}
```

### 4.2 `GET /api/v1/health`

Health endpoint used by scripts.

---

## 5. Config Service (`config-service`)

Port: **8888** (host‑mapped to 8889 in Docker).

### 5.1 `GET /{app-name}/{profile}` – Spring Cloud Config

**Example**

```text
GET /customer-service/default
```

**Response** – standard Spring Cloud Config JSON containing property sources.

### 5.2 `GET /actuator/health`

Used by Docker health checks and `verify-services.ps1`.

---

## 6. Discovery Service (`discovery-service`)

Port: **8761**.

### 6.1 `GET /`

Eureka dashboard (HTML UI to see registered instances).

### 6.2 `GET /actuator/health`

Health endpoint used by tooling.

---

## 7. Frontend (Flutter Web) – API Usage Summary

The Flutter app (web build) **only calls the BFF**, never the individual microservices directly.

### 7.1 Login Flow

- **Screen**: `LoginScreen`
- **Method**: `ApiClient.login(userId, password)`
- **Backend API**: `POST /api/bff/auth/login`
- **Data used**:
  - `customerId` → saved and passed to dashboard and analytics screens.
  - `fullName` → shown as “Welcome back, <name>”.

### 7.2 Dashboard Portfolio

- **Screen**: `DashboardScreen`
- **Method**: `ApiClient.getPortfolio(customerId)`
- **Backend API**: `GET /api/bff/portfolio/{customerId}`

Mapping:

- `PortfolioResponse.policies[]` → `Policy` model
  - `policyType` → category chips (`Life`, `Health`, `Motor`, `Others`)
  - `premiumAmount` → “Annual Premium”
  - `sumAssured` → “Sum Insured”
  - `startDate` / `endDate` → used on the policy details page
- `PortfolioResponse.totalPremium` / `totalCoverage` / `totalPolicies`
  - Shown in the three summary cards at the top.

### 7.3 Analytics Dashboard

- **Screen**: `AnalyticsDashboard`
- **Method**: `ApiClient.getPortfolio(customerId)`
- **Backend API**: `GET /api/bff/portfolio/{customerId}`

Usage:

- Computes percentages for:
  - Life Insurance (policies where `policyType` contains `"life"`)
  - Health Insurance (contains `"health"`)
  - Motor Insurance (contains `"auto"` or `"motor"`)
- Shows:
  - Policies Linked = number of policies.
  - Total Protection = sum of `sumAssured` for all policies.

### 7.4 Policy Detail Page

- **Screen**: `PolicyDetailScreen`
- Navigated from **`PolicyCard`** in `DashboardScreen`.
- Uses the already-fetched `Policy` instance (no extra HTTP call).

Fields:

- Header:
  - `policy.name`, `policy.policyId`
  - Due Date badge: `policy.endDate` (formatted as `dd/MM/yy`)
- “Policy Overview”:
  - Status (Active / Due / Expired)
  - Coverage (Life / Health / Motor / Others)
  - Start Date / Expiration Date from `policy.startDate` / `policy.endDate`
  - Premium from `policy.annualPremium`
- “Coverage Details”:
  - Sum Assured from `policy.sumInsured`

---

## Notes on PDF Export

This document is stored as `API_CONTRACTS.md` in `MyPolicy-Backend`.  
To generate a PDF version you can:

1. Open the file in VS Code / any Markdown viewer and **Print to PDF**, or
2. Use a Markdown‑to‑PDF tool (e.g. `pandoc`, or an online converter) on `API_CONTRACTS.md`.

