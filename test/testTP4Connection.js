require('dotenv').config();
const sql = require('mssql');

async function test() {
    console.log("🔍 ĐANG TEST TP4 VỚI CẤU HÌNH MỚI...");
    const config = {
        user: process.env.DB_Server4_User,
        password: process.env.DB_Server4_Pass,
        server: process.env.DB_Server4_Host,
        database: process.env.DB_Server4_Name,
        options: { encrypt: false, trustServerCertificate: true }
    };
    try {
        let pool = await sql.connect(config);
        console.log("✅ TP4 (SomeE): KẾT NỐI THÀNH CÔNG RỒI!");
        await pool.close();
    } catch (err) {
        console.log("❌ VẪN LỖI: ", err.message);
    }
}
test();