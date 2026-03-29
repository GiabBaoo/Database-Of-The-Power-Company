require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// ========================================
// CẤU HÌNH CÁC MẢNH (DATABASE CONFIGS)
// ========================================

// TP1: Local SQL Server
const dbConfigManh1 = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name,
    options: { encrypt: true, trustServerCertificate: true },
    pool: { max: 10, min: 0, idleTimeoutMillis: 30000 }
};

// TP2: SomeE SQL Server (Chính)
const dbConfigManh2 = {
    user: process.env.DB_User2,
    password: process.env.DB_Password2,
    server: process.env.DB_Server2,
    port: parseInt(process.env.DB_Server2_Port) || 1433,
    database: process.env.DB_Name2 || process.env.DB_Name,
    options: { encrypt: true, trustServerCertificate: true },
    pool: { max: 10, min: 0, idleTimeoutMillis: 30000 }
};

// TP4: SomeE SQL Server (Backup cho TP2)
const dbConfigManh4_Backup = {
    user: process.env.DB_Server4_User,
    password: process.env.DB_Server4_Pass,
    server: process.env.DB_Server4_Host,
    port: parseInt(process.env.DB_Server4_Port) || 1433,
    database: process.env.DB_Server4_Name,
    options: { encrypt: true, trustServerCertificate: true }
};

// TP3: Supabase PostgreSQL (Chính)
const dbConfigManh3 = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: parseInt(process.env.DB_Server3_Port) || 5432,
    database: process.env.DB_Name3 || 'postgres',
    ssl: { rejectUnauthorized: false }
};

// TP5: Supabase PostgreSQL (Backup cho TP3) - ĐÃ SỬA DÙNG OBJECT CHO CHẮC CHẮN
const dbConfigManh5_Backup = {
    user: process.env.DB_Server5_User,
    password: process.env.DB_Server5_Pass,
    host: process.env.DB_Server5_Host,
    port: parseInt(process.env.DB_Server5_Port) || 5432,
    database: process.env.DB_Server5_Name || 'postgres',
    ssl: { rejectUnauthorized: false }
};

// User Database: SomeE SQL Server
const dbConfigManh2Users = {
    user: process.env.DB_UserManage_User,
    password: process.env.DB_UserManage_Password,
    server: process.env.DB_UserManage_Server,
    port: parseInt(process.env.DB_UserManage_Port) || 1433,
    database: process.env.DB_UserManage_Name,
    options: { encrypt: true, trustServerCertificate: true },
    pool: { max: 10, min: 0, idleTimeoutMillis: 30000 }
};

let primaryDBPool;
let secondaryDBPool;
let thirdDBPool;
let userDBPool;

// ========================================
// HÀM KẾT NỐI (CONNECTION FUNCTIONS)
// ========================================

const GetManh1DBPool = async () => {
    if (primaryDBPool && primaryDBPool.connected) return primaryDBPool;
    try {
        primaryDBPool = new sql.ConnectionPool(dbConfigManh1);
        await primaryDBPool.connect();
        console.log("✅ TP1 (Local): Kết nối thành công");
        return primaryDBPool;
    } catch (err) {
        console.error("❌ TP1 Lỗi:", err.message);
        throw new Error("Không thể kết nối TP1");
    }
};

const GetManh2DBPool = async () => {
    if (secondaryDBPool && secondaryDBPool.connected) return secondaryDBPool;
    try {
        secondaryDBPool = new sql.ConnectionPool(dbConfigManh2);
        await secondaryDBPool.connect();
        console.log("✅ TP2 (Chính): Kết nối thành công");
        return secondaryDBPool;
    } catch (err) {
        console.warn("⚠️ TP2 Lỗi, đang chuyển sang TP4 (Backup)...");
        try {
            secondaryDBPool = new sql.ConnectionPool(dbConfigManh4_Backup);
            await secondaryDBPool.connect();
            console.log("✅ TP4 (Backup): Kết nối thành công!");
            return secondaryDBPool;
        } catch (backupErr) {
            console.error("❌ Cả TP2 và TP4 đều sập!");
            throw new Error("Không thể kết nối TP2 & TP4");
        }
    }
};

const GetManh3DBPool = async () => {
    if (thirdDBPool) {
        try { await thirdDBPool.query('SELECT 1'); return thirdDBPool; } 
        catch (err) { thirdDBPool = null; }
    }
    try {
        thirdDBPool = new Client(dbConfigManh3);
        await thirdDBPool.connect();
        console.log("✅ TP3 (Chính): Kết nối thành công");
        return thirdDBPool;
    } catch (err) {
        console.warn("⚠️ TP3 Lỗi, đang chuyển sang TP5 (Backup)...");
        try {
            thirdDBPool = new Client(dbConfigManh5_Backup);
            await thirdDBPool.connect();
            console.log("✅ TP5 (Backup): Kết nối thành công!");
            return thirdDBPool;
        } catch (backupErr) {
            console.error("❌ Cả TP3 và TP5 đều sập! Lỗi:", backupErr.message);
            throw new Error("Không thể kết nối TP3 & TP5");
        }
    }
};

const GetManh2UserDBPool = async () => {
    if (userDBPool && userDBPool.connected) return userDBPool;
    try {
        userDBPool = new sql.ConnectionPool(dbConfigManh2Users);
        await userDBPool.connect();
        console.log("✅ User DB: Kết nối thành công");
        return userDBPool;
    } catch (err) {
        console.error("❌ User DB Lỗi:", err.message);
        throw new Error("Không thể kết nối User Database");
    }
};

const queryPostgres = async (client, query, params = []) => {
    try {
        let postgresQuery = query;
        let paramIndex = 1;
        const paramRegex = /@(\w+)/g;
        postgresQuery = postgresQuery.replace(paramRegex, () => `$${paramIndex++}`);
        const result = await client.query(postgresQuery, params);
        return { recordset: result.rows, rowsAffected: [result.rowCount] };
    } catch (err) {
        console.error("Lỗi PostgreSQL query:", err);
        throw err;
    }
};

module.exports = { 
    GetManh1DBPool, GetManh2DBPool, GetManh3DBPool, GetManh2UserDBPool, queryPostgres 
};