const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../src/Config/DBConnection');
const verifyToken = require('../src/Middleware/verifyToken');
const { tp1, tp2, tp3 } = require('../src/Config/logger');
router.get('/customers', verifyToken, async (req, res) => {
    try {
        const maNV = req.query.maNV;
        const query = 'use DienLuc SELECT *  FROM khachhang,nhanvien where khachhang.maCN=nhanvien.maCN and nhanvien.maNV=@maNV';
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();

        const request1 = pool1.request();
        const request2 = pool2.request();
        const request3 = pool3.request();
        request1.input('maNV', sql.VarChar, maNV);
        request2.input('maNV', sql.VarChar, maNV);
        request3.input('maNV', sql.VarChar, maNV);
        const result2 = await request2.query(query);
        const result1 = await request1.query(query);
        const result3 = await request3.query(query);
        const customers2 = result2.recordset;
        const customers3 = result3.recordset;
        const customers1 = result1.recordset;
        const allCustomers = [...customers1, ...customers2, ...customers3];
        return res.status(200).json({ success: true, customers: allCustomers });
    } catch (error) {
        console.error("Lỗi khi xuất danh sách khách hàng:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi lấy danh sách khách hàng" });
    }
});
router.post('/customers', verifyToken, async (req, res) => {
    try {
        //đã có ở chi nhánh khác chưa
        const { maKH, tenKH, maCN, thanhpho } = req.body;
        // Nếu có thì check tổng xem có hợp đồng chưa thanh toán bên khác hay không và hợp đồng bao nhiêu
        const queryIsPaid = `use DienLuc
select * from hopdong where hopdong.isPaid=0 and maKH=@maKH`;

        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();


        const result1 = await pool1.request().input("maKH", sql.VarChar, maKH).query(queryIsPaid);
        const result2 = await pool2.request().input("maKH", sql.VarChar, maKH).query(queryIsPaid);
        const result3 = await pool3.request().input("maKH", sql.VarChar, maKH).query(queryIsPaid);
        if (result1.recordset.length !== 0 || result2.recordset.length !== 0 || result3.recordset.length !== 0) {
            const sum = [...result1.recordset, ...result2.recordset, ...result3.recordset];
            return res.status(200).json({ isAdded: false, success: true, message: "Đã có hợp đồng tại chi nhánh khác chưa thanh toán" });

        }

        const queryAdd = "insert khachhang (maKH,tenKH,maCN) values (@maKH, @tenKH,@maCN)";
        if (thanhpho === "TP1") {
            const resultKh1 = await pool1.request().input("maKH", sql.VarChar, maKH).input("tenKH", sql.NVarChar, tenKH).input("maCN", sql.VarChar, maCN).query(queryAdd);
            if (resultKh1.rowsAffected[0] > 0)
                tp1.insert("Thêm khách hàng " + maKH, { maKH: maKH, tenKH: tenKH, maCN: maCN });
        }
        if (thanhpho === "TP2") {
            const resultKh2 = await pool2.request().input("maKH", sql.VarChar, maKH).input("tenKH", sql.NVarChar, tenKH).input("maCN", sql.VarChar, maCN).query(queryAdd);
            if (resultKh2.rowsAffected[0] > 0)
                tp2.insert("Thêm khách hàng " + maKH, { maKH: maKH, tenKH: tenKH, maCN: maCN });

        }
        if (thanhpho === "TP3") {
            const resultKh3 = await pool3.request().input("maKH", sql.VarChar, maKH).input("tenKH", sql.NVarChar, tenKH).input("maCN", sql.VarChar, maCN).query(queryAdd);
            if (resultKh3.rowsAffected[0] > 0)
                tp3.insert("Thêm khách hàng " + maKH, { maKH: maKH, tenKH: tenKH, maCN: maCN });
        }

        //nếu không có hợp đồng hoặc toàn bộ được đã chi trả thì thêm khách hàng vào chi nhánh hiện tại
        return res.status(200).json({ isAdded: true, success: true, message: "Thêm khách hàng thành công" });
    } catch (error) {
        console.error("Lỗi khi thêm khách hàng:", error);
        return res.status(500).json({ isAdded: false, success: false, message: "Lỗi máy chủ khi thêm khách hàng" });
    }

});
router.put('/customers/:id', verifyToken, async (req, res) => {
    try {
        const customerId = req.params.id;
        const { tenKH, maNV } = req.body;
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const request1 = pool1.request();
        const request2 = pool2.request();
        const request3 = pool3.request();
        request1.input('maKH', sql.VarChar, customerId);
        request1.input('tenKH', sql.NVarChar, tenKH);
        request1.input('maNV', sql.VarChar, maNV);
        request2.input('maKH', sql.VarChar, customerId);
        request2.input('tenKH', sql.NVarChar, tenKH);
        request2.input('maNV', sql.VarChar, maNV);
        request3.input('maKH', sql.VarChar, customerId);
        request3.input('tenKH', sql.NVarChar, tenKH);
        request3.input('maNV', sql.VarChar, maNV);
        const query = 'use DienLuc UPDATE khachhang SET tenKH=@tenKH WHERE maKH=@maKH and maCN=(SELECT maCN FROM nhanvien WHERE maNV=@maNV)';
        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        if (result1.rowsAffected[0] > 0)
            tp1.update("Chỉnh sửa khách hàng " + customerId, { maKH: customerId, tenKH: tenKH });
        if (result2.rowsAffected[0] > 0)
            tp2.update("Chỉnh sửa khách hàng " + customerId, { maKH: customerId, tenKH: tenKH });
        if (result3.rowsAffected[0] > 0)
            tp3.update("Chỉnh sửa khách hàng " + customerId, { maKH: customerId, tenKH: tenKH });
        return res.status(200).json({ success: true, message: "chỉnh sửa khách hàng thành công" });
    } catch (error) {
        console.error("Lỗi khi thêm khách hàng:", error);
        return res.status(500).json({ success: false, message: "Lỗi máy chủ khi chỉnh sửa khách hàng" });
    }

}); router.delete('/customers/:id', verifyToken, async (req, res) => {
    try {
        const customerId = req.params.id;
        const { maNV } = req.body;
        const pool1 = await db.GetManh1DBPool();
        const pool2 = await db.GetManh2DBPool();
        const pool3 = await db.GetManh3DBPool();
        const request1 = pool1.request();
        const request2 = pool2.request();
        const request3 = pool3.request();
        request1.input('maKH', sql.VarChar, customerId);

        request1.input('maNV', sql.VarChar, maNV);
        request2.input('maKH', sql.VarChar, customerId);

        request2.input('maNV', sql.VarChar, maNV);
        request3.input('maKH', sql.VarChar, customerId);

        request3.input('maNV', sql.VarChar, maNV);
        const query = 'use DienLuc Delete khachhang  WHERE maKH=@maKH and maCN=(SELECT maCN FROM nhanvien WHERE maNV=@maNV)';
        const result1 = await request1.query(query);
        const result2 = await request2.query(query);
        const result3 = await request3.query(query);
        if (result1.rowsAffected[0] > 0)
            tp1.delete("Xóa khách hàng " + customerId, { maKH: customerId });
        if (result2.rowsAffected[0] > 0)
            tp2.delete("Xóa khách hàng " + customerId, { maKH: customerId });
        if (result3.rowsAffected[0] > 0)
            tp3.delete("Xóa khách hàng " + customerId, { maKH: customerId });
        return res.status(200).json({ isDeleted: true, success: true, message: "Xóa khách hàng thành công" });
    } catch (error) {
        console.error("Lỗi khi thêm khách hàng:", error);
        return res.status(500).json({ isDeleted: false, success: false, message: "Lỗi máy chủ khi Xóa khách hàng" });
    }

});

module.exports = router;