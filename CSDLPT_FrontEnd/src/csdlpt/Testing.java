package csdlpt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Testing {
    public static void main(String[] args) {
        // Giả sử đây là chuỗi JSON bạn nhận được từ API hoặc Database
        String jsonResponse = "{\"staff\": [{\"id\": 1, \"name\": \"Tran\"}]}";
        
        try {
            JSONObject responseJson = new JSONObject(jsonResponse);
            
            // Dòng code gây lỗi trước đó đã được bao bọc trong try-catch
            JSONArray staffArr = responseJson.getJSONArray("staff");
            JSONObject staffObj = staffArr.getJSONObject(0);
            
            // In thử kết quả để kiểm tra
            System.out.println("Nhân viên đầu tiên: " + staffObj.toString());
            
        } catch (JSONException e) {
            // Xử lý khi cấu trúc JSON không đúng (thiếu key "staff" hoặc không phải Array)
            System.err.println("Lỗi xử lý JSON: " + e.getMessage());
        }
    }
}