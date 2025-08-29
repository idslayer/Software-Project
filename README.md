# 🎟️  Transparent Audit Trail for a Event Booking System using Microservices and Private Blockchain

**Student Name**:  Truong Minh Phuong
**Student ID**:  29697148
**Module**: Software Project

A microservices ticketing system with an **immutable, verifiable audit trail**.  
Logs are normalised, hashed, batched, and **anchored** on a permissioned ledger.  
The booking flow stays fast because anchoring runs **asynchronously**.

> Demo data only. Stripe is in **test mode**.

---

## 🛠 Tech Stack

- **Frontend**: React + Vite + Nginx
- **Backend services**: Java Spring Boot (Java 17)
- **Payments**: Stripe (test)
- **Messaging**: Apache Kafka (+ Kafka Connect)
- **Ingestion**: Logstash
- **Ledger**: Hyperledger Fabric (channel `audit`, chaincode `audit_anchor`)
- **Search & Viz**: Elasticsearch + Kibana
- **CI/CD**: GitHub Actions
- **Deployment**: Azure Virtual Machine
- **Containerisation**: Docker / Docker Compose

---

## 🚀 Features

- Booking + payment (Stripe **test** checkout)
- Canonical JSON logging for **every** domain event
- Hashing + Merkle batching + **on-chain anchor** (commitments only)
- Proof Verifier (UI/API) to check record inclusion
- Kibana dashboards: ingest rate, errors, backlog
- Idempotent anchoring with duplicate protection
- Role-based access (read-only Kibana “auditor”)

---

## 📂 Project Structure

```
/bookingservice    # React + Vite application
/febookingservice'       # Java Spring Boot backend
/log-chain  # Audit log Service
     /log_bundler/          # (Audit Normalizer and Bundler) Java Spring Boot
     /logtash/         # ingest log
     /anchor_contract/       # Fabric Contract Chaincode
     /anchor_script/        # docker-compose for orderers/peers/CAs
     /kafka-es-stack/       # Kafka, ZooKeeper/KRaft, ES, Kibana, Connect
     /docs/                   # ADRs, runbooks

```
---

### ✅ Architecture Decision Records and Run Book


See full design in [`here`](./log-chain/docs/design_1.md).

---

## 🧑‍💻 Quick Start (Run with Docker) -- Need to install docker first

### 1. Clone the repository

```bash
git clone https://github.com/idslayer/Software-Project
```

### 2. Run Frontend

```bash
cd febookingservice
docker build -t febookingservice .
docker run -p 5173:80 febookingservice
```




---

### 3. Run Backend

```bash
cd bookingservice
docker build -t bookingservice .
docker run -p 8080:8080 bookingservice
```


## 🌐 Live Demo

- **Frontend**: https://soundwave.tiktuzki.com/
- **Backend API**: https://phuong.tiktuzki.com/
  (OpenAPI:/swagger-ui/index.html#/ or /api-docs)
---



## 📦 Deployment

- CI/CD configured using GitHub Actions
- Automatically builds & deploys Docker images to Azure Virtual Machine
- Secure and scalable cloud deployment

---

### 🔧 Security & Privacy

- TLS where supported between services

- Fabric PutAnchor: dedicated identity, least privilege

- Kibana auditor: read-only role (no Dev Tools, no writes)

- No PII on chain; only commitments and metadata

- Synthetic data in demos; Stripe test mode


---


