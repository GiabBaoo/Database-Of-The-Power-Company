// Admin/Count.js
const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const { tp1, tp2, tp3 } = require('../src/Config/logger');
const verifyToken = require('../src/Middleware/verifyToken');
router.get('/sites', verifyToken, async (req, res) => {
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        request1 = pool1.request();
        request2 = pool2.request();
        request3 = pool3.request();
        const query = 'use DienLuc SELECT *  FROM chinhanh';
        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        const sites1 = result1.recordset;
        const sites2 = result2.recordset;
        const sites3 = result3.recordset;
        const allStaffs = [...sites1, ...sites2, ...sites3];
        return res.status(200).json({ success: true, sites: allStaffs });
    } catch (error) {
        console.error("Lỗi khi xuất danh sách chi nhánh:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách chi nhánh" });
    }
});
router.delete('/sites/:id', verifyToken, async (req, res) => {
    try {

        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        request1 = pool1.request();
        request2 = pool2.request();
        request3 = pool3.request();
        request1.input('MaCN', sql.VarChar, req.params.id);
        request2.input('MaCN', sql.VarChar, req.params.id);
        request3.input('MaCN', sql.VarChar, req.params.id);
        const query1 = 'use DienLuc select * from nhanvien,khachhang where nhanvien.maCN = @MaCN or khachhang.maCN = @MaCN';
        const check1 = await request1.query(query1);
        const check2 = await request2.query(query1);
        const check3 = await request3.query(query1);
        if (check1.recordset.length > 0 || check2.recordset.length > 0 || check3.recordset.length > 0) {
            return res.status(400).json({ isDeleted: false, success: false, message: "Không thể xóa chi nhánh vì có nhân viên hoặc khách hàng thuộc chi nhánh này" });
        }
        const query = 'use DienLuc delete chinhanh where maCN = @MaCN';

        const result1 = await pool1.request().input('MaCN', sql.VarChar, req.params.id).query(query);
        const result2 = await pool2.request().input('MaCN', sql.VarChar, req.params.id).query(query);
        const result3 = await pool3.request().input('MaCN', sql.VarChar, req.params.id).query(query);

        if (result1.rowsAffected[0] > 0)
            tp1.delete("Đã xóa chi nhánh " + req.params.id, { maCN: req.params.id })
        if (result2.rowsAffected[0] > 0)
            tp2.delete("Đã xóa chi nhánh " + req.params.id, { maCN: req.params.id })
        if (result3.rowsAffected[0] > 0)
            tp3.delete("Đã xóa chi nhánh " + req.params.id, { maCN: req.params.id })
        return res.status(200).json({ isDeleted: true, success: true, message: "Xóa chi nhánh thành công" });
    } catch (error) {
        console.error("Lỗi khi xuất danh sách chi nhánh:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách chi nhánh" });
    }
});
router.put('/sites/:id', verifyToken, async (req, res) => {
    try {
        const siteName = req.body.tenCN;
        const thanhpho = req.body.thanhpho;
        if (thanhpho == 'TP1') {
            const pool1 = await db.GetManh1DBPool();

            const request1 = pool1.request();
            request1.input('MaCN', sql.VarChar, req.params.id);
            request1.input('tenCN', sql.VarChar, siteName);
            const query = 'use DienLuc update chinhanh set tenCN = @tenCN where maCN = @MaCN';

            const result1 = await request1.query(query);
            if (request1.rowsAffected[0] > 0)
                tp1.update("Đã chỉnh sửa tên của chi nhánh " + req.params.id, { maCN: req.params.id, newName: tenCN });
            return res.status(200).json({ success: true, message: "Cập nhật chi nhánh thành công" });
        }
        if (thanhpho == 'TP2') {
            const pool2 = await db.GetManh2DBPool();
            const request2 = pool2.request();
            request2.input('MaCN', sql.VarChar, req.params.id);
            request2.input('tenCN', sql.VarChar, siteName);
            const query2 = 'use DienLuc update chinhanh set tenCN = @tenCN where maCN = @MaCN';
            const result2 = await request2.query(query2);
            if (request2.rowsAffected[0] > 0)
                tp2.update("Đã chỉnh sửa tên của chi nhánh" + req.params.id, { maCN: req.params.id, newName: tenCN });
            return res.status(200).json({ success: true, message: "Cập nhật chi nhánh thành công" });

        }
        if (thanhpho == 'TP3') {
            const pool3 = await db.GetManh3DBPool();
            const request3 = pool3.request();
            request3.input('MaCN', sql.VarChar, req.params.id);
            request3.input('tenCN', sql.VarChar, siteName);
            const query3 = 'use DienLuc update chinhanh set tenCN = @tenCN where maCN = @MaCN';
            const result3 = await request3.query(query3);
            if (request3.rowsAffected[0] > 0)
                tp3.update("Đã chỉnh sửa tên của chi nhánh" + req.params.id, { maCN: req.params.id, newName: tenCN });
            return res.status(200).json({ success: true, message: "Cập nhật chi nhánh thành công" });
        }



    } catch (error) {
        console.error("Lỗi khi đổi tên chi nhánh:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi đổi tên chi nhánh" });
    }
});

module.exports = router;
