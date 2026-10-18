package com.library.common;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {

    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    public static boolean verify(String plain, String hash) {
        if (plain == null || hash == null) {
            return false;
        }
        try {
            return BCrypt.checkpw(plain, hash);
        } catch (IllegalArgumentException e) {
            return false; // hash không hợp lệ (ví dụ dòng chưa được băm)
        }
    }
}
