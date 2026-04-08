const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const { ulid } = require('ulid')
const { tp1, tp2, tp3 } = require('../src/Config/logger');
const verifyToken = require('../src/Middleware/verifyToken');
router.get("/contracts", verifyToken, async (req, res) => {
    const manv = req.query.maNV;
    try {
        const query = `
    use DienLuc
    SELECT hd.soHD,
           hd.maKH,
           kh.tenKH,
           hd.ngayKy,
           hd.soDienKe,
           hd.kwDinhMuc,
           hd.dongiaKW,
           hd.isPaid
    FROM hopdong hd
    INNER JOIN khachhang kh ON hd.maKH = kh.maKH
    INNER JOIN nhanvien nv ON nv.maCN = kh.maCN
    WHERE nv.maNV = @manv
`;

        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const result1 = await pool1.request().input("manv", sql.VarChar, manv).query(query);
        const result2 = await pool2.request().input("manv", sql.VarChar, manv).query(query);
        const result3 = await pool3.request().input("manv", sql.VarChar, manv).query(query);
        const hopdong1 = result1.recordset;
        const hopdong2 = result2.recordset;
        const hopdong3 = result3.recordset;
        const allcus = [...hopdong1, ...hopdong2, ...hopdong3]
        return res.status(200).json({ success: true, customers: allcus, message: "lấy danh sách hợp đồng thành công" });

    } catch (error) {
        console.log(error)
        return res.status(500).json({ success: false, message: "lấy danh sách hợp đồng thất bại" });
    }


});

router.post("/contracts", verifyToken, async (req, res) => {
    const { thanhpho, maKH, soDienKe, kwDinhMuc, dongiaKW } = req.body;
    try {
        const query = `
    use DienLuc
   insert into hopdong (soHD,maKH,soDienKe,kwDinhMuc,dongiaKW) values (@soHD,@maKH,@soDienKe,@kwDinhMuc,@dongiaKW)
`;

        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const soHD = ulid();
        if (thanhpho === "TP1") {
            console.log("runnnnnnnnnnnnnn1111111111111")
            const result1 = await pool1.request()
                .input("soHD", sql.VarChar, soHD)
                .input("maKH", sql.VarChar, maKH)
                .input("soDienKe", sql.Int, soDienKe)
                .input("kwDinhMuc", sql.Int, kwDinhMuc)
                .input("dongiaKW", sql.Int, dongiaKW)
                .query(query);
            if (result1.rowsAffected[0] > 0)
                tp1.insert("thêm hợp đồng" + soHD, { soHD: soHD, maKH: maKH, soDienKe: soDienKe, kwDinhMuc: kwDinhMuc, dongiaKW: dongiaKW });
        }

        if (thanhpho === "TP2") {
            const result2 = await pool2.request().input("soHD", sql.VarChar, soHD)
                .input("maKH", sql.VarChar, maKH)
                .input("soDienKe", sql.Int, soDienKe)
                .input("kwDinhMuc", sql.Int, kwDinhMuc)
                .input("dongiaKW", sql.Int, dongiaKW)
                .query(query);
            if (result2.rowsAffected[0] > 0)
                tp2.insert("thêm hợp đồng" + soHD, { soHD: soHD, maKH: maKH, soDienKe: soDienKe, kwDinhMuc: kwDinhMuc, dongiaKW: dongiaKW });
        }
        if (thanhpho === "TP3") {
            const result3 = await pool3.request().input("soHD", sql.VarChar, soHD)
                .input("maKH", sql.VarChar, maKH)
                .input("soDienKe", sql.Int, soDienKe)
                .input("kwDinhMuc", sql.Int, kwDinhMuc)
                .input("dongiaKW", sql.Int, dongiaKW)
                .query(query);
            if (result3.rowsAffected[0] > 0)
                tp3.insert("thêm hợp đồng" + soHD, { soHD: soHD, maKH: maKH, soDienKe: soDienKe, kwDinhMuc: kwDinhMuc, dongiaKW: dongiaKW });
        }

        console.log("toangggggggggg")
        return res.status(200).json({ success: true, message: "Thêm hợp đồng thành công" });

    } catch (error) {
        console.log(error)
        return res.status(500).json({ success: false, message: "Thêm hợp đồng thất bại" });
    }


});

module.exports = router;

