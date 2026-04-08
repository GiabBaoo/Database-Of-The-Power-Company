const express = require('express');
const router = express.Router();
const sql = require('mssql');
const db = require('../Config/DBConnection');
const crypto = require('crypto');
const jwt = require('jsonwebtoken');
const verifyToken = require('../Middleware/verifyToken');

/**
 * @swagger
 * /login:
 *   post:
 *     summary: Đăng nhập hệ thống
 *     description: Xác thực email và password, trả về JWT token
 *     tags:
 *       - Authentication
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/LoginRequest'
 *     responses:
 *       200:
 *         description: Đăng nhập thành công
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/LoginResponse'
 *       401:
 *         description: Email hoặc mật khẩu sai
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: false
 *                 message:
 *                   type: string
 *                   example: Sai mật khẩu
 *       500:
 *         description: Lỗi server
 *         content:
 *           application/json:
 *             schema:
 *               $ref: '#/components/schemas/ErrorResponse'
 */
router.post('/', async (req, res) => {
    const { email, password } = req.body;
    try {
        const pool = await db.GetManh2UserDBPool();
        const request = pool.request();
        request.input('Email', sql.VarChar, email);
        
        // Query lấy thông tin user
        const query = 'use UsersCsdlPt SELECT * FROM Users WHERE Email = @Email';
        const result = await request.query(query);

        if (result.recordset.length > 0) {
            const user = result.recordset[0];

            // Lấy thông tin từ DB
            const MaNV = user.MaNV;
            const salt = user.Salt;
            const emailpassDB = String(user.Password).trim();
            const role = user.Role; // Lấy quyền (Admin/Staff)
            
            // Xử lý hash password để so sánh
            const passWithSalt = password + salt;
            const hashedPassword = crypto.createHash('sha512').update(passWithSalt).digest('hex');

            // Log để debug (bạn có thể xóa bớt khi đã chạy ổn)
            console.log("Input Password:", password);
            console.log("Salt from DB:", salt);
            console.log("Hashed Input:", hashedPassword);
            console.log("DB Password:", emailpassDB);

            // So sánh mật khẩu
            if (hashedPassword === emailpassDB) {
                console.log("Mật khẩu trùng khớp!");

                // Tạo token
                const token = jwt.sign(
                    { id: MaNV, email: email, role: role }, 
                    process.env.JWT_SECRET, 
                    { expiresIn: '1h' }
                );

                console.log("Role gửi đi:", role);

                // --- TRẢ VỀ KẾT QUẢ KÈM ROLE CHO JAVA ---
                return res.status(200).json({ 
                    token: token, 
                    role: role,
                    isLocked: false, 
                    success: true, 
                    message: "Đăng nhập thành công",
                    id: MaNV, 
                    MaNV: MaNV
                });

            } else {
                return res.status(401).json({ success: false, message: "Sai mật khẩu" });
            }

        } else {
            // Không tìm thấy email
            res.json({ exists: false, message: "Tài khoản không tồn tại" });
        }
    } catch (err) {
        console.error('Lỗi kiểm tra email:', err);
        res.status(500).json({ error: 'Lỗi server' });
    }
});

/**
 * @swagger
 * /login/protected:
 *   get:
 *     summary: Test route bảo vệ
 *     description: Route chỉ truy cập được khi có JWT token hợp lệ
 *     tags:
 *       - Authentication
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Truy cập thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 message:
 *                   type: string
 *                   example: This is a protected route
 *                 userId:
 *                   type: string
 *                   example: NV001
 *       401:
 *         description: Token không hợp lệ hoặc thiếu
 */
router.get('/protected', verifyToken, (req, res) => {
    res.json({ message: 'This is a protected route', userId: req.userId });
});

module.exports = router;