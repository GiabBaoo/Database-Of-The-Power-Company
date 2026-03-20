# ⚡ Electricity Service System - Distributed Database Architecture

[![Docker](https://img.shields.io/badge/Docker-Enabled-blue.svg)](https://www.docker.com/)
[![Node.js](https://img.shields.io/badge/Node.js-v18-green.svg)](https://nodejs.org/)
[![MSSQL](https://img.shields.io/badge/MSSQL-2022-red.svg)](https://www.microsoft.com/sql-server)

Final project for the **Distributed Database Systems** course. [cite_start]This system simulates an electricity service management platform featuring a horizontal fragmentation architecture, integrated with an automatic Failover mechanism and an **AI-powered (LLM) Log Replay** tool for data recovery. [cite: 5-6, 60-61, 68]

---

## 📖 Table of Contents
- [System Architecture](#-system-architecture)
- [Tech Stack](#-tech-stack)
- [Key Features](#-key-features)
- [Installation & Deployment](#-installation--deployment)
- [Usage Guide](#-usage-guide)
- [AI Recovery Tool](#-ai-recovery-tool-advanced-feature)

---

## 🏛 System Architecture

The system is deployed using a distributed model on Linux (via Docker Containers), consisting of:
* [cite_start]**3 Data Sites (TP1, TP2, TP3):** Store horizontally fragmented data based on geographical regions. [cite: 76]
* [cite_start]**API Gateway (Node.js):** Handles request routing and load balancing. [cite: 63]
* [cite_start]**Log DB (Loki):** Centralized log storage for monitoring and disaster recovery. [cite: 389]

![Architecture Diagram](images/architecture_diagram.png)

---

## 🛠 Tech Stack

| Component | Technology | Note |
| :--- | :--- | :--- |
| **Database** | Microsoft SQL Server 2022 | Running on Docker Containers |
| **Backend** | Node.js (Express) | API Gateway & Logic Processing |
| **Container** | Docker & Docker Compose | Infrastructure Management |
| **Monitoring** | Grafana & Loki | System Log Monitoring |
| **AI Module** | Ollama (Model Qwen 2.5) | Parses JSON Logs -> Generates SQL Scripts |

---

## 🚀 Key Features

1.  [cite_start]**Horizontal Fragmentation:** Data is distributed across different servers based on branch/city location. [cite: 60]
2.  [cite_start]**Custom Replication:** A self-developed data synchronization mechanism optimized for flexibility in Linux environments. [cite: 61]
3.  [cite_start]**Failover System:** Automatically redirects traffic to a backup server when the primary server goes down. [cite: 64]
4.  [cite_start]**AI Log Replay:** Utilizes a Local AI model to parse JSON logs and reconstruct SQL commands to synchronize missing data during backup delays. [cite: 68, 74]

---

## ⚙ Installation & Deployment

### 1. Prerequisites
* Docker Desktop
* Node.js (v18 or higher)
* Ollama (Required for the AI module)

### 2. Start Database & Monitoring
Use Docker Compose to set up the SQL Server infrastructure, Grafana, and Loki:

```bash
docker-compose up -d