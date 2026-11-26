require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// ========================================
// TP1: Local SQL Server (mssql)
// Database: DienLuc
// ========================================
const dbConfigManh1 = {
    user: process.env.DB_User1_DienLuc,
    password: process.env.DB_Password1_DienLuc,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name1_DienLuc,
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
    database: process.env.DB_Name2,
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

// Config cho TP1 - Users Database
const dbConfigManh1Users = {
    user: process.env.DB_User1_Users,
    password: process.env.DB_Password1_Users,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name1_Users,
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
// SV4: Somee SQL Server (BACKUP TP1)
// ========================================
const dbConfigSV4 = {
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
// SV5: Supabase PostgreSQL (BACKUP TP2)
// ========================================
const dbConfigSV5 = {
    user: process.env.DB_User5,
    password: process.env.DB_Password5,
    host: process.env.DB_Server5,
    port: parseInt(process.env.DB_Server5_Port) || 6543,
    database: process.env.DB_Name5 || 'postgres',
    ssl: {
        rejectUnauthorized: false
    }
};

let primaryDBPool;
let secondaryDBPool;
let thirdDBPool;
let userDBPool;
let tp1UsersDBPool;
let sv4DBPool;   // Backup TP1
let sv5DBPool;   // Backup TP2

// Failover state
let tp1UsingBackup = false;
let tp2UsingBackup = false;
let healthCheckInterval = null;

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
        
        // Nếu trước đó dùng backup → TP1 online lại
        if (tp1UsingBackup) {
            console.log("🎉 TP1 đã online trở lại!");
            tp1UsingBackup = false;
        }
        
        return primaryDBPool;
    } catch (err) {
        console.error("❌ TP1 (Local SQL Server): Lỗi kết nối", err.message);
        
        // Failover sang SV4
        console.log("⚡ FAILOVER: Chuyển sang SV4 (Backup TP1)...");
        tp1UsingBackup = true;
        startHealthCheck();
        
        try {
            const sv4Pool = await GetSV4DBPool();
            console.log("✅ FAILOVER thành công: Đang dùng SV4 thay TP1");
            return sv4Pool;
        } catch (sv4Err) {
            console.error("❌ CẢ TP1 VÀ SV4 ĐỀU KHÔNG KẾT NỐI ĐƯỢC!");
            throw new Error("Không thể kết nối TP1 hoặc SV4");
        }
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
        
        if (tp2UsingBackup) {
            console.log("🎉 TP2 đã online trở lại!");
            tp2UsingBackup = false;
        }
        
        return secondaryDBPool;
    } catch (err) {
        console.error("❌ TP2 (SomeE SQL Server): Lỗi kết nối", err.message);
        
        // Failover sang SV5
        console.log("⚡ FAILOVER: Chuyển sang SV5 (Backup TP2)...");
        tp2UsingBackup = true;
        startHealthCheck();
        
        try {
            const sv5Pool = await GetSV5DBPool();
            console.log("✅ FAILOVER thành công: Đang dùng SV5 thay TP2");
            return sv5Pool;
        } catch (sv5Err) {
            console.error("❌ CẢ TP2 VÀ SV5 ĐỀU KHÔNG KẾT NỐI ĐƯỢC!");
            throw new Error("Không thể kết nối TP2 hoặc SV5");
        }
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
// TP1 Users: Local SQL Server
// ========================================
const GetManh1UserDBPool = async () => {
    if (tp1UsersDBPool && tp1UsersDBPool.connected) {
        return tp1UsersDBPool;
    }

    try {
        tp1UsersDBPool = new sql.ConnectionPool(dbConfigManh1Users);
        await tp1UsersDBPool.connect();
        console.log("✅ TP1 Users (Local SQL Server): Kết nối thành công");
        return tp1UsersDBPool;
    } catch (err) {
        console.error("❌ TP1 Users (Local SQL Server): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối TP1 Users");
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
// SV4: Somee SQL Server (BACKUP TP1)
// ========================================
const GetSV4DBPool = async () => {
    if (sv4DBPool && sv4DBPool.connected) {
        return sv4DBPool;
    }

    try {
        sv4DBPool = new sql.ConnectionPool(dbConfigSV4);
        await sv4DBPool.connect();
        console.log("✅ SV4 (Backup TP1 - Somee): Kết nối thành công");
        return sv4DBPool;
    } catch (err) {
        console.error("❌ SV4 (Backup TP1): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối SV4 (Backup TP1)");
    }
};

// ========================================
// SV5: Supabase PostgreSQL (BACKUP TP2)
// ========================================
const GetSV5DBPool = async () => {
    if (sv5DBPool) {
        try {
            await sv5DBPool.query('SELECT 1');
            return sv5DBPool;
        } catch (err) {
            sv5DBPool = null;
        }
    }

    try {
        sv5DBPool = new Client(dbConfigSV5);
        await sv5DBPool.connect();
        console.log("✅ SV5 (Backup TP2 - Supabase): Kết nối thành công");
        return sv5DBPool;
    } catch (err) {
        console.error("❌ SV5 (Backup TP2): Lỗi kết nối", err.message);
        throw new Error("Không thể kết nối SV5 (Backup TP2)");
    }
};

// ========================================
// Health Check: Kiểm tra server chính định kỳ
// ========================================
const startHealthCheck = () => {
    if (healthCheckInterval) return; // Đã chạy rồi

    console.log("🏥 Health Check đã bắt đầu (kiểm tra mỗi 30 giây)");
    
    healthCheckInterval = setInterval(async () => {
        try {
            // Kiểm tra TP1 nếu đang dùng backup
            if (tp1UsingBackup) {
                console.log("🏥 Health Check: Đang kiểm tra TP1...");
                try {
                    const testPool = new sql.ConnectionPool(dbConfigManh1);
                    await testPool.connect();
                    console.log("🎉 TP1 đã ONLINE trở lại!");
                    primaryDBPool = testPool;
                    tp1UsingBackup = false;
                } catch (err) {
                    console.log("🏥 TP1 vẫn đang sập...");
                }
            }

            // Kiểm tra TP2 nếu đang dùng backup
            if (tp2UsingBackup) {
                console.log("🏥 Health Check: Đang kiểm tra TP2...");
                try {
                    const testPool = new sql.ConnectionPool(dbConfigManh2);
                    await testPool.connect();
                    console.log("🎉 TP2 đã ONLINE trở lại!");
                    secondaryDBPool = testPool;
                    tp2UsingBackup = false;
                } catch (err) {
                    console.log("🏥 TP2 vẫn đang sập...");
                }
            }

            // Dừng health check nếu tất cả primary đã online
            if (!tp1UsingBackup && !tp2UsingBackup) {
                console.log("✅ Tất cả server chính đã online. Dừng health check.");
                stopHealthCheck();
            }
        } catch (err) {
            console.error("❌ Lỗi Health Check:", err.message);
        }
    }, 30000); // 30 giây
};

const stopHealthCheck = () => {
    if (healthCheckInterval) {
        clearInterval(healthCheckInterval);
        healthCheckInterval = null;
        console.log("🏥 Health Check đã dừng");
    }
};

// ========================================
// Failover Status API
// ========================================
const getFailoverStatus = () => ({
    tp1: {
        primary: !tp1UsingBackup,
        usingBackup: tp1UsingBackup,
        backupServer: tp1UsingBackup ? "SV4 (Somee MSSQL)" : null
    },
    tp2: {
        primary: !tp2UsingBackup,
        usingBackup: tp2UsingBackup,
        backupServer: tp2UsingBackup ? "SV5 (Supabase PostgreSQL)" : null
    },
    tp3: {
        primary: true,
        usingBackup: false,
        backupServer: null
    }
});

// ========================================
// Helper: Thực thi query cho PostgreSQL
// ========================================
const queryPostgres = async (client, query, params = []) => {
    try {
        let postgresQuery = query;
        let paramIndex = 1;

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
    GetManh1UserDBPool,
    GetManh2DBPool, 
    GetManh3DBPool, 
    GetManh2UserDBPool,
    GetSV4DBPool,
    GetSV5DBPool,
    queryPostgres,
    getFailoverStatus,
    startHealthCheck,
    stopHealthCheck,
    isTP1UsingBackup: () => tp1UsingBackup,
    isTP2UsingBackup: () => tp2UsingBackup
};