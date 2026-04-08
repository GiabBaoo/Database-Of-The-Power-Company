const sql = require('mssql');

/**
 * Helper thực thi query với tự động nhận diện SQL Server hoặc PostgreSQL
 * @param {Object} pool - Connection pool (mssql hoặc pg)
 * @param {String} query - SQL query string
 * @param {Object} params - Parameters {paramName: value, ...}
 * @returns {Array} recordset
 */
const executeQuery = async (pool, query, params = {}) => {
    // Kiểm tra xem là SQL Server (mssql) hay PostgreSQL (pg)
    const isPostgres = pool.query && typeof pool.query === 'function' && !pool.request;
    
    if (isPostgres) {
        // PostgreSQL
        return await executePostgresQuery(pool, query, params);
    } else {
        // SQL Server
        return await executeMSSQLQuery(pool, query, params);
    }
};

/**
 * Thực thi query cho SQL Server (mssql)
 */
const executeMSSQLQuery = async (pool, query, params = {}) => {
    try {
        const request = pool.request();
        
        // Thêm parameters
        for (const [key, value] of Object.entries(params)) {
            // Xác định kiểu dữ liệu
            const type = getDataType(value);
            request.input(key, type, value);
        }
        
        const result = await request.query(query);
        return {
            recordset: result.recordset || [],
            rowsAffected: result.rowsAffected
        };
    } catch (err) {
        console.error("SQL Server Query Error:", err.message);
        throw err;
    }
};

/**
 * Thực thi query cho PostgreSQL
 */
const executePostgresQuery = async (client, query, params = {}) => {
    try {
        // Chuyển đổi MSSQL query format sang PostgreSQL format
        const { pgQuery, pgParams } = convertToPostgres(query, params);
        
        const result = await client.query(pgQuery, pgParams);
        
        return {
            recordset: result.rows || [],
            rowsAffected: [result.rowCount]
        };
    } catch (err) {
        console.error("PostgreSQL Query Error:", err.message);
        throw err;
    }
};

/**
 * Chuyển đổi MSSQL query sang PostgreSQL query
 * @param {String} query - MSSQL query (.../... @paramName ...)
 * @param {Object} params - Parameters
 * @returns {Object} {pgQuery, pgParams}
 */
const convertToPostgres = (query, params) => {
    let pgQuery = query;
    let paramIndex = 1;
    const paramNames = Object.keys(params);
    const pgParams = [];

    // Thay @paramName bằng $1, $2, ...
    paramNames.forEach(paramName => {
        const regex = new RegExp(`@${paramName}\\b`, 'gi');
        pgQuery = pgQuery.replace(regex, `$${paramIndex}`);
        pgParams.push(params[paramName]);
        paramIndex++;
    });

    // Xóa các câu lệnh USE (không cần trong PostgreSQL)
    pgQuery = pgQuery.replace(/use\s+\w+\s*;?/gi, '');

    // Xóa khoảng trắng thừa
    pgQuery = pgQuery.replace(/\s+/g, ' ').trim();

    return { pgQuery, pgParams };
};

/**
 * Xác định kiểu dữ liệu MSSQL từ giá trị
 */
const getDataType = (value) => {
    if (value === null || value === undefined) return sql.NVarChar;
    if (typeof value === 'number') {
        return Number.isInteger(value) ? sql.Int : sql.Float;
    }
    if (typeof value === 'boolean') return sql.Bit;
    if (value instanceof Date) return sql.DateTime;
    return sql.NVarChar;
};

module.exports = {
    executeQuery,
    executeMSSQLQuery,
    executePostgresQuery,
    convertToPostgres,
    getDataType
};
