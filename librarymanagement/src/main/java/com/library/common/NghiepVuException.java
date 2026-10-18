package com.library.common;

// Lỗi nghiệp vụ: router chuyển thành response với status tương ứng
public class NghiepVuException extends RuntimeException {

    private final String status;

    public NghiepVuException(String message) {
        this("ERROR", message);
    }

    public NghiepVuException(String status, String message) {
        super(message);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}