const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const verifyToken = require('../src/Middleware/verifyToken');
router.get('/cau1', verifyToken, async (req, res) => {
    const { maNV, maKH } = req.query;
    const query = "use dienluc select soHDN,thang,nam,soTien from  hoadon,hopdong where  hoadon.maNV=@maNV and hopdong.maKH=@maKH and hopdong.soHD=hoadon.soHD  ";
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const hoadon1 = await pool1.request().input("maNV", sql.VarChar, maNV).input("maKH", sql.VarChar, maKH).query(query);
        const hoadon2 = await pool2.request().input("maNV", sql.VarChar, maNV).input("maKH", sql.VarChar, maKH).query(query);
        const hoadon3 = await pool3.request().input("maNV", sql.VarChar, maNV).input("maKH", sql.VarChar, maKH).query(query);

        const tong = [...hoadon1.recordset, ...hoadon2.recordset, ...hoadon3.recordset];
        return res.status(200).json({ success: true, hoatong: tong, message: "lấy dữ liệu hóa đơn thành công" })
    } catch (error) {
        console.log("loi lay danh sach hoa don : ", error);
        return res.status(500).json({ success: false, message: "lấy dữ liệu hóa đơn thất bại" });
    }
});
router.get('/cau2', verifyToken, async (req, res) => {

    const query = `USE DienLuc;
SELECT 
    hoadon.soHDN,
    hopdong.maKH,
    hopdong.kwDinhMuc,
    (hoadon.soTien / hopdong.donGiaKW) AS kwSuDung
FROM hoadon
JOIN hopdong ON hopdong.soHD = hoadon.soHD
WHERE (hoadon.soTien / hopdong.donGiaKW) > hopdong.kwDinhMuc;
  `;
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const hoadon1 = await pool1.request().query(query);
        const hoadon2 = await pool2.request().query(query);
        const hoadon3 = await pool3.request().query(query);
        const tong = [...hoadon1.recordset, ...hoadon2.recordset, ...hoadon3.recordset];
        return res.status(200).json({ success: true, hoatong: tong, message: "lấy dữ liệu hóa đơn thành công" })
    } catch (error) {
        console.log("loi lay danh sach hoa don : ", error);
        return res.status(500).json({ success: false, message: "lấy dữ liệu hóa đơn thất bại" });
    }
});
module.exports = router;