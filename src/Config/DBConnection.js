require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// ========================================
// TP1: Local SQL Server - Database: DienLuc
// ========================================
const dbConfigManh1 = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name_DienLuc,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    }
};

// ========================================
// TP1: Local SQL Server - Database: Users
// ========================================
const dbConfigManh1Users = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name_Users,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    }
};

// ========================================
// TP2: SomeE SQL Server
// ========================================
const dbConfigManh2 = {
    user: process.env.DB_User2,
    password: process.env.DB_Password2,
    server: process.env.DB_Server2,
    port: parseInt(process.env.DB_Server2_Port) || 1433,
    database: process.env.DB_Name2,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    }
};

// ========================================
// TP3: Supabase PostgreSQL
// ========================================
const dbConfigManh3 = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: parseInt(process.env.DB_Server3_Port) || 5432,
    database: process.env.DB_Name3,
    ssl: { rejectUnauthorized: false }
};

// ========================================
// User Manage Database (SomeE)
// ========================================
const dbConfigManh2Users = {
    user: process.env.DB_UserManage_User,
    password: process.env.DB_UserManage_Password,
    server: process.env.DB_UserManage_Server,
    port: parseInt(process.env.DB_UserManage_Port) || 1433,
    database: process.env.DB_UserManage_Name,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    }
};

let primaryDBPool, secondaryDBPool, thirdDBPool, userDBPool, tp1UsersDBPool;

// Hàm kết nối TP1
const GetManh1DBPool = async () => {
    if (primaryDBPool && primaryDBPool.connected) return primaryDBPool;
    try {
        primaryDBPool = await new sql.ConnectionPool(dbConfigManh1).connect();
        console.log("✅ TP1 (Local SQL): Kết nối thành công");
        return primaryDBPool;
    } catch (err) {
        console.error("❌ TP1 Lỗi:", err.message);
        throw err;
    }
};

// Hàm kết nối TP1 Users
const GetManh1UserDBPool = async () => {
    if (tp1UsersDBPool && tp1UsersDBPool.connected) return tp1UsersDBPool;
    try {
        tp1UsersDBPool = await new sql.ConnectionPool(dbConfigManh1Users).connect();
        console.log("✅ TP1 Users (Local SQL): Kết nối thành công");
        return tp1UsersDBPool;
    } catch (err) {
        console.error("❌ TP1 Users Lỗi:", err.message);
        throw err;
    }
};

// Các hàm khác (Manh2, Manh3, Manh2User)
const GetManh2DBPool = async () => {
    if (secondaryDBPool && secondaryDBPool.connected) return secondaryDBPool;
    secondaryDBPool = await new sql.ConnectionPool(dbConfigManh2).connect();
    return secondaryDBPool;
};

const GetManh3DBPool = async () => {
    if (!thirdDBPool) {
        thirdDBPool = new Client(dbConfigManh3);
        await thirdDBPool.connect();
    }
    return thirdDBPool;
};

const GetManh2UserDBPool = async () => {
    if (userDBPool && userDBPool.connected) return userDBPool;
    userDBPool = await new sql.ConnectionPool(dbConfigManh2Users).connect();
    return userDBPool;
};

const queryPostgres = async (client, query, params = []) => {
    let postgresQuery = query.replace(/@(\w+)/g, (m, p, index) => `$${index + 1}`);
    const result = await client.query(postgresQuery, params);
    return { recordset: result.rows, rowsAffected: [result.rowCount] };
};

module.exports = { 
    GetManh1DBPool, GetManh1UserDBPool, GetManh2DBPool, 
    GetManh3DBPool, GetManh2UserDBPool, queryPostgres 
};