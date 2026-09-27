### **1\. Cấu trúc Mã nguồn Dùng chung (Shared Component)**

Hai bạn nên tạo một package chung (ví dụ: `com.library.common`) và copy package này vào dự án của cả 2 bên.

#### **a) Class hằng số** **ActionType.java** **(Định nghĩa tên hành động)**

Tránh việc người làm Client gửi `"LOGIN"` nhưng người làm Server lại chờ `"login"` gây lỗi:

```
package com.library.common;

public class ActionType {
    public static final String LOGIN = "LOGIN";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String SEARCH_BOOKS = "SEARCH_BOOKS";
    public static final String GET_BOOK_DETAIL = "GET_BOOK_DETAIL";
    public static final String CREATE_BORROW_TICKET = "CREATE_BORROW_TICKET";
    public static final String PROCESS_RETURN_BOOK = "PROCESS_RETURN_BOOK";
    public static final String RENEW_BOOK = "RENEW_BOOK";
    public static final String RESERVE_BOOK = "RESERVE_BOOK";
    public static final String GET_SYSTEM_CONFIG = "GET_SYSTEM_CONFIG";
    public static final String GET_REPORTS = "GET_REPORTS";
}

```

#### **b) Class tiện ích** **JsonUtil.java** **(Xử lý đóng gói &amp; giải mã JSON-P)**

Sử dụng **Object Model API (** **JsonObject** **,** **JsonReader** **,** **JsonObjectBuilder** **)** của thư viện JSON-P để sinh và đọc chuỗi JSON thống nhất[2]:

```
package com.library.common;

import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import java.io.StringReader;

public class JsonUtil {

    // Đóng gói JSON Request từ Client (Dạng Compact trên 1 dòng)
    public static String buildRequest(String action, JsonObject data) {
        JsonObjectBuilder builder = Json.createObjectBuilder().add("action", action);
        if (data != null) {
            builder.add("data", data);
        } else {
            builder.addNull("data");
        }
        return builder.build().toString(); // Chuỗi JSON chuẩn không xuống dòng
    }

    // Đóng gói JSON Response từ Server
    public static String buildResponse(String status, String message, JsonObject data) {
        JsonObjectBuilder builder = Json.createObjectBuilder()
                .add("status", status)
                .add("message", message);
        if (data != null) {
            builder.add("data", data);
        } else {
            builder.addNull("data");
        }
        return builder.build().toString();
    }

    // Parse chuỗi JSON nhận từ Socket
    public static JsonObject parseJson(String jsonString) {
        try (JsonReader reader = Json.createReader(new StringReader(jsonString))) {
            return reader.readObject();
        }
    }
}

```

---

### **2\. 4 Lưu ý Kỹ thuật Sống còn khi Truyền nhận JSON qua Socket**

#### 📌 **Lưu ý 1: JSON phải thu gọn trên 1 dòng (Compact JSON / Line-based Protocol)**

* Bộ đọc Socket ở cả 2 phía sử dụng `BufferedReader.readLine()`. Hàm này sẽ chờ gặp ký tự xuống dòng `\n` để kết thúc 1 thông điệp.
* Do đó, chuỗi JSON gửi qua Socket **tuyệt đối không được chứa ký tự xuống dòng thực tế (Pretty Printing)**[5][6]. Lớp `JsonObjectBuilder.build().toString()` của JSON-P mặc định sinh ra chuỗi nằm trọn trên 1 dòng[7].
* Khi gửi qua Socket, bắt buộc dùng `out.println(jsonString)` (để chèn `\n` ở cuối) và gọi `out.flush()`[8].

#### 📌 **Lưu ý 2: Bắt buộc cấu hình mã hóa UTF-8 cho tiếng Việt**

* Tên sách, tên độc giả và thông báo hiển thị đều có tiếng Việt có dấu[9].
* Cả Client và Server đều phải bọc Socket Stream bằng `StandardCharsets.UTF_8` để tránh bị lỗi hiển thị ký tự (`???`):

```
// Luồng đọc
BufferedReader in = new BufferedReader(
    new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

// Luồng ghi
PrintWriter out = new PrintWriter(
    new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);

```

#### 📌 **Lưu ý 3: Xử lý Bất đồng bộ ở GUI Client (Không làm treo giao diện)**

* Giao diện Java Swing / JavaFX chạy trên một luồng đặc biệt gọi là **Event Dispatch Thread (EDT)**[10].
* Việc chờ nhận dữ liệu từ Socket là thao tác I/O chặn (blocking IO)[1]. Nếu người làm Client gọi Socket trực tiếp trong sự kiện bấm nút (ví dụ `btnLogin.addActionListener`), **giao diện sẽ bị đơ/treo hoàn toàn (freeze)** trong lúc chờ Server trả lời[10][11].
* **Giải pháp:** Người làm Client phải chạy tác vụ gửi/nhận Socket trên luồng phụ (`SwingWorker` hoặc `Task`)[10][11].

#### 📌 **Lưu ý 4: Kiểm tra** **null** **an toàn khi đọc JSON-P**

* Trong JSON-P, nếu gọi `jsonObject.getString("key")` mà `key` đó có giá trị `null` hoặc không tồn tại trong chuỗi JSON, chương trình sẽ quăng lỗi `NullPointerException` hoặc `ClassCastException`[12][13].
* Cần kiểm tra an toàn trước khi truy xuất:

```
if (jsonObject.containsKey("data") &amp;&amp; !jsonObject.isNull("data")) {
    JsonObject data = jsonObject.getJsonObject("data");
    // Đọc tiếp các trường bên trong data...
}
```
