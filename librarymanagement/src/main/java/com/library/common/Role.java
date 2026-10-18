package com.library.common;

public final class Role {

    private Role() {}

    public static final int ADMIN_ID     = 1;
    public static final int LIBRARIAN_ID = 2;
    public static final int READER_ID    = 3;

    public static final String ADMIN     = "ADMIN";
    public static final String LIBRARIAN = "LIBRARIAN";
    public static final String READER    = "READER";

    public static String fromMaVaiTro(Integer maVaiTro) {
        if (maVaiTro == null) {
            return READER;
        }
        switch (maVaiTro) {
            case ADMIN_ID:     return ADMIN;
            case LIBRARIAN_ID: return LIBRARIAN;
            default:           return READER;
        }
    }
}
