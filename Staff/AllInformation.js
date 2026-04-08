const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const verifyToken = require('../src/Middleware/verifyToken');
router.get("/allInformation", verifyToken, async (req, res) => {
    const { maNV } = req.query;
    const query = `use dienluc
use DienLuc select hoten,tenCN,nhanvien.maCN,thanhpho from nhanvien,chinhanh where nhanvien.maCN=chinhanh.maCN and nhanvien.maNV=@maNV`;
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const result1 = await pool1.request().input("maNV", sql.VarChar, maNV).query(query);
        const result2 = await pool2.request().input("maNV", sql.VarChar, maNV).query(query);
        const result3 = await pool3.request().input("maNV", sql.VarChar, maNV).query(query);
        if (result1.recordset.length !== 0 || result2.recordset.length !== 0 || result3.recordset.length !== 0) {
            return res.status(200).json({ success: true, staff: [...result1.recordset, ...result2.recordset, ...result3.recordset], message: "Lấy dữ liệu nhân viên thành công" })
        }
    }
    catch (err) {
        console.log("Lỗi lấy thông tin Staff", err);
        return res.status(500).json({ success: false, message: "Lỗi không tìm thấy thông tin nhân viên" });
    }
});
module.exports = router