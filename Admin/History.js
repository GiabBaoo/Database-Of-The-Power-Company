// Admin/History.js
const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const verifyToken = require('../src/Middleware/verifyToken');
router.get('/historyRecords', verifyToken, async (req, res) => {
    const query = `USE UsersCsdlPt select * from lichSuChuyenCongTac`
    try {

        let pool = await db.GetManh2UserDBPool();
        let result = await pool.request().query(query);
        const historyRecords = result.recordset;
        res.status(200).json({ success: true, history: historyRecords, message: 'History records fetched successfully' });
    } catch (error) {
        console.error('Error fetching history records:', error);
        res.status(500).json({ message: 'Internal server error' });
    }

}
);
module.exports = router;