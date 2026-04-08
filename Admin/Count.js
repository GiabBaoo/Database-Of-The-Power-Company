// Admin/Count.js
const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');

const verifyToken = require('../src/Middleware/verifyToken');
router.get('/CountSite', verifyToken, async (req, res) => {
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        request1 = pool1.request();
        request2 = pool2.request();
        request3 = pool3.request();
        const query = 'use DienLuc SELECT COUNT(*) AS count FROM chinhanh';

        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        const count1 = result1.recordset[0].count;
        const count2 = result2.recordset[0].count;
        const count3 = result3.recordset[0].count;
        const totalCount = count1 + count2 + count3;
        console.log("site count1" + count1);
        console.log("site count2" + count2);
        console.log("site count3" + count3);
        return res.status(200).json({ success: true, totalCount: totalCount });
    } catch (error) {
        console.error("Lỗi khi đếm số chi nhánh:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi đếm số chi nhánh" });
    }
});
router.get('/CountCustomer', verifyToken, async (req, res) => {
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        request1 = pool1.request();
        request2 = pool2.request();
        request3 = pool3.request();
        const query = 'use DienLuc SELECT COUNT(*) AS count FROM khachhang';

        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        const count1 = result1.recordset[0].count;
        const count2 = result2.recordset[0].count;
        const count3 = result3.recordset[0].count;
        const totalCount = count1 + count2 + count3;

        return res.status(200).json({ success: true, totalCount: totalCount });
    } catch (error) {
        console.error("Lỗi khi đếm số Khách hàng:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi đếm số khách hàng" });
    }
});

router.get('/CountStaff', verifyToken, async (req, res) => {
    try {
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        request1 = pool1.request();
        request2 = pool2.request();
        request3 = pool3.request();
        const query = 'use DienLuc SELECT COUNT(*) AS count FROM nhanvien';

        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        const count1 = result1.recordset[0].count;
        const count2 = result2.recordset[0].count;
        const count3 = result3.recordset[0].count;
        const totalCount = count1 + count2 + count3;

        return res.status(200).json({ success: true, totalCount: totalCount });
    } catch (error) {
        console.error("Lỗi khi đếm số Nhân Viên:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi đếm số Nhân viên" });
    }
});


module.exports = router;