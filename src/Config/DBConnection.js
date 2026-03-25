require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// ========================================
// TP1: Local SQL Server (mssql)
// ========================================
const dbConfigManh1 = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    },
    pool: {
        max: 10,
        min: 0,
        idleTimeoutMillis: 30000
    }
};

// ========================================
// TP2: SomeE SQL Server (mssql)
// ========================================
const dbConfigManh2 = {
    user: process.env.DB_User2,
    password: process.env.DB_Password2,
    server: process.env.DB_Server2,
    port: parseInt(process.env.DB_Server2_Port) || 1433,
    database: process.env.DB_Name2 || process.env.DB_Name,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    },
    pool: {
        max: 10,
        min: 0,
        idleTimeoutMillis: 30000
    }
};

// ========================================
// TP3: Supabase PostgreSQL (pg)
// ========================================
const dbConfigManh3 = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: parseInt(process.env.DB_Server3_Port) || 5432,
    database: process.env.DB_Name3 || 'postgres',
    ssl: {
        rejectUnauthorized: false
    }
};

// ========================================
// User Database: SomeE SQL Server (mssql)
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
    },
    pool: {
        max: 10,
        min: 0,
        idleTimeoutMillis: 30000
    }
};

// ========================================
// TP4: SomeE SQL Server (mssql)
// ========================================
const dbConfigManh4 = {
    user: process.env.DB_User4,
    password: process.env.DB_Password4,
    server: process.env.DB_Server4,
    port: parseInt(process.env.DB_Server4_Port) || 1433,
    database: process.env.DB_Name4,
    options: {
        encrypt: true,
        trustServerCertificate: true,
    },
    pool: {
        max: 10,
        min: 0,
        idleTimeoutMillis: 30000
    }
};

// ========================================
// TP5: Supabase PostgreSQL (pg)
// ========================================
const dbConfigManh5 = {
    user: process.env.DB_User5,
    password: process.env.DB_Password5,
    host: process.env.DB_Server5,
    port: parseInt(process.env.DB_Server5_Port) || 5432,
    database: process.env.DB_Name5 || 'postgres',
    ssl: {
        rejectUnauthorized: false
    }
};

let primaryDBPool;
let secondaryDBPool;
let thirdDBPool;
let fourthDBPool;
let fifthDBPool;
let userDBPool;

// ========================================
// TP1: Local SQL Server
// ========================================
const GetManh1DBPool = async () => {
    if (primaryDBPool && primaryDBPool.connected) {
        return primaryDBPool;
    }

    try {
        primaryDBPool = new sql.ConnectionPool(dbConfigManh1);
        await primaryDBPool.connect();
        console.log("✅ TP1 (Local SQL Server): Kết nối thành công");
        return primaryDBPool;
    } catch (err) {
        console.error("❌ TP1 (Local SQL Server): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP1");
    }
};

// ========================================
// TP2: SomeE SQL Server
// ========================================
const GetManh2DBPool = async () => {
    if (secondaryDBPool && secondaryDBPool.connected) {
        return secondaryDBPool;
    }

    try {
        secondaryDBPool = new sql.ConnectionPool(dbConfigManh2);
        await secondaryDBPool.connect();
        console.log("✅ TP2 (SomeE SQL Server): Kết nối thành công");
        return secondaryDBPool;
    } catch (err) {
        console.error("❌ TP2 (SomeE SQL Server): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP2");
    }
};

// ========================================
// TP3: Supabase PostgreSQL
// ========================================
const GetManh3DBPool = async () => {
    if (thirdDBPool && thirdDBPool) {
        try {
            await thirdDBPool.query('SELECT 1');
            return thirdDBPool;
        } catch (err) {
            thirdDBPool = null;
        }
    }

    try {
        thirdDBPool = new Client(dbConfigManh3);
        await thirdDBPool.connect();
        console.log("✅ TP3 (Supabase PostgreSQL): Kết nối thành công");
        return thirdDBPool;
    } catch (err) {
        console.error("❌ TP3 (Supabase PostgreSQL): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP3");
    }
};

// ========================================
// TP4: SomeE SQL Server
// ========================================
const GetManh4DBPool = async () => {
    if (fourthDBPool && fourthDBPool.connected) {
        return fourthDBPool;
    }

    try {
        fourthDBPool = new sql.ConnectionPool(dbConfigManh4);
        await fourthDBPool.connect();
        console.log("✅ TP4 (SomeE SQL Server): Kết nối thành công");
        return fourthDBPool;
    } catch (err) {
        console.error("❌ TP4 (SomeE SQL Server): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP4");
    }
};

// ========================================
// TP5: Supabase PostgreSQL
// ========================================
const GetManh5DBPool = async () => {
    if (fifthDBPool) {
        try {
            await fifthDBPool.query('SELECT 1');
            return fifthDBPool;
        } catch (err) {
            fifthDBPool = null;
        }
    }

    try {
        fifthDBPool = new Client(dbConfigManh5);
        await fifthDBPool.connect();
        console.log("✅ TP5 (Supabase PostgreSQL): Kết nối thành công");
        return fifthDBPool;
    } catch (err) {
        console.error("❌ TP5 (Supabase PostgreSQL): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP5");
    }
};

// ========================================
// User Database: SomeE SQL Server
// ========================================
const GetManh2UserDBPool = async () => {
    if (userDBPool && userDBPool.connected) {
        return userDBPool;
    }

    try {
        userDBPool = new sql.ConnectionPool(dbConfigManh2Users);
        await userDBPool.connect();
        console.log("✅ User DB (SomeE SQL Server): Kết nối thành công");
        return userDBPool;
    } catch (err) {
        console.error("❌ User DB (SomeE SQL Server): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối User Database");
    }
};

// ========================================
// Helper: Thực thi query cho PostgreSQL
// ========================================
const queryPostgres = async (client, query, params = []) => {
    try {
        // Chuyển đổi từ MSSQL parameters sang PostgreSQL
        let postgresQuery = query;
        let paramIndex = 1;
        const paramMap = {};

        // Thay @paramName bằng $1, $2, ...
        const paramRegex = /@(\w+)/g;
        postgresQuery = postgresQuery.replace(paramRegex, () => {
            return `$${paramIndex++}`;
        });

        const result = await client.query(postgresQuery, params);
        
        return {
            recordset: result.rows,
            rowsAffected: [result.rowCount]
        };
    } catch (err) {
        console.error("Lỗi PostgreSQL query:", err);
        throw err;
    }
};

module.exports = { 
    GetManh1DBPool, 
    GetManh2DBPool, 
    GetManh3DBPool, 
    GetManh4DBPool,
    GetManh5DBPool,
    GetManh2UserDBPool,
    queryPostgres
};