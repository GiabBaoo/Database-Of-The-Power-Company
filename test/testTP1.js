require('dotenv').config();
const sql = require('mssql');

async function testTP1() {
    console.log("🔍 ĐANG KIỂM TRA TP1 (LOCAL)...");
    const config = {
        user: 'sa',
        password: 'Baospaki1234@', // Pass nãy bạn đưa nè
        server: '127.0.0.1',       // Thử chạy nội bộ máy mình
        database: 'DienLuc',
        options: {
            encrypt: false,
            trustServerCertificate: true
        }
    };

    try {
        let pool = await sql.connect(config);
        console.log("✅ TP1 (Local): KẾT NỐI NGON LÀNH RỒI!");
        const result = await pool.request().query('SELECT DB_NAME() as db');
        console.log("📍 Đang đứng ở Database:", result.recordset[0].db);
        await pool.close();
    } catch (err) {
        console.log("❌ VẪN LỖI TP1: ", err.message);
        console.log("👉 Nhắc nhẹ: Bạn đã bật TCP/IP trong SQL Server Configuration Manager chưa?");
    }
}
testTP1();