require('dotenv').config();
// ĐẢM BẢO TÊN FILE DƯỚI ĐÂY ĐÚNG VỚI FILE CỦA ÔNG
const { GetManh3DBPool } = require('./DBConnection'); 

async function runTest() {
    console.log("🚀 Đang kiểm tra Postgres...");
    
    // Thử kết nối lấy client (Hàm này của ông đã có logic TP3 -> TP5 rồi)
    const client = await GetManh3DBPool();

    if (!client) {
        console.error("❌ Không thể kết nối tới TP3 hoặc TP5!");
        return;
    }

    try {
        const ma = 'PG_' + Date.now(); // Tạo mã không bao giờ trùng
        const ten = 'Gia Bao Supabase';
        const cn = 'CN3';

        // Insert thử
        await client.query(
            "INSERT INTO nhanvien (maNV, hoten, maCN) VALUES ($1, $2, $3)",
            [ma, ten, cn]
        );
        
        console.log(`✅ Thành công! Đã chèn nhân viên ${ma}`);
    } catch (err) {
        console.error("❌ Lỗi truy vấn:", err.message);
    } finally {
        // Đóng kết nối
        if (client.end) await client.end();
    }
}

runTest();