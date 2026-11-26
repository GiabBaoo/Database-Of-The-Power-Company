# ⚡ Distributed Electricity Management System (DoAnCSDLPT)

A distributed database management system for electricity services built on a multi-platform **Distributed Database (CSDLPT)** architecture (SQL Server & PostgreSQL), featuring **automatic failover**, **backup synchronization**, **centralized logging with Loki/Grafana**, and **AI-powered SQL replay script generation (Ollama)**.

---

## 🏗️ System Architecture

![System Architecture Diagram](images/architecture_diagram.png)

### Database Fragmentation (3 Primary Shards + 3 Backup Servers)

| Shard | Region | DBMS | Role | Backup Server |
| :--- | :--- | :--- | :--- | :--- |
| **TP1** | Hanoi | SQL Server (Local) | `DienLuc` & `UsersCsdlPt` databases | **SV4** (Somee MSSQL) |
| **TP2** | Da Nang / Cloud | SQL Server (Somee) | `csdlpt_lab2` database (Electricity & Users) | **SV5** (Somee MSSQL / PostgreSQL) |
| **TP3** | Ho Chi Minh City | PostgreSQL (Supabase) | `postgres` database | **SV6** (Somee MSSQL) |

- **Failover & Health Check**: When a primary shard (TP1/TP2) loses connection, the system automatically fails over to its corresponding backup server and runs a periodic health check every 30 seconds to restore primary connections once online (`src/Config/DBConnection.js`, `CSDLPT_FrontEnd/src/csdlpt/DatabaseConnection.java`).
- **Backup Synchronization (`BackupSyncService`)**: Logs operations to the `_backup_changelog` table on backup servers during primary downtime and replays them back to the primary server upon recovery.

---

## 📁 Project Structure

```text
DoAnCSDLPT/
├── server.js                  # Express Backend entry point (default port: 9999)
├── swagger.js                 # Swagger UI configuration (/api-docs)
├── resetAdminPassword.js      # Utility script to reset Admin password
├── Admin/                     # Admin APIs (Branches, Staff, Counts, Transfer History)
├── Staff/                     # Staff APIs (Customers, Contracts, Bills, Staff Info)
├── Question/                  # Distributed analytical query APIs (cau1, cau2)
├── src/
│   ├── Config/
│   │   ├── DBConnection.js    # Multi-DB connection pools (TP1, TP2, TP3, SV4, SV5) & Failover
│   │   ├── QueryHelper.js     # Query normalization helper between MSSQL and PostgreSQL
│   │   └── logger.js          # Winston JSON logger partitioned by city (TP1, TP2, TP3)
│   ├── Middleware/
│   │   └── verifyToken.js     # JWT authentication middleware
│   └── Route/
│       └── Login.js           # Authentication route (SHA-512 + Salt, JWT)
├── CSDLPT_FrontEnd/           # Java Swing Desktop App (direct JDBC to 3 shards + backups)
├── CSDLPT/                    # Initial Java Swing UI version
├── scripts/                   # SQL scripts for schema, Users tables & Stored Procedures per server
├── data/                      # SQL Server data files (.mdf, .ldf) for TP1, TP2, TP3
├── loki-logging/              # Docker Compose for Grafana Loki, Promtail, Grafana & logs
├── AI/                        # Tool to parse JSON logs and call Ollama (qwen2.5-coder) for SQL replay
├── test/                      # Database connection and logger test scripts
├── SUPABASE_GUIDE.md          # Setup guide for PostgreSQL on Supabase (TP3)
└── SWAGGER_EXAMPLES.md        # Swagger API usage examples
```

---

## 🛠️ Tech Stack

- **Backend**: Node.js, Express 5, `mssql`, `pg`, `jsonwebtoken`, `crypto` (SHA-512), `winston`, `winston-daily-rotate-file`, `swagger-jsdoc`, `swagger-ui-express`, `ulid`.
- **Desktop Frontend**: Java Swing (JDK 8+/11+, NetBeans/Ant), JDBC (`mssql-jdbc`, `postgresql`).
- **Databases**: Microsoft SQL Server (Local & Somee Cloud), PostgreSQL (Supabase).
- **Logging & Monitoring**: Docker, Grafana Loki, Promtail, Grafana.
- **AI Replay Log**: Ollama (`qwen2.5-coder`).

---

## ⚙️ Installation & Configuration

### 1. Prerequisites
- **Node.js** (v18+ recommended)
- **Java JDK** 11+ (for `CSDLPT_FrontEnd`)
- **SQL Server** (for local TP1) & **Somee / Supabase** accounts (for cloud shards)
- **Docker & Docker Compose** (optional, for Loki/Grafana)
- **Ollama** with `qwen2.5-coder` model (optional, for AI Replay Log)

### 2. Database Initialization
Run the SQL scripts in `scripts/` on their respective servers:
- Primary shards: `setup_server1_hanoi.sql`, `setup_server2_danang.sql`, `setup_server3_hcm.sql`
- Backup shards: `setup_server4_backup_tp1.sql`, `setup_server5_backup_tp2.sql`, `setup_server6_backup_tp3.sql`
- Users tables: `setup_userscsdlpt_tp1.sql`, `setup_userscsdlpt_tp2.sql`, `setup_userscsdlpt_tp3.sql`

> See [SUPABASE_GUIDE.md](SUPABASE_GUIDE.md) for setting up TP3 on Supabase.

### 3. Environment Variables (`.env`)
Create or update `.env` in the root directory with the following variables:

```env
port_serverNode=9999
JWT_SECRET=your_jwt_secret_key

# TP1 - Local SQL Server (DienLuc & UsersCsdlPt)
DB_Server1_Type=mssql
DB_Server1=192.168.56.1
DB_Server1_Port=1433
DB_User1_DienLuc=sa
DB_Password1_DienLuc=your_password
DB_Name1_DienLuc=DienLuc

DB_Server1_Type_Users=mssql
DB_Server1_Users=192.168.56.1
DB_Server1_Port_Users=1433
DB_User1_Users=sa
DB_Password1_Users=your_password
DB_Name1_Users=UsersCsdlPt

# TP2 - Somee SQL Server
DB_Server2_Type=mssql
DB_Server2=your_tp2_host
DB_Server2_Port=1433
DB_User2=your_user
DB_Password2=your_password
DB_Name2=your_db

# TP3 - Supabase PostgreSQL
DB_Server3_Type=postgres
DB_Server3=your_supabase_host
DB_Server3_Port=6543
DB_User3=your_user
DB_Password3=your_password
DB_Name3=postgres
DB_Supabase_Schema=public

# User Database (TP2)
DB_UserManage_Type=mssql
DB_UserManage_Server=your_user_db_host
DB_UserManage_Port=1433
DB_UserManage_User=your_user
DB_UserManage_Password=your_password
DB_UserManage_Name=your_db

# SV4 - Backup TP1 & SV5 - Backup TP2
DB_Server4_Type=mssql
DB_Server4=your_sv4_host
DB_Server4_Port=1433
DB_User4=your_user
DB_Password4=your_password
DB_Name4=your_db

DB_Server5_Type=mssql
DB_Server5=your_sv5_host
DB_Server5_Port=1433
DB_User5=your_user
DB_Password5=your_password
DB_Name5=your_db
```

---

## 🚀 Running the Project

### 1. Backend API (Node.js)
```bash
npm install
node server.js
```
- Server URL: `http://localhost:9999`
- Swagger API Docs: `http://localhost:9999/api-docs`
- Failover Status: `http://localhost:9999/failover-status`

Test database connections:
```bash
node test/testConnectAll.js
```

### 2. Desktop Frontend (`CSDLPT_FrontEnd`)
1. Add `mssql-jdbc` and `postgresql` JDBC drivers into `CSDLPT_FrontEnd/lib/`.
2. Verify connection strings in `CSDLPT_FrontEnd/src/csdlpt/DatabaseConnection.java`.
3. Open `CSDLPT_FrontEnd` in NetBeans and press **F6** (Run Project), or follow [CSDLPT_FrontEnd/QUICK_START.md](CSDLPT_FrontEnd/QUICK_START.md) and [CSDLPT_FrontEnd/SETUP.md](CSDLPT_FrontEnd/SETUP.md).

### 3. Logging Stack (Loki + Promtail + Grafana)
```bash
cd loki-logging
docker-compose up -d
```
- **Loki**: `http://localhost:3200`
- **Grafana**: `http://localhost:3300`

### 4. AI SQL Replay Generator (Ollama)
Ensure `ollama serve` is running with the `qwen2.5-coder` model:
```bash
cd AI
npm install
node generateSQL.js TP1 2025-11-26 13:00:00
```
Generated SQL scripts are saved to `ai-sql-output/<CITY>/<CITY>-<DAY>-replay.sql`.

---

## 📡 Main API Endpoints

| Group | Prefix / Endpoint | Description |
| :--- | :--- | :--- |
| **System** | `GET /`, `GET /failover-status` | Server health and primary/backup failover status |
| **Auth** | `POST /login`, `GET /login/protected` | Authenticate and return JWT token & role (`Admin`/`staff`) |
| **Admin** | `/admin/CountSite`, `/admin/CountCustomer`, `/admin/CountStaff` | Count total branches, customers, and staff across all 3 shards |
| **Admin** | `/admin/addsite`, `/admin/sites`, `/admin/sites/:id` | Create, list, update, and delete branches |
| **Admin** | `/admin/staffs`, `/admin/staffs/:id`, `/admin/historyRecords` | Manage staff, cross-branch/city transfers, and transfer history |
| **Staff** | `/employee/customers`, `/employee/contracts`, `/employee/bills`, `/employee/allInformation` | Manage customers, electricity meter contracts, and bill payments |
| **Queries** | `/question/cau1`, `/question/cau2` | Query bills by staff/customer and bills exceeding kW quota |
