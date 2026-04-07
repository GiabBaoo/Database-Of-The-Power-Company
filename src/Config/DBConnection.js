require("dotenv").config();
const sql = require('mssql');
const { Client } = require('pg');

// =========================================================
// 1. CONFIG CHO TẤT CẢ TP
// =========================================================

const commonOptions = {
    encrypt: true, 
    trustServerCertificate: true,
    connectionTimeout: 10000
};

const dbConfigManh1 = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name,
    options: { ...commonOptions, encrypt: false } // TP1 thường là Local nên để false
};

const dbConfigManh2 = {
    user: process.env.DB_User2,
    password: process.env.DB_Password2,
    server: process.env.DB_Server2,
    database: process.env.DB_Name2,
    port: parseInt(process.env.DB_Server2_Port) || 1433,
    options: commonOptions
};

const dbConfigManh3 = {
    user: process.env.DB_User3,
    password: process.env.DB_Password3,
    host: process.env.DB_Server3,
    port: parseInt(process.env.DB_Server3_Port) || 6543,
    database: 'postgres',
    ssl: { rejectUnauthorized: false }
};

const dbConfigManh5_Backup = {
    user: process.env.DB_User5,
    password: process.env.DB_Password5,
    host: process.env.DB_Server5,
    port: parseInt(process.env.DB_Server5_Port) || 6543,
    database: 'postgres',
    ssl: { rejectUnauthorized: false }
};

const dbConfigManh4_Backup = {
    user: process.env.DB_User4,
    password: process.env.DB_Password4,
    server: process.env.DB_Server4,
    port: parseInt(process.env.DB_Server4_Port) || 1433,
    database: process.env.DB_Name4,
    options: commonOptions
};

// =========================================================
// 2. FAILOVER LOGIC
// =========================================================

const GetManh2DBPool = async () => {
    try {
        let pool = new sql.ConnectionPool(dbConfigManh2);
        await pool.connect();
        await pool.request().query(`USE [${process.env.DB_Name2}]`);
        console.log("✅ TP2 kết nối thành công");
        return pool;
    } catch (err) {
        console.log("⚠️ TP2 sập (hoặc sai pass) → Chuyển sang TP4 dự phòng");
        try {
            let poolBackup = new sql.ConnectionPool(dbConfigManh4_Backup);
            await poolBackup.connect();
            await poolBackup.request().query(`USE [${process.env.DB_Name4}]`);
            console.log("✅ TP4 kết nối thành công (dự phòng)");
            return poolBackup;
        } catch (e) {
            console.log("💀 Cả TP2 và TP4 đều không thể kết nối!");
            throw new Error("Cả TP2 và TP4 đều không thể kết nối!");
        }
    }
};

const GetManh3DBPool = async () => {
    try {
        let pool = new Client(dbConfigManh3);
        await pool.connect();
        console.log("✅ TP3 kết nối thành công");
        return pool;
    } catch (err) {
        console.log("⚠️ TP3 sập → Chuyển sang TP5 dự phòng");
        try {
            let poolBackup = new Client(dbConfigManh5_Backup);
            await poolBackup.connect();
            console.log("✅ TP5 kết nối thành công (dự phòng)");
            return poolBackup;
        } catch (e) {
            console.log("💀 Cả TP3 và TP5 đều không thể kết nối!");
            throw new Error("Cả TP3 và TP5 đều không thể kết nối!");
        }
    }
};

// =========================================================
// 3. SYNC BACK TP4 → TP2
// =========================================================

const syncSQLServer = async () => {
    let pool2, pool4;
    try {
        console.log(`🔄 Đang kết nối TP2 (${process.env.DB_Name2}) và TP4 (${process.env.DB_Name4})...`);
        
        pool2 = new sql.ConnectionPool(dbConfigManh2);
        await pool2.connect();
        await pool2.request().query(`USE [${process.env.DB_Name2}]`);

        pool4 = new sql.ConnectionPool(dbConfigManh4_Backup);
        await pool4.connect();
        await pool4.request().query(`USE [${process.env.DB_Name4}]`);

        // --- SYNC NHANVIEN ---
        const resNV = await pool4.request()
            .query("SELECT * FROM nhanvien");

        console.log(`📦 Tìm thấy ${resNV.recordset.length} nhân viên cần đồng bộ.`);

        for (let row of resNV.recordset) {
            try {
                const checkNV = await pool2.request()
                    .input('ma', row.maNV)
                    .query("SELECT maNV FROM nhanvien WHERE maNV = @ma");

                if (checkNV.recordset.length === 0) {
                    await pool2.request()
                        .input('ma', row.maNV)
                        .input('ten', row.hoten)
                        .input('cn', row.maCN)
                        .query("INSERT INTO nhanvien (maNV, hoten, maCN) VALUES (@ma, @ten, @cn)");
                    console.log(`  ✅ Đã sync mới: ${row.maNV}`);
                } else {
                    console.log(`  ℹ️ ${row.maNV} đã có bên TP2.`);
                }
            } catch (err) {
                console.error(`  ❌ Lỗi dòng ${row.maNV}:`, err.message);
            }
        }
        
        console.log("🚀 Quá trình đồng bộ hoàn tất!");

    } catch (syncErr) {
        console.error("❌ Lỗi kết nối khi Sync:", syncErr.message);
    } finally {
        if (pool2) await pool2.close();
        if (pool4) await pool4.close();
    }
};

// Các hàm khác giữ nguyên...
const GetManh1DBPool = async () => { /* ... giữ code cũ của bạn ... */ };

module.exports = {
    GetManh1DBPool,
    GetManh2DBPool,
    GetManh3DBPool,
    syncSQLServer,
    GetManh2UserDBPool: GetManh2DBPool
};