package csdlpt;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service quản lý đồng bộ giữa server chính và server backup
 * 
 * Chức năng:
 * 1. Ghi changelog khi thao tác trên backup server (khi primary sập)
 * 2. Sync dữ liệu từ backup → primary khi primary online lại
 * 3. Replicate dữ liệu từ primary → backup (initial sync)
 */
public class BackupSyncService {

    // Danh sách bảng cần replicate (theo thứ tự dependency)
    private static final String[] TABLES_ORDER = {
        "chinhanh", "nhanvien", "khachhang", "hopdong", "hoadon"
    };

    // ====== CHANGELOG - Ghi log thao tác khi dùng backup ======

    /**
     * Ghi log thao tác vào bảng _backup_changelog trên server backup
     * @param conn Connection tới backup server (SV4 hoặc SV5)
     * @param tableName Tên bảng đang thao tác
     * @param operation INSERT, UPDATE, hoặc DELETE
     * @param rowData JSON string chứa dữ liệu của row
     * @param isPostgres true nếu backup server là PostgreSQL (SV5)
     */
    public static void logToChangelog(Connection conn, String tableName, String operation, String rowData, boolean isPostgres) {
        if (conn == null) return;

        String sql;
        if (isPostgres) {
            sql = "INSERT INTO _backup_changelog (table_name, operation, row_data) VALUES (?, ?, ?)";
        } else {
            sql = "INSERT INTO _backup_changelog (table_name, operation, row_data) VALUES (?, ?, ?)";
        }

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, tableName);
            pstmt.setString(2, operation);
            pstmt.setString(3, rowData);
            pstmt.executeUpdate();
            System.out.println("📝 Changelog: " + operation + " " + tableName + " → " + rowData);
        } catch (SQLException e) {
            System.err.println("❌ Lỗi ghi changelog: " + e.getMessage());
        }
    }

    /**
     * Tạo JSON string đơn giản từ key-value pairs
     */
    public static String buildJsonData(String... keyValues) {
        StringBuilder sb = new StringBuilder("{");
        for (int i = 0; i < keyValues.length; i += 2) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(keyValues[i]).append("\":\"")
              .append(keyValues[i + 1] != null ? keyValues[i + 1].replace("\"", "\\\"") : "")
              .append("\"");
        }
        sb.append("}");
        return sb.toString();
    }

    // ====== SYNC BACKUP → PRIMARY ======

    /**
     * Đồng bộ dữ liệu từ SV4 (MSSQL) → TP1 (MSSQL)
     * Đọc _backup_changelog của SV4 và replay lên TP1
     */
    public static void syncBackupToTP1() {
        System.out.println("\n🔄 ========== BẮT ĐẦU ĐỒNG BỘ SV4 → TP1 ==========");
        
        Connection sv4Conn = DatabaseConnection.getSV4Connection();
        Connection tp1Conn = DatabaseConnection.getTP1DirectConnection();
        
        if (sv4Conn == null) {
            System.err.println("❌ Không kết nối được SV4 để đọc changelog");
            return;
        }
        if (tp1Conn == null) {
            System.err.println("❌ Không kết nối được TP1 để sync");
            return;
        }

        try {
            // Đọc changelog chưa sync (ORDER BY id ASC để replay đúng thứ tự)
            String selectSql = "SELECT id, table_name, operation, row_data FROM _backup_changelog WHERE synced = 0 ORDER BY id ASC";
            List<Map<String, String>> changes = new ArrayList<>();
            
            try (Statement stmt = sv4Conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    Map<String, String> change = new HashMap<>();
                    change.put("id", rs.getString("id"));
                    change.put("table_name", rs.getString("table_name"));
                    change.put("operation", rs.getString("operation"));
                    change.put("row_data", rs.getString("row_data"));
                    changes.add(change);
                }
            }

            if (changes.isEmpty()) {
                System.out.println("✨ Không có dữ liệu tồn đọng từ phiên Failover trước đó trên SV4.");
                tp1Conn.close();
                return;
            }

            System.out.println("📋 Tìm thấy " + changes.size() + " thay đổi cần đồng bộ");

            int successCount = 0;
            for (Map<String, String> change : changes) {
                boolean success = replayChange(tp1Conn, change, false);
                if (success) {
                    // Đánh dấu đã sync
                    markSynced(sv4Conn, change.get("id"), false);
                    successCount++;
                }
            }

            System.out.println("✅ Đồng bộ hoàn tất: " + successCount + "/" + changes.size() + " thay đổi");
            tp1Conn.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đồng bộ SV4 → TP1: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🔄 ========== KẾT THÚC ĐỒNG BỘ SV4 → TP1 ==========\n");
    }

    /**
     * Đồng bộ dữ liệu từ SV5 (PostgreSQL) → TP2 (MSSQL)
     * Cross-database sync: PostgreSQL → MSSQL
     */
    public static void syncBackupToTP2() {
        System.out.println("\n🔄 ========== BẮT ĐẦU ĐỒNG BỘ SV5 → TP2 ==========");
        
        Connection sv5Conn = DatabaseConnection.getSV5Connection();
        Connection tp2Conn = DatabaseConnection.getTP2DirectConnection();
        
        if (sv5Conn == null) {
            System.err.println("❌ Không kết nối được SV5 để đọc changelog");
            return;
        }
        if (tp2Conn == null) {
            System.err.println("❌ Không kết nối được TP2 để sync");
            return;
        }

        try {
            // Xác định siteIndex cho TP2/SV5 (thường là 1)
            boolean pg = DatabaseConnection.isPostgresSite(1);
            String syncedVal = pg ? "false" : "0";
            String selectSql = "SELECT id, table_name, operation, row_data FROM _backup_changelog WHERE synced = " + syncedVal + " ORDER BY id ASC";
            
            List<Map<String, String>> changes = new ArrayList<>();
            
            try (Statement stmt = sv5Conn.createStatement();
                 ResultSet rs = stmt.executeQuery(selectSql)) {
                while (rs.next()) {
                    Map<String, String> change = new HashMap<>();
                    change.put("id", rs.getString("id"));
                    change.put("table_name", rs.getString("table_name"));
                    change.put("operation", rs.getString("operation"));
                    change.put("row_data", rs.getString("row_data"));
                    changes.add(change);
                }
            }

            if (changes.isEmpty()) {
                System.out.println("✨ Không có dữ liệu tồn đọng từ phiên Failover trước đó trên SV5.");
                tp2Conn.close();
                return;
            }

            System.out.println("📋 Tìm thấy " + changes.size() + " thay đổi cần đồng bộ");

            int successCount = 0;
            for (Map<String, String> change : changes) {
                // SV5 (Somee) là MSSQL nên isPostgres = false
                boolean success = replayChange(tp2Conn, change, false);
                if (success) {
                    // SV5 hiện tại là MSSQL nên markSynced với isPostgres = false
                    markSynced(sv5Conn, change.get("id"), false);
                    successCount++;
                }
            }

            System.out.println("✅ Đồng bộ hoàn tất: " + successCount + "/" + changes.size() + " thay đổi");
            tp2Conn.close();

        } catch (SQLException e) {
            System.err.println("❌ Lỗi đồng bộ SV5 → TP2: " + e.getMessage());
            e.printStackTrace();
        }
        
        System.out.println("🔄 ========== KẾT THÚC ĐỒNG BỘ SV5 → TP2 ==========\n");
    }

    /**
     * Replay một thay đổi từ changelog lên server đích
     */
    private static boolean replayChange(Connection targetConn, Map<String, String> change, boolean targetIsPostgres) {
        String tableName = change.get("table_name");
        String operation = change.get("operation");
        String rowData = change.get("row_data");
        
        Map<String, String> data = parseSimpleJson(rowData);
        if (data == null || data.isEmpty()) {
            System.err.println("⚠️ Could not parse JSON data for: " + tableName);
            return false;
        }

        try {
            String sql = null;
            switch (operation.toUpperCase()) {
                case "INSERT":
                    sql = buildInsertSql(tableName, data, targetIsPostgres);
                    break;
                case "UPDATE":
                    sql = buildUpdateSql(tableName, data, targetIsPostgres);
                    break;
                case "DELETE":
                    sql = buildDeleteSql(tableName, data, targetIsPostgres);
                    break;
                default:
                    System.err.println("⚠️ Unknown operation: " + operation);
                    return false;
            }
            if (sql == null) {
                System.err.println("⚠️ Could not build SQL for: " + operation + " " + tableName);
                return false;
            }

            try (Statement stmt = targetConn.createStatement()) {
                stmt.executeUpdate(sql);
                System.out.println("  ✅ Replay: " + operation + " " + tableName);
                return true;
            }
        } catch (SQLException e) {
            System.err.println("  ❌ Lỗi replay " + operation + " " + tableName + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Replay một thay đổi từ changelog lên server đích
     */
    private static String buildInsertSql(String tableName, Map<String, String> data, boolean isPostgres) {
        StringBuilder cols = new StringBuilder();
        StringBuilder vals = new StringBuilder();
        
        for (Map.Entry<String, String> entry : data.entrySet()) {
            if (entry.getKey().startsWith("_pk_")) continue; // Skip metadata
            if (cols.length() > 0) { cols.append(", "); vals.append(", "); }
            
            // PostgreSQL columns are lowercase
            String colName = isPostgres ? entry.getKey().toLowerCase() : entry.getKey();
            cols.append(colName);
            
            // MSSQL needs N prefix for unicode, PostgreSQL doesn't
            if (isPostgres) {
                vals.append("'").append(entry.getValue().replace("'", "''")).append("'");
            } else {
                vals.append("N'").append(entry.getValue().replace("'", "''")).append("'");
            }
        }
        
        return "INSERT INTO " + tableName + " (" + cols + ") VALUES (" + vals + ")";
    }

    private static String buildUpdateSql(String tableName, Map<String, String> data, boolean isPostgres) {
        // Determine primary key column based on DB type
        String pkColumn = getPrimaryKeyColumn(tableName, isPostgres);
        String pkValue = data.get(pkColumn);
        if (pkValue == null) return null;

        StringBuilder setClause = new StringBuilder();
        for (Map.Entry<String, String> entry : data.entrySet()) {
            // Columns must match casing
            String colName = isPostgres ? entry.getKey().toLowerCase() : entry.getKey();
            if (colName.equals(pkColumn)) continue;
            if (entry.getKey().startsWith("_pk_")) continue;
            if (setClause.length() > 0) setClause.append(", ");
            
            if (isPostgres) {
                setClause.append(colName).append(" = '")
                         .append(entry.getValue().replace("'", "''")).append("'");
            } else {
                setClause.append(colName).append(" = N'")
                         .append(entry.getValue().replace("'", "''")).append("'");
            }
        }

        if (isPostgres) {
            return "UPDATE " + tableName + " SET " + setClause + " WHERE " + pkColumn + " = '" + pkValue.replace("'", "''") + "'";
        } else {
            return "UPDATE " + tableName + " SET " + setClause + " WHERE " + pkColumn + " = N'" + pkValue.replace("'", "''") + "'";
        }
    }

    private static String buildDeleteSql(String tableName, Map<String, String> data, boolean isPostgres) {
        String pkColumn = getPrimaryKeyColumn(tableName, isPostgres);
        String pkValue = data.get(isPostgres ? pkColumn : getPrimaryKeyColumn(tableName, false));
        // Fallback for pkValue if parsing case was different
        if (pkValue == null) {
            for (String key : data.keySet()) {
                if (key.equalsIgnoreCase(pkColumn)) {
                    pkValue = data.get(key);
                    break;
                }
            }
        }
        if (pkValue == null) return null;

        if (isPostgres) {
            return "DELETE FROM " + tableName + " WHERE " + pkColumn + " = '" + pkValue.replace("'", "''") + "'";
        } else {
            return "DELETE FROM " + tableName + " WHERE " + pkColumn + " = N'" + pkValue.replace("'", "''") + "'";
        }
    }

    private static String getPrimaryKeyColumn(String tableName, boolean isPostgres) {
        String lowerTable = tableName.toLowerCase();
        // Determine column name based on DB type
        if (isPostgres) {
            switch (lowerTable) {
                case "chinhanh": return "macn";
                case "nhanvien": return "manv";
                case "khachhang": return "makh";
                case "hopdong": return "sohd";
                case "hoadon": return "sohdn";
                default: return "id";
            }
        } else {
            switch (lowerTable) {
                case "chinhanh": return "maCN";
                case "nhanvien": return "maNV";
                case "khachhang": return "maKH";
                case "hopdong": return "soHD";
                case "hoadon": return "soHDN";
                default: return "id";
            }
        }
    }

    /**
     * Đánh dấu changelog entry đã được sync
     */
    private static void markSynced(Connection backupConn, String id, boolean isPostgres) {
        String sql;
        if (isPostgres) {
            sql = "UPDATE _backup_changelog SET synced = true WHERE id = " + id;
        } else {
            sql = "UPDATE _backup_changelog SET synced = 1 WHERE id = " + id;
        }
        
        try (Statement stmt = backupConn.createStatement()) {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            System.err.println("⚠️ Lỗi đánh dấu synced cho changelog id=" + id);
        }
    }

    // ====== INITIAL DATA REPLICATION ======

    /**
     * Copy toàn bộ dữ liệu từ TP1 → SV4 (Initial sync)
     * Gọi khi khởi tạo lần đầu hoặc khi cần reset backup
     */
    public static boolean replicateTP1ToSV4() {
        System.out.println("\n📦 ========== REPLICATE TP1 → SV4 ==========");
        Connection tp1 = DatabaseConnection.getTP1DirectConnection();
        Connection sv4 = DatabaseConnection.getSV4Connection();

        if (tp1 == null) {
            System.err.println("❌ Không thể kết nối tới TP1 Direct. Kiểm tra lại thông tin đăng nhập trong .env!");
            return false;
        }
        if (sv4 == null) {
            System.err.println("❌ Không thể kết nối tới SV4 (Backup TP1)!");
            return false;
        }

        try {
            // Xóa dữ liệu cũ trên SV4 (ngược thứ tự dependency)
            String[] deleteOrder = {"hoadon", "hopdong", "khachhang", "nhanvien", "chinhanh"};
            for (String table : deleteOrder) {
                try (Statement stmt = sv4.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + table);
                    System.out.println("  🗑️ Xóa dữ liệu " + table + " trên SV4");
                } catch (SQLException e) {
                    System.err.println("  ⚠️ Lỗi xóa " + table + " trên SV4: " + e.getMessage());
                }
            }

            // Copy từng bảng theo thứ tự dependency
            replicateTableMssqlToMssql(tp1, sv4, "chinhanh", "maCN, tenCN, thanhpho");
            replicateTableMssqlToMssql(tp1, sv4, "nhanvien", "maNV, hoten, maCN, password, role");
            replicateTableMssqlToMssql(tp1, sv4, "khachhang", "maKH, tenKH, maCN");
            replicateTableMssqlToMssql(tp1, sv4, "hopdong", "soHD, maKH, soDienKe, kwDinhMuc, dongiaKW");
            replicateTableMssqlToMssql(tp1, sv4, "hoadon", "soHDN, thang, nam, soHD, maNV, soTien");

            // Xóa changelog cũ
            try (Statement stmt = sv4.createStatement()) {
                stmt.executeUpdate("DELETE FROM _backup_changelog");
            } catch (SQLException ignored) {}

            tp1.close();
            System.out.println("✅ Replicate TP1 → SV4 hoàn tất!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi replicate TP1 → SV4: " + e.getMessage());
            return false;
        } finally {
            System.out.println("📦 ==========================================\n");
        }
    }

    /**
     * Copy toàn bộ dữ liệu từ TP2 → SV5 (MSSQL → PostgreSQL)
     */
    public static boolean replicateTP2ToSV5() {
        System.out.println("\n📦 ========== REPLICATE TP2 → SV5 ==========");
        Connection tp2 = DatabaseConnection.getTP2DirectConnection();
        Connection sv5 = DatabaseConnection.getSV5Connection();

        if (tp2 == null) {
            System.err.println("❌ Không thể kết nối tới TP2 Direct (Source). Kiểm tra mật khẩu tại DatabaseConnection.java:24!");
            return false;
        }
        if (sv5 == null) {
            System.err.println("❌ Không thể kết nối tới SV5 (Backup TP2)!");
            return false;
        }

        try {
            // Xóa dữ liệu cũ trên SV5 (PostgreSQL)
            String[] deleteOrder = {"hoadon", "hopdong", "khachhang", "nhanvien", "chinhanh"};
            for (String table : deleteOrder) {
                try (Statement stmt = sv5.createStatement()) {
                    stmt.executeUpdate("DELETE FROM " + table);
                    System.out.println("  🗑️ Xóa dữ liệu " + table + " trên SV5");
                } catch (SQLException e) {
                    System.err.println("  ⚠️ Lỗi xóa " + table + " trên SV5: " + e.getMessage());
                }
            }

            // Copy từng bảng (MSSQL → MSSQL)
            replicateTableMssqlToMssql(tp2, sv5, "chinhanh", "maCN, tenCN, thanhpho");
            replicateTableMssqlToMssql(tp2, sv5, "nhanvien", "maNV, hoten, maCN, password, role");
            replicateTableMssqlToMssql(tp2, sv5, "khachhang", "maKH, tenKH, maCN");
            replicateTableMssqlToMssql(tp2, sv5, "hopdong", "soHD, maKH, soDienKe, kwDinhMuc, dongiaKW");
            replicateTableMssqlToMssql(tp2, sv5, "hoadon", "soHDN, thang, nam, soHD, maNV, soTien");

            // Xóa changelog cũ
            try (Statement stmt = sv5.createStatement()) {
                stmt.executeUpdate("DELETE FROM _backup_changelog");
            } catch (SQLException ignored) {}

            tp2.close();
            System.out.println("✅ Replicate TP2 → SV5 hoàn tất!");
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Lỗi replicate TP2 → SV5: " + e.getMessage());
            return false;
        } finally {
            System.out.println("📦 ==========================================\n");
        }
    }

    /**
     * Copy dữ liệu bảng từ MSSQL → MSSQL
     */
    private static void replicateTableMssqlToMssql(Connection source, Connection target, String tableName, String columns) {
        String selectSql = "SELECT " + columns + " FROM " + tableName;
        int count = 0;

        try (Statement stmt = source.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                StringBuilder vals = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) vals.append(", ");
                    String val = rs.getString(i);
                    if (val == null) {
                        vals.append("NULL");
                    } else {
                        // Kiểm tra kiểu dữ liệu numeric
                        int colType = meta.getColumnType(i);
                        if (colType == Types.INTEGER || colType == Types.FLOAT || 
                            colType == Types.DOUBLE || colType == Types.DECIMAL ||
                            colType == Types.NUMERIC || colType == Types.BIGINT ||
                            colType == Types.SMALLINT || colType == Types.REAL) {
                            vals.append(val);
                        } else {
                            vals.append("N'").append(val.replace("'", "''")).append("'");
                        }
                    }
                }

                String insertSql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + vals + ")";
                try (Statement insertStmt = target.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    count++;
                }
            }
            System.out.println("  📋 " + tableName + ": " + count + " bản ghi");
        } catch (SQLException e) {
            System.err.println("  ❌ Lỗi replicate " + tableName + ": " + e.getMessage());
        }
    }

    /**
     * Copy dữ liệu bảng từ MSSQL → PostgreSQL (cross-database)
     */
    private static void replicateTableMssqlToPostgres(Connection source, Connection target, String tableName, String columns) {
        String selectSql = "SELECT " + columns + " FROM " + tableName;
        int count = 0;

        try (Statement stmt = source.createStatement();
             ResultSet rs = stmt.executeQuery(selectSql)) {
            
            ResultSetMetaData meta = rs.getMetaData();
            int colCount = meta.getColumnCount();

            while (rs.next()) {
                StringBuilder vals = new StringBuilder();
                for (int i = 1; i <= colCount; i++) {
                    if (i > 1) vals.append(", ");
                    String val = rs.getString(i);
                    if (val == null) {
                        vals.append("NULL");
                    } else {
                        int colType = meta.getColumnType(i);
                        if (colType == Types.INTEGER || colType == Types.FLOAT || 
                            colType == Types.DOUBLE || colType == Types.DECIMAL ||
                            colType == Types.NUMERIC || colType == Types.BIGINT ||
                            colType == Types.SMALLINT || colType == Types.REAL) {
                            vals.append(val);
                        } else {
                            // PostgreSQL dùng E'...' hoặc '...' (không có N prefix)
                            vals.append("'").append(val.replace("'", "''")).append("'");
                        }
                    }
                }

                String insertSql = "INSERT INTO " + tableName + " (" + columns + ") VALUES (" + vals + ")";
                try (Statement insertStmt = target.createStatement()) {
                    insertStmt.executeUpdate(insertSql);
                    count++;
                }
            }
            System.out.println("  📋 " + tableName + ": " + count + " bản ghi");
        } catch (SQLException e) {
            System.err.println("  ❌ Lỗi replicate " + tableName + " (MSSQL→PG): " + e.getMessage());
        }
    }

    // ====== JSON PARSER (Simple) ======

    /**
     * Parse JSON đơn giản {"key1":"val1","key2":"val2"} 
     * Không dùng thư viện bên ngoài
     */
    public static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        if (json == null || json.isEmpty()) return map;
        
        // Loại bỏ { } ngoài cùng
        json = json.trim();
        if (json.startsWith("{")) json = json.substring(1);
        if (json.endsWith("}")) json = json.substring(0, json.length() - 1);
        
        // Split theo pattern ","  (giữa các cặp key-value)
        int i = 0;
        while (i < json.length()) {
            // Tìm key
            int keyStart = json.indexOf("\"", i);
            if (keyStart == -1) break;
            int keyEnd = json.indexOf("\"", keyStart + 1);
            if (keyEnd == -1) break;
            String key = json.substring(keyStart + 1, keyEnd);
            
            // Tìm ":"
            int colonIdx = json.indexOf(":", keyEnd + 1);
            if (colonIdx == -1) break;
            
            // Tìm value
            int valStart = json.indexOf("\"", colonIdx + 1);
            if (valStart == -1) break;
            
            // Tìm end quote (xử lý escaped quotes)
            int valEnd = valStart + 1;
            while (valEnd < json.length()) {
                if (json.charAt(valEnd) == '"' && json.charAt(valEnd - 1) != '\\') {
                    break;
                }
                valEnd++;
            }
            
            String value = json.substring(valStart + 1, valEnd).replace("\\\"", "\"");
            map.put(key, value);
            
            i = valEnd + 1;
        }
        
        return map;
    }
}
