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
