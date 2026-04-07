package csdlpt;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Tiện ích ghi log JSON để phục vụ cho công cụ AI Replay
 */
public class JsonLogger {

    private static final String LOG_BASE_DIR = "D:/loki-logging/logs";

    /**
     * Ghi log thao tác dữ liệu
     * 
     * @param branch Tên chi nhánh (ví dụ: TP1, TP2, TP3)
     * @param level Loại thao tác (insert, update, delete)
     * @param table Tên bảng bị tác động
     * @param data Map chứa dữ liệu (Key: tên cột, Value: giá trị)
     */
    public static void log(String branch, String level, String table, Map<String, Object> data) {
        try {
            // 1. Kiểm tra/Tạo thư mục log
            File dir = new File(LOG_BASE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. Xác định tên file: <Branch>-<YYYY-MM-DD>.log
            String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            String fileName = String.format("%s/%s-%s.log", LOG_BASE_DIR, branch, today);

            // 3. Tạo nội dung JSON
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"));
            StringBuilder json = new StringBuilder();
            json.append("{");
            json.append("\"timestamp\":\"").append(timestamp).append("\",");
            json.append("\"level\":\"").append(level.toLowerCase()).append("\",");
            json.append("\"table\":\"").append(table).append("\",");
            json.append("\"data\":{");
            
            int count = 0;
            for (Map.Entry<String, Object> entry : data.entrySet()) {
                json.append("\"").append(entry.getKey()).append("\":");
                Object val = entry.getValue();
                if (val instanceof String) {
                    json.append("\"").append(val.toString().replace("\"", "\\\"")).append("\"");
                } else {
                    json.append(val);
                }
                
                if (++count < data.size()) {
                    json.append(",");
                }
            }
            json.append("}}");

            // 4. Ghi file (nối thêm vào cuối file - append mode)
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName, true))) {
                writer.write(json.toString());
                writer.newLine();
                System.out.println("📝 [LOG] Đã ghi log tự động vào: " + fileName);
            }

        } catch (IOException e) {
            System.err.println("❌ Lỗi ghi log JSON: " + e.getMessage());
        }
    }
}
