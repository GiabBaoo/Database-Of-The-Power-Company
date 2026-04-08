import fs from "fs";
import path from "path";

// ===============================
// CONFIG
// ===============================

const LOG_BASE = "D:/loki-logging/logs";
const SQL_OUTPUT_BASE = "D:/loki-logging/ai-sql-output";

// ===============================
// INPUT HANDLING
// ===============================
const [, , city, day, lastBackupTime] = process.argv;

if (!city || !day || !lastBackupTime) {
    console.log("❌ Thiếu tham số!");
    console.log("Cách chạy: node replay-generator.js TP1 2025-11-26 13:00:00");
    process.exit(1);
}

const logFilePath = path.join(LOG_BASE, `${city}-${day}.log`);
if (!fs.existsSync(logFilePath)) {
    console.error("❌ Không tìm thấy file log:", logFilePath);
    process.exit(1);
}

// Convert last backup time -> ISO
const lastBackupISO = `${day}T${lastBackupTime}.000Z`;

// ===============================
// READ & FILTER LOGS
// ===============================
console.log("📄 Đang đọc log:", logFilePath);

const rawLogs = fs.readFileSync(logFilePath, "utf8").trim().split("\n");
const filteredLogs = rawLogs.filter(line => {
    try {
        if (!line.trim()) return false;
        const json = JSON.parse(line);
        // Lọc log sau thời điểm backup và chỉ lấy các action quan trọng
        return json.timestamp > lastBackupISO && (json.level === 'insert' || json.level === 'update' || json.level === 'delete');
    } catch (e) { return false; }
});

if (filteredLogs.length === 0) {
    console.log("⚠ Không có log nào cần xử lý sau thời điểm backup.");
    process.exit(0);
}

console.log(`🔍 Tìm thấy ${filteredLogs.length} log cần replay.`);

// ===============================
// SCHEMA DEFINITION (Của bạn)
// ===============================
// Tôi đã minify schema để tiết kiệm token cho AI và giúp nó tập trung hơn
const dbSchema = `
CREATE TABLE Users(MaNV VARCHAR(20) PK, Email varchar(200), Role varchar(30));
CREATE TABLE lichSuChuyenCongTac(id INT PK, MaNV VARCHAR(20), MaKH VARCHAR(20), NgayChuyen datetime, maCNCu VARCHAR(20), maCNMoi VARCHAR(20));
CREATE TABLE chinhanh (maCN VARCHAR(20) PK, tenCN NVARCHAR(255), thanhpho VARCHAR(100));
CREATE TABLE nhanvien (maNV VARCHAR(20) PK, hoten NVARCHAR(255), maCN VARCHAR(20) FK);
CREATE TABLE khachhang (maKH VARCHAR(20) PK, tenKH NVARCHAR(255), maCN VARCHAR(20) FK);
CREATE TABLE hopdong (soHD VARCHAR(26) PK, ngayKy DATE, maKH VARCHAR(20), soDienKe VARCHAR(50), kwDinhMuc INT, dongiaKW int, isPaid bit);
CREATE TABLE hoadon (soHDN VARCHAR(26) PK, thang INT, nam INT, soHD VARCHAR(26) FK, maNV VARCHAR(20), soTien int);
`;

// ===============================
// AI PROMPT
// ===============================
const prompt = `
[ROLE]
Bạn là chuyên gia SQL Server (T-SQL) cho hệ thống quản lý điện lực.

[SCHEMA]
${dbSchema}

[NHIỆM VỤ]
Chuyển đổi các dòng log JSON bên dưới thành script SQL để đồng bộ dữ liệu (Replay Log).

[LOG INPUT]
${filteredLogs.join("\n")}

[QUY TẮC XỬ LÝ]
1. Dựa vào trường 'table' hoặc ngữ cảnh trong log để xác định bảng cần ghi.
2. Nếu log thiếu tên bảng, hãy suy luận:
   - "Thanh toán", "Tiền điện" -> update bảng 'hopdong' (set isPaid=1) hoặc insert 'hoadon'.
   - "Đăng ký", "Khách hàng" -> insert/update bảng 'khachhang' hoặc 'hopdong'.
   - "Chuyển công tác" -> insert bảng 'lichSuChuyenCongTac'.
3. Output chỉ chứa code SQL thuần túy, không Markdown, không giải thích.
4. Xử lý escape ký tự ' (dấu nháy đơn) cẩn thận cho chuỗi Unicode (N'string').
`;

// ===============================
// CALL OLLAMA API
// ===============================
console.log("🤖 Đang gửi request tới Qwen 2.5 Coder...");

async function runAI() {
    try {
        const response = await fetch("http://127.0.0.1:11434/api/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                model: "qwen2.5-coder", // Khớp với model qwen2.5-coder bạn đang có
                prompt: prompt,
                stream: false,
                options: {
                    temperature: 0.2,
                    num_ctx: 16384
                }
            }),
        });

        const data = await response.json();

        if (!data.response) {
            console.error("❌ Ollama trả về rỗng. Kiểm tra lại log input.");
            return;
        }

        const sqlResult = data.response;

        // ===============================
        // SAVE OUTPUT
        // ===============================
        const outputDir = path.join(SQL_OUTPUT_BASE, city);
        if (!fs.existsSync(outputDir)) fs.mkdirSync(outputDir, { recursive: true });

        const outputFile = path.join(outputDir, `${city}-${day}-replay.sql`);
        fs.writeFileSync(outputFile, sqlResult, "utf8");

        console.log("✅ Đã tạo file SQL thành công!");
        console.log("📂 File:", outputFile);

    } catch (error) {
        console.error("❌ Lỗi kết nối Ollama:", error.message);
        console.log("👉 Hãy chắc chắn bạn đã chạy lệnh: 'ollama serve'");
    }
}

runAI();