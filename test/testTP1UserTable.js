const sql = require("mssql");
require("dotenv").config();

// Cấu hình kết nối TP1 (Local SQL Server)
const dbConfig = {
    user: process.env.DB_User,
    password: process.env.DB_Password,
    server: process.env.DB_Server1,
    port: parseInt(process.env.DB_Server1_Port) || 1433,
    database: process.env.DB_Name,
    options: {
        encrypt: true,
        trustServerCertificate: true,
        enableArithAbort: true
    }
};

async function testTP1UserConnection() {
    let pool;
    try {
        console.log("🔄 Đang kết nối đến TP1 (Local SQL Server)...");
        console.log(`   Server: ${dbConfig.server}:${dbConfig.port}`);
        console.log(`   Database: ${dbConfig.database}`);
        console.log(`   User: ${dbConfig.user}\n`);

        pool = new sql.ConnectionPool(dbConfig);
        await pool.connect();
        console.log("✅ Kết nối thành công!\n");

        // Test query bảng user
        console.log("🔄 Đang kiểm tra bảng users...");
        const result = await pool.request().query("SELECT TOP 5 * FROM Users");
        
        if (result.recordset.length > 0) {
            console.log("✅ Bảng Users tồn tại!");
            console.log(`   Số bản ghi: ${result.recordset.length}\n`);
            console.log("📋 Dữ liệu mẫu:");
            console.table(result.recordset);
        } else {
            console.log("⚠️  Bảng Users tồn tại nhưng không có dữ liệu\n");
        }

    } catch (error) {
        console.error("❌ Lỗi:", error.message);
        if (error.code === "ESOCKET") {
            console.error("   → Không thể kết nối tới server. Kiểm tra IP/Port");
        } else if (error.code === "ER_ACCESS_DENIED_ERROR") {
            console.error("   → Tên đăng nhập hoặc mật khẩu sai");
        } else if (error.message.includes("Cannot find table")) {
            console.error("   → Bảng Users không tồn tại");
        }
    } finally {
        if (pool) {
            await pool.close();
            console.log("\n🔌 Đã đóng kết nối");
        }
    }
}

testTP1UserConnection();
