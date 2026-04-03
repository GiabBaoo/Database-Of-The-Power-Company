require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// =========================================================
// 1. CẤU HÌNH DATABASE
// =========================================================

const dbConfigManh1 = {
    user: process.env.DB_User || 'sa',
    password: process.env.DB_Password || 'Baospaki1234@',
    server: '127.0.0.1',
    port: 1433,
    database: 'DienLuc_TP1', 
    options: { encrypt: true, trustServerCertificate: true },
    pool: { max: 5, min: 0, idleTimeoutMillis: 5000 },
    connectionTimeout: 5000
};

const dbConfigManh2 = {
    user: process.env.DB_User2,
    password: process.env.DB_Password2,
    server: process.env.DB_Server2,
    port: 1433,
    database: process.env.DB_Name2,
    options: { encrypt: true, trustServerCertificate: true },
    pool: { max: 5, min: 0, idleTimeoutMillis: 5000 },
    connectionTimeout: 5000
};

// Cấu hình TP4 (Backup Local cho SQL Server)
const dbConfigManh4_Backup = {
    user: 'sa',
    password: 'Baospaki1234@',
    server: '127.0.0.1',
    port: 1433,
    database: 'DienLuc_TP4', // Cái DB ông vừa tạo
    options: { encrypt: true, trustServerCertificate: true }
};

const dbConfigManh3 = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: 6543,
    database: 'postgres',
    ssl: { rejectUnauthorized: false },
    connectionTimeoutMillis: 5000
};

const dbConfigManh5_Backup = {
    user: process.env.DB_Server5_User,
    password: process.env.DB_Server5_Pass,
    host: process.env.DB_Server5_Host,
    port: 6543,
    database: 'postgres',
    ssl: { rejectUnauthorized: false }
};

const dbConfigManh2Users = {
    user: process.env.DB_UserManage_User || 'sa',
    password: process.env.DB_UserManage_Password || 'Baospaki1234@',
    server: process.env.DB_UserManage_Server || '127.0.0.1',
    port: 1433,
    database: 'UsersCsdlPt',
    options: { encrypt: true, trustServerCertificate: true }
};

let primaryDBPool, secondaryDBPool, thirdDBPool, userDBPool;

// =========================================================
// 2. CÁC HÀM KẾT NỐI (FAILOVER)
// =========================================================

const GetManh1DBPool = async () => {
    if (primaryDBPool && primaryDBPool.connected) return primaryDBPool;
    try {
        primaryDBPool = new sql.ConnectionPool(dbConfigManh1);
        await primaryDBPool.connect();
        return primaryDBPool;
    } catch (err) { return null; }
};

const GetManh2DBPool = async () => {
    if (secondaryDBPool && secondaryDBPool.connected) return secondaryDBPool;
    try {
        secondaryDBPool = new sql.ConnectionPool(dbConfigManh2);
        await secondaryDBPool.connect();
        return secondaryDBPool;
    } catch (err) {
        try {
            secondaryDBPool = new sql.ConnectionPool(dbConfigManh4_Backup);
            await secondaryDBPool.connect();
            console.log("⚠️ TP2 (Somee) sập, đang dùng tạm TP4 (Local)");
            return secondaryDBPool;
        } catch (e) { return null; }
    }
};

const GetManh3DBPool = async () => {
    if (thirdDBPool) return thirdDBPool;
    try {
        thirdDBPool = new Client(dbConfigManh3);
        await thirdDBPool.connect();
        return thirdDBPool;
    } catch (err) {
        try {
            thirdDBPool = new Client(dbConfigManh5_Backup);
            await thirdDBPool.connect();
            console.log("⚠️ TP3 sập, đang dùng tạm TP5");
            return thirdDBPool;
        } catch (e) { return null; }
    }
};

const GetManh2UserDBPool = async () => {
    if (userDBPool && userDBPool.connected) return userDBPool;
    try {
        userDBPool = new sql.ConnectionPool(dbConfigManh2Users);
        await userDBPool.connect();
        return userDBPool;
    } catch (err) { return null; }
};

const queryPostgres = async (client, query, params = []) => {
    if (!client) throw new Error("Chưa kết nối được Postgres!");
    try {
        let pgQuery = query.replace(/@(\w+)/g, (m, p1, i) => `$${i + 1}`);
        const result = await client.query(pgQuery, params);
        return { recordset: result.rows, rowsAffected: [result.rowCount] };
    } catch (err) { throw err; }
};

// =========================================================
// 3. HÀM ĐỒNG BỘ NGƯỢC (SYNC BACK)
// =========================================================

const syncSQLServer = async () => {
    try {
        const pool2 = new sql.ConnectionPool(dbConfigManh2);
        await pool2.connect(); // Thử xem Somee sống lại chưa
        const pool4 = new sql.ConnectionPool(dbConfigManh4_Backup);
        await pool4.connect();

        // 1. Quét bảng nhân viên (nhanvien)
        const resNV = await pool4.request().query("SELECT * FROM nhanvien WHERE Synced = 0");
        for (let row of resNV.recordset) {
            await pool2.request()
                .input('ma', row.MaNV).input('ten', row.HoTen).input('cn', row.MaCN)
                .query("INSERT INTO nhanvien (MaNV, HoTen, MaCN) VALUES (@ma, @ten, @cn)");
            await pool4.request().query(`UPDATE nhanvien SET Synced = 1 WHERE MaNV = '${row.MaNV}'`);
        }

        // 2. Quét bảng chi nhánh (chinhanh)
        const resCN = await pool4.request().query("SELECT * FROM chinhanh WHERE Synced = 0");
        for (let row of resCN.recordset) {
            await pool2.request()
                .input('ma', row.MaCN).input('ten', row.TenCN).input('tp', row.ThanhPho)
                .query("INSERT INTO chinhanh (MaCN, TenCN, ThanhPho) VALUES (@ma, @ten, @tp)");
            await pool4.request().query(`UPDATE chinhanh SET Synced = 1 WHERE MaCN = '${row.MaCN}'`);
        }

        await pool2.close(); await pool4.close();
        if (resNV.recordset.length > 0 || resCN.recordset.length > 0) {
            console.log("✅ Đã đồng bộ dữ liệu tồn đọng từ TP4 về TP2!");
        }
    } catch (e) { /* Somee vẫn sập thì thôi */ }
};

const syncPostgres = async () => {
    let c3, c5;
    try {
        c3 = new Client(dbConfigManh3); await c3.connect();
        c5 = new Client(dbConfigManh5_Backup); await c5.connect();
        const res = await c5.query("SELECT * FROM \"hoadon\" WHERE \"Synced\" = 0");
        for (let row of res.rows) {
            await c3.query("INSERT INTO \"hoadon\" (\"MaHD\", \"SoTien\") VALUES ($1, $2)", [row.MaHD, row.SoTien]);
            await c5.query("UPDATE \"hoadon\" SET \"Synced\" = 1 WHERE \"MaHD\" = $1", [row.MaHD]);
        }
        if (res.rows.length > 0) console.log("✅ Đã đồng bộ Postgres về TP3!");
    } catch (e) {}
    finally { if (c3) await c3.end(); if (c5) await c5.end(); }
};
// Thay đoạn cũ bằng đoạn này nhé:
module.exports = { 
    GetManh1DBPool, 
    GetManh2DBPool, 
    GetManh3DBPool, 
    GetManh2UserDBPool, 
    queryPostgres 
};

// VÀ quan trọng nhất, ông thêm dòng này ngay bên dưới để "cứu net" 
// nếu server.js gọi kiểu DB.GetManh1DBPool:
Object.assign(module.exports, {
    GetManh1DBPool, GetManh2DBPool, GetManh3DBPool, GetManh2UserDBPool, queryPostgres
});