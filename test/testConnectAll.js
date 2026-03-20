#!/usr/bin/env node
/**
 * TEST KẾT NỐI 3 DATABASE
 * Chạy: node test/testConnectAll.js
 */

require('dotenv').config();
const db = require('../src/Config/DBConnection');

const colors = {
    reset: '\x1b[0m',
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[36m',
    bold: '\x1b[1m'
};

async function testTP1() {
    console.log(`\n${colors.bold}${colors.blue}=== TEST TP1: Local SQL Server ===${colors.reset}`);
    try {
        const pool = await db.GetManh1DBPool();
        const request = pool.request();
        const result = await request.query('SELECT GETDATE() as currentTime');
        console.log(`${colors.green}✅ TP1 Kết nối thành công!${colors.reset}`);
        console.log(`   Thời gian server: ${result.recordset[0].currentTime}`);
        return true;
    } catch (err) {
        console.log(`${colors.red}❌ TP1 Lỗi: ${err.message}${colors.reset}`);
        return false;
    }
}

async function testTP2() {
    console.log(`\n${colors.bold}${colors.blue}=== TEST TP2: SomeE SQL Server ===${colors.reset}`);
    try {
        const pool = await db.GetManh2DBPool();
        const request = pool.request();
        const result = await request.query('SELECT GETDATE() as currentTime');
        console.log(`${colors.green}✅ TP2 Kết nối thành công!${colors.reset}`);
        console.log(`   Thời gian server: ${result.recordset[0].currentTime}`);
        return true;
    } catch (err) {
        console.log(`${colors.red}❌ TP2 Lỗi: ${err.message}${colors.reset}`);
        return false;
    }
}

async function testTP3() {
    console.log(`\n${colors.bold}${colors.blue}=== TEST TP3: Supabase PostgreSQL ===${colors.reset}`);
    try {
        const pool = await db.GetManh3DBPool();
        const result = await pool.query('SELECT NOW() as current_time');
        console.log(`${colors.green}✅ TP3 Kết nối thành công!${colors.reset}`);
        console.log(`   Thời gian server: ${result.rows[0].current_time}`);
        return true;
    } catch (err) {
        console.log(`${colors.red}❌ TP3 Lỗi: ${err.message}${colors.reset}`);
        return false;
    }
}

async function testUserDB() {
    console.log(`\n${colors.bold}${colors.blue}=== TEST USER DB: SomeE SQL Server ===${colors.reset}`);
    try {
        const pool = await db.GetManh2UserDBPool();
        const request = pool.request();
        const result = await request.query('SELECT GETDATE() as currentTime');
        console.log(`${colors.green}✅ User DB Kết nối thành công!${colors.reset}`);
        console.log(`   Thời gian server: ${result.recordset[0].currentTime}`);
        return true;
    } catch (err) {
        console.log(`${colors.red}❌ User DB Lỗi: ${err.message}${colors.reset}`);
        return false;
    }
}

async function testQueryExamples() {
    console.log(`\n${colors.bold}${colors.blue}=== QUERY EXAMPLES ===${colors.reset}`);
    
    try {
        // Test TP1
        console.log(`\n${colors.yellow}📌 TP1: Đếm số chi nhánh${colors.reset}`);
        const pool1 = await db.GetManh1DBPool();
        const req1 = pool1.request();
        const result1 = await req1.query('USE DienLuc SELECT COUNT(*) as count FROM chinhanh');
        console.log(`   Số chi nhánh TP1: ${result1.recordset[0].count}`);

        // Test TP3
        console.log(`\n${colors.yellow}📌 TP3: Đếm số chi nhánh${colors.reset}`);
        const pool3 = await db.GetManh3DBPool();
        const result3 = await pool3.query('SELECT COUNT(*) as count FROM chinhanh');
        console.log(`   Số chi nhánh TP3: ${result3.rows[0].count}`);

    } catch (err) {
        console.log(`${colors.red}❌ Lỗi query: ${err.message}${colors.reset}`);
    }
}

async function runAllTests() {
    console.log(`${colors.bold}${colors.green}🔍 KIỂM TRA KẾT NỐI TOÀN BỘ DATABASE${colors.reset}\n`);
    console.log(`Cấu hình từ .env file:`);
    console.log(`  TP1: ${process.env.DB_Server1}:${process.env.DB_Server1_Port}`);
    console.log(`  TP2: ${process.env.DB_Server2}:${process.env.DB_Server2_Port}`);
    console.log(`  TP3: ${process.env.DB_Server3}:${process.env.DB_Server3_Port}`);

    const results = {};
    
    results.tp1 = await testTP1();
    results.tp2 = await testTP2();
    results.tp3 = await testTP3();
    results.userDB = await testUserDB();
    
    await testQueryExamples();

    // Tóm tắt
    console.log(`\n${colors.bold}${colors.blue}=== TÓM TẮT ===${colors.reset}`);
    console.log(`  TP1 (Local SQL): ${results.tp1 ? colors.green + '✅' : colors.red + '❌'}${colors.reset}`);
    console.log(`  TP2 (SomeE SQL): ${results.tp2 ? colors.green + '✅' : colors.red + '❌'}${colors.reset}`);
    console.log(`  TP3 (Supabase):  ${results.tp3 ? colors.green + '✅' : colors.red + '❌'}${colors.reset}`);
    console.log(`  User DB:         ${results.userDB ? colors.green + '✅' : colors.red + '❌'}${colors.reset}`);

    const allSuccess = Object.values(results).every(r => r);
    
    if (allSuccess) {
        console.log(`\n${colors.bold}${colors.green}✅ TẤT CẢ KẾT NỐI THÀNH CÔNG!${colors.reset}`);
        process.exit(0);
    } else {
        console.log(`\n${colors.bold}${colors.red}❌ CÓ LỖIA RỒI! Hãy kiểm tra cấu hình .env${colors.reset}`);
        process.exit(1);
    }
}

runAllTests().catch(err => {
    console.error(`${colors.red}❌ Lỗi không mong muốn: ${err.message}${colors.reset}`);
    process.exit(1);
});
