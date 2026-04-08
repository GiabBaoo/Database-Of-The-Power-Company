/**
 * Ví dụ query cho Supabase (TP3 - PostgreSQL)
 * File này hướng dẫn cách query từ các routes
 */

const express = require('express');
const router = express.Router();
const db = require('../src/Config/DBConnection');
const { executeQuery } = require('../src/Config/QueryHelper');
const { ulid } = require('ulid');
const { tp3 } = require('../src/Config/logger');
const verifyToken = require('../src/Middleware/verifyToken');

// ========================================
// TP3 SUPABASE - Lấy danh sách khách hàng
// ========================================
router.get('/tp3/customers', verifyToken, async (req, res) => {
    try {
        const maNV = req.query.maNV;
        
        // Supabase query (PostgreSQL)
        const pool3 = await db.GetManh3DBPool();
        
        // Cách 1: Dùng executeQuery helper (khuyến nghị)
        const query = `
            SELECT k.* 
            FROM khachhang k
            INNER JOIN nhanvien nv ON k."maCN" = nv."maCN"
            WHERE nv."maNV" = @maNV
        `;
        
        const result = await executeQuery(pool3, query, { maNV });
        
        return res.status(200).json({ 
            success: true, 
            customers: result.recordset 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi server Supabase" 
        });
    }
});

// ========================================
// TP3 SUPABASE - Thêm khách hàng
// ========================================
router.post('/tp3/customers', verifyToken, async (req, res) => {
    try {
        const { maKH, tenKH, maCN } = req.body;
        const pool3 = await db.GetManh3DBPool();
        
        const query = `
            INSERT INTO khachhang ("maKH", "tenKH", "maCN")
            VALUES (@maKH, @tenKH, @maCN)
        `;
        
        const result = await executeQuery(pool3, query, {
            maKH,
            tenKH,
            maCN
        });
        
        // Log vào Winston
        if (result.rowsAffected[0] > 0) {
            tp3.insert("Thêm khách hàng " + maKH, { maKH, tenKH, maCN });
        }
        
        return res.status(200).json({ 
            success: true, 
            message: "Thêm khách hàng thành công" 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi khi thêm khách hàng" 
        });
    }
});

// ========================================
// TP3 SUPABASE - Lấy hợp đồng
// ========================================
router.get('/tp3/contracts', verifyToken, async (req, res) => {
    try {
        const maNV = req.query.maNV;
        const pool3 = await db.GetManh3DBPool();
        
        const query = `
            SELECT hd."soHD", hd."maKH", k."tenKH", 
                   hd."ngayKy", hd."soDienKe", 
                   hd."kwDinhMuc", hd."dongiaKW", hd."isPaid"
            FROM hopdong hd
            INNER JOIN khachhang k ON hd."maKH" = k."maKH"
            INNER JOIN nhanvien nv ON nv."maCN" = k."maCN"
            WHERE nv."maNV" = @maNV
        `;
        
        const result = await executeQuery(pool3, query, { maNV });
        
        return res.status(200).json({ 
            success: true, 
            contracts: result.recordset 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi khi lấy hợp đồng" 
        });
    }
});

// ========================================
// TP3 SUPABASE - Tạo hợp đồng
// ========================================
router.post('/tp3/contracts', verifyToken, async (req, res) => {
    try {
        const { maKH, soDienKe, kwDinhMuc, dongiaKW } = req.body;
        const pool3 = await db.GetManh3DBPool();
        
        const soHD = ulid();
        
        const query = `
            INSERT INTO hopdong ("soHD", "maKH", "soDienKe", "kwDinhMuc", "dongiaKW")
            VALUES (@soHD, @maKH, @soDienKe, @kwDinhMuc, @dongiaKW)
        `;
        
        const result = await executeQuery(pool3, query, {
            soHD,
            maKH,
            soDienKe: parseInt(soDienKe),
            kwDinhMuc: parseInt(kwDinhMuc),
            dongiaKW: parseFloat(dongiaKW)
        });
        
        if (result.rowsAffected[0] > 0) {
            tp3.insert("Thêm hợp đồng " + soHD, { 
                soHD, maKH, soDienKe, kwDinhMuc, dongiaKW 
            });
        }
        
        return res.status(200).json({ 
            success: true, 
            message: "Tạo hợp đồng thành công",
            soHD 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi khi tạo hợp đồng" 
        });
    }
});

// ========================================
// TP3 SUPABASE - Tạo hóa đơn
// ========================================
router.post('/tp3/bills', verifyToken, async (req, res) => {
    try {
        const { soHD, maNV, soTien } = req.body;
        const pool3 = await db.GetManh3DBPool();
        
        const soHDN = ulid();
        const month = new Date().getMonth() + 1;
        const year = new Date().getFullYear();
        
        // Cập nhật hợp đồng: isPaid = true
        const updateQuery = `
            UPDATE hopdong SET "isPaid" = true WHERE "soHD" = @soHD
        `;
        
        await executeQuery(pool3, updateQuery, { soHD });
        
        // Thêm hóa đơn
        const insertQuery = `
            INSERT INTO hoadon ("soHDN", "thang", "nam", "soHD", "maNV", "soTien")
            VALUES (@soHDN, @thang, @nam, @soHD, @maNV, @soTien)
        `;
        
        const result = await executeQuery(pool3, insertQuery, {
            soHDN,
            thang: month,
            nam: year,
            soHD,
            maNV,
            soTien: parseFloat(soTien)
        });
        
        if (result.rowsAffected[0] > 0) {
            tp3.insert("Thêm hóa đơn " + soHDN, {
                soHDN, soHD, maNV, thang: month, nam: year, soTien
            });
        }
        
        return res.status(200).json({ 
            success: true, 
            message: "Tạo hóa đơn thành công",
            soHDN 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi khi tạo hóa đơn" 
        });
    }
});

// ========================================
// TP3 SUPABASE - Lấy hóa đơn
// ========================================
router.get('/tp3/bills', verifyToken, async (req, res) => {
    try {
        const maNV = req.query.maNV;
        const pool3 = await db.GetManh3DBPool();
        
        const query = `
            SELECT "soHDN", "thang", "nam", "soHD", "maNV", "soTien"
            FROM hoadon
            WHERE "maNV" = @maNV
            ORDER BY "nam" DESC, "thang" DESC
        `;
        
        const result = await executeQuery(pool3, query, { maNV });
        
        return res.status(200).json({ 
            success: true, 
            bills: result.recordset 
        });

    } catch (error) {
        console.error("❌ Lỗi Supabase:", error.message);
        return res.status(500).json({ 
            success: false, 
            message: "Lỗi khi lấy hóa đơn" 
        });
    }
});

module.exports = router;
