const { GetManh2DBPool } = require('./src/Config/DBConnection'); // Trỏ đúng file connection của bạn
const sql = require('mssql');

async function insertNewEmployee(maNV, hoTen, maCN) {
    let pool;
    try {
        console.log(`🚀 Đang thử chèn nhân viên ${maNV}...`);
        
        // Gọi hàm GetManh2DBPool - Hàm này đã có sẵn logic: Nếu TP2 lỗi thì trả về TP4
        pool = await GetManh2DBPool();

        if (!pool) {
            console.error("❌ Không thể kết nối tới bất kỳ server nào (TP2 & TP4 đều tèo)!");
            return;
        }

        // Thực hiện chèn dữ liệu
        await pool.request()
            .input('ma', sql.VarChar, maNV)
            .input('ten', sql.NVarChar, hoTen)
            .input('cn', sql.VarChar, maCN)
            .query("INSERT INTO nhanvien (maNV, hoten, maCN) VALUES (@ma, @ten, @cn)");

        console.log(`✅ Thành công! Nhân viên ${maNV} đã được lưu.`);

    } catch (err) {
        console.error("❌ Lỗi khi chèn dữ liệu:", err.message);
    } finally {
        if (pool) await pool.close();
    }
}

// Chạy thử với dữ liệu giả lập
// Bạn có thể đổi 'NV_TEST_001' thành tên khác mỗi lần chạy để tránh trùng khóa
insertNewEmployee('NV_088', 'Nguyen Van B', 'CN1');
