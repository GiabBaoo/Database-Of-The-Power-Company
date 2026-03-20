#!/usr/bin/env node
/**
 * RESET ADMIN PASSWORD
 * Chạy: node resetAdminPassword.js
 */

require('dotenv').config();
const sql = require('mssql');
const crypto = require('crypto');
const db = require('./src/Config/DBConnection');

const colors = {
    reset: '\x1b[0m',
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[36m',
    bold: '\x1b[1m'
};

// Thông tin admin
const adminData = {
    email: 'admin@dienluc.com',
    maNV: 'admin001',
    newPassword: 'Baospaki1234@',
    role: 'Admin'
};

// Hàm tạo salt
function generateSalt(length = 16) {
    return crypto.randomBytes(length).toString('hex');
}

// Hàm hash password
function hashPassword(password, salt) {
    const passWithSalt = password + salt;
    return crypto.createHash('sha512').update(passWithSalt).digest('hex');
}

async function resetAdminPassword() {
    console.log(`${colors.bold}${colors.blue}=== RESET ADMIN PASSWORD ===${colors.reset}\n`);

    console.log(`${colors.yellow}📋 THÔNG TIN ADMIN:${colors.reset}`);
    console.log(`  Email:   ${adminData.email}`);
    console.log(`  MaNV:    ${adminData.maNV}`);
    console.log(`  Password: ${adminData.newPassword}`);
    console.log(`  Role:    ${adminData.role}\n`);

    try {
        // 1. Tạo salt mới
        const salt = generateSalt();
        console.log(`${colors.yellow}🔐 TÍNH TOÁN HASH:${colors.reset}`);
        console.log(`  Salt:    ${salt}`);

        // 2. Hash password
        const hashedPassword = hashPassword(adminData.newPassword, salt);
        console.log(`  Hash:    ${hashedPassword}\n`);

        // 3. Kết nối database TP2
        console.log(`${colors.yellow}💾 CẬP NHẬT VÀO DATABASE:${colors.reset}`);
        console.log(`  Đang kết nối TP2 (SomeE)...`);
        
        const pool = await db.GetManh2UserDBPool();
        console.log(`  ✅ Kết nối thành công!\n`);

        // 4. Kiểm tra user có tồn tại không
        const checkQuery = `
            USE csdlpt_lab2
            SELECT * FROM Users WHERE Email = @email
        `;

        const checkRequest = pool.request();
        checkRequest.input('email', sql.VarChar, adminData.email);
        const checkResult = await checkRequest.query(checkQuery);

        if (checkResult.recordset.length > 0) {
            console.log(`${colors.yellow}🔄 USER ĐÃ TỒN TẠI - CẬP NHẬT MẬT KHẨU:${colors.reset}`);
            
            const updateQuery = `
                USE csdlpt_lab2
                UPDATE Users 
                SET Password = @password, 
                    Salt = @salt,
                    Role = @role
                WHERE Email = @email
            `;

            const updateRequest = pool.request();
            updateRequest.input('email', sql.VarChar, adminData.email);
            updateRequest.input('password', sql.VarChar, hashedPassword);
            updateRequest.input('salt', sql.VarChar, salt);
            updateRequest.input('role', sql.VarChar, adminData.role);

            const updateResult = await updateRequest.query(updateQuery);

            if (updateResult.rowsAffected[0] > 0) {
                console.log(`  ${colors.green}✅ Mật khẩu đã được cập nhật!${colors.reset}\n`);
            }
        } else {
            console.log(`${colors.yellow}➕ USER KHÔNG TỒN TẠI - THÊM MỚI:${colors.reset}`);
            
            const insertQuery = `
                USE csdlpt_lab2
                INSERT INTO Users (MaNV, Email, Password, Salt, Role)
                VALUES (@maNV, @email, @password, @salt, @role)
            `;

            const insertRequest = pool.request();
            insertRequest.input('maNV', sql.VarChar, adminData.maNV);
            insertRequest.input('email', sql.VarChar, adminData.email);
            insertRequest.input('password', sql.VarChar, hashedPassword);
            insertRequest.input('salt', sql.VarChar, salt);
            insertRequest.input('role', sql.VarChar, adminData.role);

            const insertResult = await insertRequest.query(insertQuery);

            if (insertResult.rowsAffected[0] > 0) {
                console.log(`  ${colors.green}✅ Tài khoản admin đã được tạo!${colors.reset}\n`);
            }
        }

        // 5. Kiểm tra lại
        console.log(`${colors.yellow}✔️ KIỂM TRA LẠI:${colors.reset}`);
        const verifyRequest = pool.request();
        verifyRequest.input('email', sql.VarChar, adminData.email);
        const verifyResult = await verifyRequest.query(checkQuery);

        if (verifyResult.recordset.length > 0) {
            const user = verifyResult.recordset[0];
            console.log(`  Email:      ${user.Email}`);
            console.log(`  MaNV:       ${user.MaNV}`);
            console.log(`  Role:       ${user.Role}`);
            console.log(`  Password:   Set ✓`);
            console.log(`  Salt:       ${user.Salt.substring(0, 16)}...\n`);
        }

        // 6. Hướng dẫn đăng nhập
        console.log(`${colors.bold}${colors.green}✅ RESET THÀNH CÔNG!${colors.reset}\n`);
        console.log(`${colors.yellow}📝 THÔNG TIN ĐĂNG NHẬP:${colors.reset}`);
        console.log(`  Email:    ${adminData.email}`);
        console.log(`  Password: ${adminData.newPassword}\n`);

        console.log(`${colors.yellow}🔗 CÓ THỂ ĐĂNG NHẬP TẠI:${colors.reset}`);
        console.log(`  POST http://localhost:9999/login`);
        console.log(`  {
    "email": "${adminData.email}",
    "password": "${adminData.newPassword}"
  }\n`);

        await pool.close();
        process.exit(0);

    } catch (err) {
        console.log(`${colors.red}❌ LỖI: ${err.message}${colors.reset}\n`);
        process.exit(1);
    }
}

resetAdminPassword().catch(err => {
    console.error(`${colors.red}Lỗi: ${err.message}${colors.reset}`);
    process.exit(1);
});
