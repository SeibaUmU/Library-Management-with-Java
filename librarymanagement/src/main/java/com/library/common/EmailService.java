package com.library.common;

public final class EmailService {

    private EmailService() {
    }

    public static void gui(String den, String tieuDe, String noiDung) {
        if (den == null || den.trim().isEmpty()) {
            System.out.println("📧 [Email] Bỏ qua: độc giả chưa có địa chỉ Email");
            return;
        }

        System.out.println(
                "📧 [Email] Tới " + den + " | " + tieuDe + " | " + noiDung);
    }
}