package com.library.common;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

import jakarta.json.JsonArray;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public final class JsonParams {

    private JsonParams() {
    }

    private static BadRequestException bad(String key) {
        return new BadRequestException("Thiếu hoặc sai tham số: " + key);
    }

    // Trả về null nếu thiếu key hoặc giá trị là JSON null
    private static JsonValue get(JsonObject d, String key) {
        if (d == null || !d.containsKey(key) || d.isNull(key)) {
            return null;
        }
        return d.get(key);
    }

    // ---------- Chuỗi ----------

    public static String reqString(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null || v.getValueType() != ValueType.STRING) {
            throw bad(key);
        }
        String s = ((JsonString) v).getString();
        if (s.trim().isEmpty()) {
            throw bad(key);
        }
        return s;
    }

    public static String optString(JsonObject d, String key, String def) {
        JsonValue v = get(d, key);
        if (v == null) {
            return def;
        }
        if (v.getValueType() != ValueType.STRING) {
            throw bad(key);
        }
        return ((JsonString) v).getString();
    }

    // ---------- Số nguyên ----------

    public static int reqInt(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null || v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).intValue();
    }

    public static int optInt(JsonObject d, String key, int def) {
        JsonValue v = get(d, key);
        if (v == null) {
            return def;
        }
        if (v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).intValue();
    }

    // Trả về null nếu không gửi
    public static Integer optInteger(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null) {
            return null;
        }
        if (v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).intValue();
    }

    // ---------- Số thực ----------

    public static double reqDouble(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null || v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).doubleValue();
    }

    public static double optDouble(JsonObject d, String key, double def) {
        JsonValue v = get(d, key);
        if (v == null) {
            return def;
        }
        if (v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).doubleValue();
    }

    // ---------- Mảng chuỗi ----------

    public static List<String> reqStringList(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null || v.getValueType() != ValueType.ARRAY) {
            throw bad(key);
        }
        List<String> ketQua = new ArrayList<>();
        for (JsonValue item : (JsonArray) v) {
            if (item.getValueType() != ValueType.STRING) {
                throw bad(key);
            }
            ketQua.add(((JsonString) item).getString());
        }
        return ketQua;
    }

    // ---------- Phân trang (mục 1.5 của API.md) ----------

    public static int pageOf(JsonObject d) {
        return Math.max(1, optInt(d, "page", 1));
    }

    public static int pageSizeOf(JsonObject d) {
        return Math.min(100, Math.max(1, optInt(d, "pageSize", 10)));
    }

    // Thêm totalResults, page, pageSize, totalPages vào response
    public static JsonObjectBuilder addPaging(
            JsonObjectBuilder b,
            long totalResults,
            int page,
            int pageSize) {
        long totalPages = (totalResults + pageSize - 1) / pageSize;

        return b.add("totalResults", totalResults)
                .add("page", page)
                .add("pageSize", pageSize)
                .add("totalPages", totalPages);
    }

    // Mảng object, ví dụ danhSachTra
    public static List<JsonObject> reqObjectList(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null || v.getValueType() != ValueType.ARRAY) {
            throw bad(key);
        }
        List<JsonObject> ketQua = new ArrayList<>();
        for (JsonValue item : (JsonArray) v) {
            if (item.getValueType() != ValueType.OBJECT) {
                throw bad(key);
            }
            ketQua.add((JsonObject) item);
        }
        return ketQua;
    }

    // Số thực không bắt buộc: null nếu không gửi
    public static Double optDoubleOrNull(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null) {
            return null;
        }
        if (v.getValueType() != ValueType.NUMBER) {
            throw bad(key);
        }
        return ((JsonNumber) v).doubleValue();
    }

    // Ngày yyyy-MM-dd không bắt buộc: null nếu không gửi hoặc rỗng
    public static LocalDate optDate(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null) {
            return null;
        }
        if (v.getValueType() != ValueType.STRING) {
            throw bad(key);
        }
        String s = ((JsonString) v).getString().trim();
        if (s.isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(s);
        } catch (DateTimeParseException e) {
            throw new BadRequestException(
                    "Sai định dạng ngày (yyyy-MM-dd): " + key);
        }
    }

    // Boolean không bắt buộc: null nếu không gửi
    public static Boolean optBooleanOrNull(JsonObject d, String key) {
        JsonValue v = get(d, key);
        if (v == null) {
            return null;
        }
        if (v.getValueType() == ValueType.TRUE) {
            return true;
        }
        if (v.getValueType() == ValueType.FALSE) {
            return false;
        }
        throw bad(key);
    }

    public static boolean optBoolean(JsonObject d, String key, boolean def) {
        Boolean v = optBooleanOrNull(d, key);
        return v != null ? v : def;
    }
}