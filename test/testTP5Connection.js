require('dotenv').config();
const { Pool } = require('pg');

async function test() {
    console.log("🔍 ĐANG TEST TP5 (SUPABASE AWS-1)...");
    const pool = new Pool({
        host: process.env.DB_Server5_Host,
        user: process.env.DB_Server5_User,
        password: process.env.DB_Server5_Pass,
        database: process.env.DB_Server5_Name,
        port: process.env.DB_Server5_Port,
        ssl: { rejectUnauthorized: false }
    });

    try {
        const res = await pool.query('SELECT NOW()');
        console.log("✅ TP5 (Supabase AWS-1): KẾT NỐI NGON LÀNH RỒI!");
        await pool.end();
    } catch (err) {
        console.log("❌ VẪN LỖI TP5: ", err.message);
    }
}
test();