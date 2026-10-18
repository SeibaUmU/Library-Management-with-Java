package com.library.common;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Permission {

    private Permission() {}

    private static final int ADMIN  = Role.ADMIN_ID;
    private static final int THUTHU = Role.LIBRARIAN_ID;
    private static final int DOCGIA = Role.READER_ID;

    private static final Set<String> PUBLIC = new HashSet<>();
    private static final Map<String, Set<Integer>> ROLES = new HashMap<>();

    static {
        // Không cần đăng nhập
        pub(ActionType.LOGIN,
            ActionType.SEARCH_BOOKS,
            ActionType.GET_BOOK_DETAIL,
            ActionType.GET_CATEGORIES);

        // Mọi vai trò đã đăng nhập
        allow(new int[]{ADMIN, THUTHU, DOCGIA},
            ActionType.CHANGE_PASSWORD,
            ActionType.LOGOUT,
            ActionType.GET_SYSTEM_CONFIG);

        // Chỉ Thủ thư
        allow(new int[]{THUTHU},
            ActionType.CHECK_CARD_ELIGIBILITY,
            ActionType.GET_COPY_BY_BARCODE,
            ActionType.CREATE_BORROW_TICKET,
            ActionType.PROCESS_RETURN_BOOK,
            ActionType.ADD_BOOK_TITLE,
            ActionType.UPDATE_BOOK_TITLE,
            ActionType.DELETE_BOOK_TITLE,
            ActionType.ADD_BOOK_COPIES,
            ActionType.UPDATE_COPY,
            ActionType.ADD_CATALOG,
            ActionType.UPDATE_CATALOG,
            ActionType.DELETE_CATALOG,
            ActionType.GET_WAITING_RESERVATIONS,
            ActionType.ASSIGN_RESERVATION,
            ActionType.GET_FINE_SUGGESTION,
            ActionType.CREATE_FINE,
            ActionType.PAY_FINE,
            ActionType.GET_READERS,
            ActionType.ADD_READER,
            ActionType.UPDATE_READER,
            ActionType.UPDATE_CARD);

        // Chỉ Độc giả
        allow(new int[]{DOCGIA},
            ActionType.RESERVE_BOOK,
            ActionType.GET_MY_RESERVATIONS);

        // Độc giả và Thủ thư
        allow(new int[]{THUTHU, DOCGIA},
            ActionType.CANCEL_RESERVATION,
            ActionType.RENEW_BOOK,
            ActionType.GET_BORROW_HISTORY,
            ActionType.GET_FINES);

        // Chỉ Admin
        allow(new int[]{ADMIN},
            ActionType.UPDATE_SYSTEM_CONFIG,
            ActionType.GET_LIBRARIANS,
            ActionType.ADD_LIBRARIAN,
            ActionType.UPDATE_LIBRARIAN);

        // Admin và Thủ thư
        allow(new int[]{ADMIN, THUTHU},
            ActionType.GET_REPORTS);
    }

    private static void pub(String... actions) {
        for (String a : actions) {
            PUBLIC.add(a);
        }
    }

    private static void allow(int[] maVaiTroList, String... actions) {
        for (String a : actions) {
            Set<Integer> set = ROLES.computeIfAbsent(a, k -> new HashSet<>());
            for (int id : maVaiTroList) {
                set.add(id);
            }
        }
    }

    public static boolean isKnown(String action) {
        return PUBLIC.contains(action) || ROLES.containsKey(action);
    }

    public static boolean isPublic(String action) {
        return PUBLIC.contains(action);
    }

    public static boolean isAllowed(String action, int maVaiTro) {
        Set<Integer> set = ROLES.get(action);
        return set != null && set.contains(maVaiTro);
    }
}
