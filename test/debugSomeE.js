#!/usr/bin/env node
/**
 * TEST KẾT NỐI SOMEE CHI TIẾT
 * Chạy: node test/debugSomeE.js
 */

require('dotenv').config();
const sql = require('mssql');

const colors = {
    reset: '\x1b[0m',
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[36m',
    bold: '\x1b[1m'
};

async function testSomeEConnection() {
    console.log(`${colors.bold}${colors.blue}=== DEBUG SOMEE CONNECTION ===${colors.reset}\n`);

    // 1. Hiển thị cấu hình hiện tại
    console.log(`${colors.yellow}📋 CẤU HÌNH HIỆN TẠI:${colors.reset}`);
    console.log(`  Server:   ${process.env.DB_Server2}`);
    console.log(`  Port:     ${process.env.DB_Server2_Port || 1433}`);
    console.log(`  User:     ${process.env.DB_User2}`);
    console.log(`  Password: ${process.env.DB_Password2 ? '***' + process.env.DB_Password2.slice(-3) : 'KHÔNG CÓ'}`);
    console.log(`  Database: ${process.env.DB_Name2}\n`);

    // 2. Kiểm tra có parameter nào bị thiếu không
    console.log(`${colors.yellow}🔍 KIỂM TRA PARAMETERS:${colors.reset}`);
    const requiredParams = [
        'DB_Server2',
        'DB_User2',
        'DB_Password2'
    ];

    let allPresent = true;
    requiredParams.forEach(param => {
        if (process.env[param]) {
            console.log(`  ✅ ${param}: ${process.env[param]}`);
        } else {
            console.log(`  ❌ ${param}: MISSING!`);
            allPresent = false;
        }
    });

    if (!allPresent) {
        console.log(`\n${colors.red}❌ Thiếu thông tin! Hãy check .env file${colors.reset}`);
        process.exit(1);
    }

    // 3. Test kết nối
    console.log(`\n${colors.yellow}🔌 TEST KẾT NỐI:${colors.reset}`);

    const dbConfig = {
        user: process.env.DB_User2,
        password: process.env.DB_Password2,
        server: process.env.DB_Server2,
        port: parseInt(process.env.DB_Server2_Port) || 1433,
        database: process.env.DB_Name2 || 'master',
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

    console.log(`  Chuỗi kết nối: Server=${dbConfig.server},${dbConfig.port};User=${dbConfig.user};Database=${dbConfig.database};Encrypt=${dbConfig.options.encrypt}\n`);

    try {
        console.log(`  ⏳ Đang kết nối...`);
        const pool = new sql.ConnectionPool(dbConfig);
        
        const startTime = Date.now();
        await pool.connect();
        const connectTime = Date.now() - startTime;

        console.log(`\n  ${colors.green}✅ KẾT NỐI THÀNH CÔNG!${colors.reset}`);
        console.log(`  Thời gian kết nối: ${connectTime}ms`);

        // Test query
        console.log(`\n  ${colors.yellow}🧪 TEST QUERY:${colors.reset}`);
        const request = pool.request();
        const result = await request.query('SELECT GETDATE() as currentTime, @@version as version');
        
        console.log(`  ✅ Query thành công!`);
        console.log(`  Thời gian server: ${result.recordset[0].currentTime}`);
        console.log(`  Version: ${result.recordset[0].version.substring(0, 50)}...\n`);

        // Test database
        console.log(`  ${colors.yellow}📦 KIỂM TRA DATABASE:${colors.reset}`);
        const dbCheck = await request.query(`SELECT DB_NAME() as database_name`);
        console.log(`  Database hiện tại: ${dbCheck.recordset[0].database_name}`);

        // Kiểm tra các bảng trong database User
        console.log(`\n  ${colors.yellow}📊 KIỂM TRA BẢNG:${colors.reset}`);
        try {
            const tableCheck = await request.query(`
                SELECT TABLE_NAME 
                FROM INFORMATION_SCHEMA.TABLES 
                WHERE TABLE_TYPE = 'BASE TABLE'
            `);
            
            if (tableCheck.recordset.length > 0) {
                console.log(`  Các bảng có sẵn:`);
                tableCheck.recordset.forEach(table => {
                    console.log(`    - ${table.TABLE_NAME}`);
                });
            } else {
                console.log(`  ⚠️ Không có bảng nào trong database${colors.reset}`);
            }
        } catch (err) {
            console.log(`  ⚠️ Không thể liệt kê bảng: ${err.message}`);
        }

        // Tóm tắt
        console.log(`\n${colors.bold}${colors.green}✅ TẤT CẢ TEST THÀNH CÔNG!${colors.reset}`);
        console.log(`\nBây giờ bạn có thể dùng SomeE trong ứng dụng Node.js`);

        await pool.close();
        process.exit(0);

    } catch (err) {
        console.log(`\n  ${colors.red}❌ LỖI KẾT NỐI:${colors.reset}`);
        console.log(`  ${err.message}\n`);

        // Gợi ý giải quyết
        console.log(`${colors.yellow}💡 GỢI Ý:${colors.reset}`);
        if (err.message.includes('Login failed')) {
            console.log(`  1. Kiểm tra username/password có đúng không?`);
            console.log(`  2. Tài khoản SQL Login có active không?`);
            console.log(`  3. Thử lại mật khẩu trong SomeE dashboard`);
        } else if (err.message.includes('timeout')) {
            console.log(`  1. Kiểm tra kết nối Internet`);
            console.log(`  2. Server SomeE có down không?`);
            console.log(`  3. Firewall chặn port 1433 không?`);
        } else if (err.message.includes('Certificate')) {
            console.log(`  1. Vấn đề SSL/TLS`);
            console.log(`  2. Kiểm tra trustServerCertificate trong config`);
        }

        process.exit(1);
    }
}

testSomeEConnection().catch(err => {
    console.error(`${colors.red}Lỗi: ${err.message}${colors.reset}`);
    process.exit(1);
});
