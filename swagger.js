const swaggerJsdoc = require('swagger-jsdoc');
const swaggerUi = require('swagger-ui-express');

const options = {
    definition: {
        openapi: '3.0.0',
        info: {
            title: 'HỆ THỐNG QUẢN LÝ ĐIỆN LỰC - API Documentation',
            version: '1.0.0',
            description: 'API cho hệ thống quản lý dịch vụ điện lực với kiến trúc DB phân tán (TP1, TP2, TP3)',
            contact: {
                name: 'Development Team',
                email: 'dev@example.com',
            },
        },
        servers: [
            {
                url: 'http://localhost:9999',
                description: 'Development Server',
            },
            {
                url: 'http://your-production-url:9999',
                description: 'Production Server',
            },
        ],
        components: {
            securitySchemes: {
                bearerAuth: {
                    type: 'http',
                    scheme: 'bearer',
                    bearerFormat: 'JWT',
                    description: 'JWT token từ endpoint /login',
                },
            },
            schemas: {
                LoginRequest: {
                    type: 'object',
                    required: ['email', 'password'],
                    properties: {
                        email: {
                            type: 'string',
                            example: 'admin@example.com',
                        },
                        password: {
                            type: 'string',
                            example: '123456',
                        },
                    },
                },
                LoginResponse: {
                    type: 'object',
                    properties: {
                        success: { type: 'boolean', example: true },
                        token: {
                            type: 'string',
                            example: 'eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...',
                        },
                        role: { type: 'string', example: 'Admin' },
                        id: { type: 'string', example: 'NV001' },
                        message: { type: 'string', example: 'Đăng nhập thành công' },
                    },
                },
                Customer: {
                    type: 'object',
                    properties: {
                        maKH: { type: 'string', example: 'KH001' },
                        tenKH: { type: 'string', example: 'Công ty Điện lực 1' },
                        maCN: { type: 'string', example: 'CN001' },
                    },
                },
                Contract: {
                    type: 'object',
                    properties: {
                        soHD: { type: 'string', example: 'HD001' },
                        maKH: { type: 'string', example: 'KH001' },
                        tenKH: { type: 'string', example: 'Công ty Điện lực 1' },
                        soDienKe: { type: 'integer', example: 1001 },
                        kwDinhMuc: { type: 'integer', example: 100 },
                        dongiaKW: { type: 'number', example: 2500 },
                        isPaid: { type: 'boolean', example: false },
                    },
                },
                Bill: {
                    type: 'object',
                    properties: {
                        soHDN: { type: 'string', example: 'HDN001' },
                        thang: { type: 'integer', example: 3 },
                        nam: { type: 'integer', example: 2026 },
                        soHD: { type: 'string', example: 'HD001' },
                        maNV: { type: 'string', example: 'NV001' },
                        soTien: { type: 'number', example: 250000 },
                    },
                },
                ErrorResponse: {
                    type: 'object',
                    properties: {
                        success: { type: 'boolean', example: false },
                        message: { type: 'string', example: 'Lỗi server' },
                    },
                },
            },
        },
    },
    apis: [
        './src/Route/Login.js',
        './Staff/Customers.js',
        './Staff/Contract.js',
        './Staff/bills.js',
        './Admin/Count.js',
        './Admin/Sites.js',
        './TP3/Supabase_Example.js',
    ],
};

const specs = swaggerJsdoc(options);

const swaggerSetup = (app) => {
    app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(specs, {
        customCss: '.swagger-ui .topbar { display: none }',
        customSiteTitle: 'Điện Lực API Docs',
        swaggerOptions: {
            persistAuthorization: true,
        },
    }));
};

module.exports = swaggerSetup;
