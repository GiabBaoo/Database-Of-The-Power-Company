// Import các logger đã export từ file logger.js
const { tp1, tp2, tp3 } = require('../src/Config/logger');

console.log("--- Bắt đầu ghi log thử nghiệm ---");

// 1. Test ghi log cho TP1 (Ví dụ: Thêm mới dữ liệu)
// Vì bạn đã định nghĩa level 'insert', winston sẽ tự tạo hàm .insert() cho bạn
tp1.insert("Thêm mới sinh viên vào hệ thống", {
    studentId: "SV001",
    name: "Nguyễn Văn A",
    action: "INSERT_DB"
});

// 2. Test ghi log cho TP2 (Ví dụ: Cập nhật dữ liệu)
tp2.update("Cập nhật thông tin học phí", {
    transactionId: "TRANS_999",
    amount: 5000000
});

// 3. Test ghi log cho TP3 (Ví dụ: Xóa dữ liệu)
tp3.delete("Xóa môn học bị hủy", {
    subjectId: "MH_LTM",
    user: "admin"
});

// 4. Test các level cơ bản khác (Info, Error)
tp1.info("Server TP1 đang chạy ổn định");
tp2.warn("Cảnh báo: Bộ nhớ sắp đầy");
tp3.error("Lỗi kết nối cơ sở dữ liệu", { errorCode: 500 });

console.log("--- Đã ghi log xong. Hãy kiểm tra thư mục D:/loki-logging/logs ---");