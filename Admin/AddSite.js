// Admin/AddSite.js
const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const bcrypt = require('bcrypt');
const crypto = require('crypto');
const verifyToken = require('../src/Middleware/verifyToken');
const { tp1, tp2, tp3 } = require('../src/Config/logger');
router.post('/', verifyToken, async (req, res) => {
    const { maCN, tenCN, thanhpho } = req.body;
    try {
        let pool;
        if (thanhpho == 'TP1') {
            pool = await db.GetManh1DBPool();
            tp1.insert("thêm chi nhánh " + tenCN, { maCN: maCN, tenCN: tenCN, thanhpho: thanhpho })
        }
        else if (thanhpho == 'TP2') {
            pool = await db.GetManh2DBPool();
            tp2.insert("thêm chi nhánh " + tenCN, { maCN: maCN, tenCN: tenCN, thanhpho: thanhpho })
        }
        else {
            pool = await db.GetManh3DBPool();
            tp3.insert("thêm chi nhánh " + tenCN, { maCN: maCN, tenCN: tenCN, thanhpho: thanhpho })
        }

        const request = pool.request();
        request.input('MaCN', sql.VarChar, maCN);
        request.input('TenCN', sql.NVarChar, tenCN);
        request.input('ThanhPho', sql.NVarChar, thanhpho);
        const query = 'use DienLuc INSERT INTO ChiNhanh (maCN, tenCN, thanhpho) VALUES (@MaCN, @TenCN, @ThanhPho)';

        await request.query(query);

        return res.status(200).json({ success: true, message: "Thêm chi nhánh thành công" });
    } catch (error) {
        console.error("Lỗi khi thêm chi nhánh:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi thêm chi nhánh" });
    }
});

module.exports = router;