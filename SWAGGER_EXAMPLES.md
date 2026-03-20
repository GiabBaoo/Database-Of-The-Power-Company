/**
 * HƯỚNG DẪN THÊM SWAGGER JSDoc VÀO ROUTES
 * 
 * Format chung cho Swagger JSDoc:
 * 
 * @swagger
 * /endpoint:
 *   method (get/post/put/delete):
 *     summary: Mô tả ngắn
 *     description: Mô tả chi tiết
 *     tags:
 *       - TagName (nhóm API)
 *     security:
 *       - bearerAuth: [] (nếu cần token)
 *     parameters:
 *       - name: paramName
 *         in: query/path
 *         required: true
 *         schema:
 *           type: string
 *     requestBody: (nếu là POST/PUT)
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             $ref: '#/components/schemas/SchemaName'
 *     responses:
 *       200:
 *         description: Thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *       400:
 *         description: Lỗi request
 *       500:
 *         description: Lỗi server
 */

// ========== EXAMPLE 1: GET với query parameter ==========
/**
 * @swagger
 * /employee/customers:
 *   get:
 *     summary: Lấy danh sách khách hàng
 *     description: Lấy tất cả khách hàng của nhân viên từ 3 mảnh dữ liệu
 *     tags:
 *       - Employee - Customers
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - name: maNV
 *         in: query
 *         required: true
 *         description: Mã nhân viên
 *         schema:
 *           type: string
 *           example: NV001
 *     responses:
 *       200:
 *         description: Danh sách khách hàng
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 customers:
 *                   type: array
 *                   items:
 *                     $ref: '#/components/schemas/Customer'
 *       500:
 *         description: Lỗi server
 */
// router.get('/customers', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 2: POST với request body ==========
/**
 * @swagger
 * /employee/customers:
 *   post:
 *     summary: Thêm khách hàng mới
 *     description: Thêm một khách hàng mới vào hệ thống
 *     tags:
 *       - Employee - Customers
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - maKH
 *               - tenKH
 *               - maCN
 *               - thanhpho
 *             properties:
 *               maKH:
 *                 type: string
 *                 example: KH001
 *               tenKH:
 *                 type: string
 *                 example: Công ty Điện lực 1
 *               maCN:
 *                 type: string
 *                 example: CN001
 *               thanhpho:
 *                 type: string
 *                 enum: [TP1, TP2, TP3]
 *                 example: TP1
 *     responses:
 *       200:
 *         description: Thêm khách hàng thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 message:
 *                   type: string
 *                   example: Thêm khách hàng thành công
 *       500:
 *         description: Lỗi server
 */
// router.post('/customers', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 3: POST hợp đồng ==========
/**
 * @swagger
 * /employee/contracts:
 *   post:
 *     summary: Tạo hợp đồng mới
 *     description: Tạo một hợp đồng mới cho khách hàng
 *     tags:
 *       - Employee - Contracts
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - thanhpho
 *               - maKH
 *               - soDienKe
 *               - kwDinhMuc
 *               - dongiaKW
 *             properties:
 *               thanhpho:
 *                 type: string
 *                 enum: [TP1, TP2, TP3]
 *                 example: TP1
 *               maKH:
 *                 type: string
 *                 example: KH001
 *               soDienKe:
 *                 type: integer
 *                 example: 1001
 *               kwDinhMuc:
 *                 type: integer
 *                 example: 100
 *               dongiaKW:
 *                 type: integer
 *                 example: 2500
 *     responses:
 *       200:
 *         description: Tạo hợp đồng thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 message:
 *                   type: string
 *                   example: Tạo hợp đồng thành công
 *       500:
 *         description: Lỗi server
 */
// router.post('/contracts', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 4: POST hóa đơn ==========
/**
 * @swagger
 * /employee/bills:
 *   post:
 *     summary: Tạo hóa đơn thanh toán
 *     description: Tạo hóa đơn và cập nhật trạng thái hợp đồng là đã thanh toán
 *     tags:
 *       - Employee - Bills
 *     security:
 *       - bearerAuth: []
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             required:
 *               - thanhpho
 *               - soHD
 *               - maNV
 *               - soTien
 *             properties:
 *               thanhpho:
 *                 type: string
 *                 enum: [TP1, TP2, TP3]
 *                 example: TP1
 *               soHD:
 *                 type: string
 *                 example: HD001
 *               maNV:
 *                 type: string
 *                 example: NV001
 *               soTien:
 *                 type: integer
 *                 example: 250000
 *     responses:
 *       200:
 *         description: Tạo hóa đơn thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 message:
 *                   type: string
 *                   example: Tạo hóa đơn thành công
 *       500:
 *         description: Lỗi server
 */
// router.post('/bills', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 5: GET Admin Count Endpoints ==========
/**
 * @swagger
 * /admin/CountSite:
 *   get:
 *     summary: Đếm tổng số chi nhánh
 *     description: Đếm tất cả chi nhánh từ 3 mảnh dữ liệu (TP1, TP2, TP3)
 *     tags:
 *       - Admin - Dashboard
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Tổng số chi nhánh
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 totalCount:
 *                   type: integer
 *                   example: 5
 *       500:
 *         description: Lỗi server
 */
// router.get('/CountSite', verifyToken, async (req, res) => { ... })

/**
 * @swagger
 * /admin/CountCustomer:
 *   get:
 *     summary: Đếm tổng số khách hàng
 *     description: Đếm tất cả khách hàng từ 3 mảnh dữ liệu
 *     tags:
 *       - Admin - Dashboard
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Tổng số khách hàng
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 totalCount:
 *                   type: integer
 *                   example: 15
 *       500:
 *         description: Lỗi server
 */
// router.get('/CountCustomer', verifyToken, async (req, res) => { ... })

/**
 * @swagger
 * /admin/CountStaff:
 *   get:
 *     summary: Đếm tổng số nhân viên
 *     description: Đếm tất cả nhân viên từ 3 mảnh dữ liệu
 *     tags:
 *       - Admin - Dashboard
 *     security:
 *       - bearerAuth: []
 *     responses:
 *       200:
 *         description: Tổng số nhân viên
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 totalCount:
 *                   type: integer
 *                   example: 10
 *       500:
 *         description: Lỗi server
 */
// router.get('/CountStaff', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 6: DELETE endpoint ==========
/**
 * @swagger
 * /admin/sites/{id}:
 *   delete:
 *     summary: Xóa chi nhánh
 *     description: Xóa một chi nhánh (phải không có nhân viên/khách hàng)
 *     tags:
 *       - Admin - Sites
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - name: id
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *           example: CN001
 *     responses:
 *       200:
 *         description: Xóa chi nhánh thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 message:
 *                   type: string
 *                   example: Xóa chi nhánh thành công
 *       400:
 *         description: Không thể xóa (có nhân viên hoặc khách hàng)
 *       500:
 *         description: Lỗi server
 */
// router.delete('/sites/:id', verifyToken, async (req, res) => { ... })

// ========== EXAMPLE 7: PUT endpoint ==========
/**
 * @swagger
 * /admin/sites/{id}:
 *   put:
 *     summary: Cập nhật thông tin chi nhánh
 *     description: Sửa tên chi nhánh
 *     tags:
 *       - Admin - Sites
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - name: id
 *         in: path
 *         required: true
 *         schema:
 *           type: string
 *           example: CN001
 *     requestBody:
 *       required: true
 *       content:
 *         application/json:
 *           schema:
 *             type: object
 *             properties:
 *               tenCN:
 *                 type: string
 *                 example: Chi nhánh mới
 *               thanhpho:
 *                 type: string
 *                 enum: [TP1, TP2, TP3]
 *     responses:
 *       200:
 *         description: Cập nhật thành công
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                   example: true
 *                 message:
 *                   type: string
 *                   example: Cập nhật chi nhánh thành công
 *       500:
 *         description: Lỗi server
 */
// router.put('/sites/:id', verifyToken, async (req, res) => { ... })

// ========== SUPABASE ROUTES ==========
/**
 * @swagger
 * /tp3/customers:
 *   get:
 *     summary: Lấy khách hàng từ Supabase (TP3)
 *     description: Lấy danh sách khách hàng từ PostgreSQL Supabase
 *     tags:
 *       - Supabase (TP3)
 *     security:
 *       - bearerAuth: []
 *     parameters:
 *       - name: maNV
 *         in: query
 *         required: true
 *         schema:
 *           type: string
 *     responses:
 *       200:
 *         description: Danh sách khách hàng
 *         content:
 *           application/json:
 *             schema:
 *               type: object
 *               properties:
 *                 success:
 *                   type: boolean
 *                 customers:
 *                   type: array
 *                   items:
 *                     $ref: '#/components/schemas/Customer'
 */
// router.get('/tp3/customers', verifyToken, async (req, res) => { ... })
