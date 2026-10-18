package com.library.common;


public class ActionType {

    // Tài khoản (UC-05, UC-06)
    public static final String LOGIN = "LOGIN";
    public static final String CHANGE_PASSWORD = "CHANGE_PASSWORD";
    public static final String LOGOUT = "LOGOUT";

    // Tra cứu sách (UC-03)
    public static final String SEARCH_BOOKS = "SEARCH_BOOKS";
    public static final String GET_BOOK_DETAIL = "GET_BOOK_DETAIL";
    public static final String GET_CATEGORIES = "GET_CATEGORIES";

    // Kho sách & danh mục (UC-12)
    public static final String ADD_BOOK_TITLE = "ADD_BOOK_TITLE";
    public static final String UPDATE_BOOK_TITLE = "UPDATE_BOOK_TITLE";
    public static final String DELETE_BOOK_TITLE = "DELETE_BOOK_TITLE";
    public static final String ADD_BOOK_COPIES = "ADD_BOOK_COPIES";
    public static final String UPDATE_COPY = "UPDATE_COPY";
    public static final String ADD_CATALOG = "ADD_CATALOG";
    public static final String UPDATE_CATALOG = "UPDATE_CATALOG";
    public static final String DELETE_CATALOG = "DELETE_CATALOG";

    // Mượn - trả - gia hạn - lịch sử (UC-13, 01, 02, 09, 08)
    public static final String CHECK_CARD_ELIGIBILITY = "CHECK_CARD_ELIGIBILITY";
    public static final String GET_COPY_BY_BARCODE = "GET_COPY_BY_BARCODE";
    public static final String CREATE_BORROW_TICKET = "CREATE_BORROW_TICKET";
    public static final String PROCESS_RETURN_BOOK = "PROCESS_RETURN_BOOK";
    public static final String RENEW_BOOK = "RENEW_BOOK";
    public static final String GET_BORROW_HISTORY = "GET_BORROW_HISTORY";

    // Đặt trước (UC-07, UC-10)
    public static final String RESERVE_BOOK = "RESERVE_BOOK";
    public static final String CANCEL_RESERVATION = "CANCEL_RESERVATION";
    public static final String GET_MY_RESERVATIONS = "GET_MY_RESERVATIONS";
    public static final String GET_WAITING_RESERVATIONS = "GET_WAITING_RESERVATIONS";
    public static final String ASSIGN_RESERVATION = "ASSIGN_RESERVATION";

    // Phiếu phạt (UC-14)
    public static final String GET_FINE_SUGGESTION = "GET_FINE_SUGGESTION";
    public static final String CREATE_FINE = "CREATE_FINE";
    public static final String GET_FINES = "GET_FINES";
    public static final String PAY_FINE = "PAY_FINE";

    // Độc giả & thủ thư (UC-11, UC-15)
    public static final String GET_READERS = "GET_READERS";
    public static final String ADD_READER = "ADD_READER";
    public static final String UPDATE_READER = "UPDATE_READER";
    public static final String UPDATE_CARD = "UPDATE_CARD";
    public static final String GET_LIBRARIANS = "GET_LIBRARIANS";
    public static final String ADD_LIBRARIAN = "ADD_LIBRARIAN";
    public static final String UPDATE_LIBRARIAN = "UPDATE_LIBRARIAN";

    // Cấu hình & báo cáo (UC-04, UC-16)
    public static final String GET_SYSTEM_CONFIG = "GET_SYSTEM_CONFIG";
    public static final String UPDATE_SYSTEM_CONFIG = "UPDATE_SYSTEM_CONFIG";
    public static final String GET_REPORTS = "GET_REPORTS";
}
