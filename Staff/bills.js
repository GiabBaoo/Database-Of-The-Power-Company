const express = require('express');
const router = express.Router();
const sql = require('mssql');
const { ulid } = require('ulid');
const db = require('../src/Config/DBConnection');
const verifyToken = require('../src/Middleware/verifyToken');
const { tp1, tp2, tp3 } = require('../src/Config/logger');
router.post("/bills", verifyToken, async (req, res) => {
    const { thanhpho, soHD, maNV, soTien } = req.body;
    try {
        const queryUpdate = `use dienluc
update hopdong set isPaid = 1 where soHD = @soHD `;
        const query = `
insert into hoadon (soHDN ,
    thang ,
    nam ,
    soHD ,
    maNV ,
    soTien ) values (@soHDN,@thang,@nam,@soHD,@maNV,@soTien) `;
        const month = new Date().getMonth() + 1;
        const year = new Date().getFullYear();
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const soHDN = ulid();
        if (thanhpho === "TP1") {
            const resultUpdate1 = await pool1.request()
                .input("soHD", sql.VarChar, soHD).query(queryUpdate)
            if (resultUpdate1.rowsAffected[0] > 0)
                tp1.update("thêm hóa đơn cho hợp đồng " + soHD, { soHD: soHD })
            const result1 = await pool1.request()
                .input("soHD", sql.VarChar, soHD)
                .input("soHDN", sql.VarChar, soHDN)
                .input("maNV", sql.VarChar, maNV)
                .input("thang", sql.Int, month)
                .input("nam", sql.Int, year)
                .input("soTien", sql.Int, soTien)
                .query(query);
            if (result1.rowsAffected[0] > 0)
                tp1.insert("thêm hóa đơn " + soHDN, { soHD: soHD, soHDN: soHDN, maNV: maNV, thang: month, nam: year, soTien: soTien });
        }

        if (thanhpho === "TP2") {
            const resultUpdate2 = await pool2.request()
                .input("soHD", sql.VarChar, soHD).query(queryUpdate)
            if (resultUpdate2.rowsAffected[0] > 0)
                tp2.update("thêm hóa đơn cho hợp đồng " + soHD, { soHD: soHD })
            const result2 = await pool2.request()
                .input("soHD", sql.VarChar, soHD)
                .input("soHDN", sql.VarChar, soHDN)
                .input("maNV", sql.VarChar, maNV)
                .input("thang", sql.Int, month)
                .input("nam", sql.Int, year)
                .input("soTien", sql.Int, soTien)
                .query(query);
            if (result2.rowsAffected[0] > 0)
                tp2.insert("thêm hóa đơn " + soHDN, { soHD: soHD, soHDN: soHDN, maNV: maNV, thang: month, nam: year, soTien: soTien });

        }
        if (thanhpho === "TP3") {
            const resultUpdate3 = await pool3.request()
                .input("soHD", sql.VarChar, soHD).query(queryUpdate)
            if (resultUpdate3.rowsAffected[0] > 0)
                tp3.update("thêm hóa đơn cho hợp đồng " + soHD, { soHD: soHD })
            const result3 = await pool3.request()
                .input("soHD", sql.VarChar, soHD)
                .input("soHDN", sql.VarChar, soHDN)
                .input("maNV", sql.VarChar, maNV)
                .input("thang", sql.Int, month)
                .input("nam", sql.Int, year)
                .input("soTien", sql.Int, soTien)
                .query(query);
            if (result3.rowsAffected[0] > 0)
                tp3.insert("thêm hóa đơn " + soHDN, { soHD: soHD, soHDN: soHDN, maNV: maNV, thang: month, nam: year, soTien: soTien });
        }
        return res.status(200).json({ success: true, message: "them hoa don thanh cong" })
    } catch (error) {
        console.log("loi them hoa don ", error)
        return res.status(500).json({ success: false, message: "thêm hóa đơn thất bại" })
    }

});

router.get("/bills", verifyToken, async (req, res) => {
    // Lấy maNV từ query params được gửi từ client Java
    const maNV = req.query.maNV; 

    if (!maNV) {
        return res.status(400).json({ success: false, message: "Thiếu Mã Nhân Viên để truy vấn hóa đơn." });
    }
    
    // Query: Lấy tất cả các cột cần thiết cho bảng Hóa Đơn, lọc theo MaNV
    const query = `
        USE DienLuc;
        SELECT soHDN, thang, nam, soHD, maNV, soTien
        FROM hoadon
        WHERE maNV = @maNV;
    `;
    
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        
        // Thực hiện truy vấn trên 3 mảnh dữ liệu
        const result1 = await pool1.request().input("maNV", sql.VarChar, maNV).query(query);
        const result2 = await pool2.request().input("maNV", sql.VarChar, maNV).query(query);
        const result3 = await pool3.request().input("maNV", sql.VarChar, maNV).query(query);
        
        // Kết hợp kết quả từ 3 mảnh
        const allBills = [...result1.recordset, ...result2.recordset, ...result3.recordset];
        
        // Trả về dữ liệu dưới key "bills" (key này sẽ được code Java Client trích xuất)
        return res.status(200).json({ 
            success: true, 
            bills: allBills, 
            message: "Lấy danh sách hóa đơn thành công" 
        });
        
    } catch (error) {
        console.error("Lỗi lấy danh sách hóa đơn:", error);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi máy chủ khi lấy danh sách hóa đơn: " + error.message 
        });
    }
});
module.exports = router;