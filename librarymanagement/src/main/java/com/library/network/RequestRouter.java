package com.library.network;

import com.library.common.ActionType;
import com.library.common.JsonUtil;
import com.library.dao.*;
import com.library.entities.*;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

import java.util.List;

public class RequestRouter {

    private final UserDAO userDAO = new UserDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final BorrowDAO borrowDAO = new BorrowDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final ConfigDAO configDAO = new ConfigDAO();
    private final ReportDAO reportDAO = new ReportDAO();


    public String route(String requestJsonStr) {
        try {
            JsonObject request = JsonUtil.parseJson(requestJsonStr);

            String action = request.getString("action");
            JsonObject data = request.isNull("data")
                    ? null
                    : request.getJsonObject("data");

            switch (action) {

                // ==================== AUTHENTICATION & USER ====================
                case ActionType.LOGIN:
                    return handleLogin(data);

                case ActionType.CHANGE_PASSWORD:
                    return handleChangePassword(data);


                // ==================== TRA CỨU & QUẢN LÝ SÁCH ====================
                case ActionType.SEARCH_BOOKS:
                    return handleSearchBooks(data);

                case ActionType.GET_BOOK_DETAIL:
                    return handleGetBookDetail(data);

                case ActionType.ADD_BOOK_TITLE:
                    return handleAddBookTitle(data);


                // ==================== MƯỢN TRẢ & GIA HẠN ====================
                case ActionType.CREATE_BORROW_TICKET:
                    return handleCreateBorrowTicket(data);

                case ActionType.PROCESS_RETURN_BOOK:
                    return handleProcessReturnBook(data);

                case ActionType.RENEW_BOOK:
                    return handleRenewBook(data);


                // ==================== CẤU HÌNH & BÁO CÁO ====================
                case ActionType.GET_SYSTEM_CONFIG:
                    return handleGetSystemConfig();

                case ActionType.GET_REPORTS:
                    return handleGetReports(data);

                default:
                    return JsonUtil.buildResponse(
                            "BAD_REQUEST",
                            "Hành động không được hỗ trợ: " + action,
                            null
                    );
            }

        } catch (Exception e) {
            e.printStackTrace();

            return JsonUtil.buildResponse(
                    "ERROR",
                    "Lỗi xử lý Server: " + e.getMessage(),
                    null
            );
        }
    }


    // --- Các hàm xử lý nghiệp vụ chi tiết ---

    private String handleLogin(JsonObject data) {
        String username = data.getString("username");
        String password = data.getString("password");

        UserAccount user = userDAO.authenticate(username, password);

        if (user == null) {
            return JsonUtil.buildResponse(
                    "UNAUTHORIZED",
                    "Tài khoản hoặc mật khẩu không chính xác!",
                    null
            );
        }

        if (user.getStatus() == UserAccount.AccountStatus.BLOCKED) {
            return JsonUtil.buildResponse(
                    "UNAUTHORIZED",
                    "Tài khoản hiện đang bị KHÓA!",
                    null
            );
        }

        JsonObject userJson = Json.createObjectBuilder()
                .add("username", user.getUsername())
                .add(
                        "fullName",
                        user.getFullName() != null
                                ? user.getFullName()
                                : ""
                )
                .add("role", user.getRole().name())
                .add("status", user.getStatus().name())
                .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Đăng nhập thành công",
                userJson
        );
    }


    private String handleChangePassword(JsonObject data) {
        String username = data.getString("username");
        String oldPassword = data.getString("oldPassword");
        String newPassword = data.getString("newPassword");

        UserAccount user = userDAO.authenticate(username, oldPassword);

        if (user == null) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Mật khẩu cũ không đúng!",
                    null
            );
        }

        user.setPassword(newPassword);

        if (userDAO.update(user)) {
            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Đổi mật khẩu thành công!",
                    null
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Lỗi cập nhật mật khẩu!",
                null
        );
    }


    private String handleSearchBooks(JsonObject data) {
        String keyword = data.containsKey("keyword")
                && !data.isNull("keyword")
                ? data.getString("keyword")
                : "";

        String category = data.containsKey("category")
                && !data.isNull("category")
                ? data.getString("category")
                : "";

        int page = data.containsKey("page")
                ? data.getInt("page")
                : 1;

        int pageSize = data.containsKey("pageSize")
                ? data.getInt("pageSize")
                : 10;

        List<BookTitle> books =
                bookDAO.searchBookTitles(
                        keyword,
                        category,
                        page,
                        pageSize
                );

        JsonArrayBuilder arrayBuilder =
                Json.createArrayBuilder();

        for (BookTitle b : books) {
            arrayBuilder.add(
                    Json.createObjectBuilder()
                            .add("isbn", b.getIsbn())
                            .add("title", b.getTitle())
                            .add(
                                    "author",
                                    b.getAuthor() != null
                                            ? b.getAuthor()
                                            : ""
                            )
                            .add(
                                    "publisher",
                                    b.getPublisher() != null
                                            ? b.getPublisher()
                                            : ""
                            )
                            .add(
                                    "category",
                                    b.getCategory() != null
                                            ? b.getCategory()
                                            : ""
                            )
                            .add(
                                    "price",
                                    b.getPrice() != null
                                            ? b.getPrice()
                                            : 0.0
                            )
                            .add("totalQuantity", b.getTotalQuantity())
                            .add(
                                    "availableQuantity",
                                    b.getAvailableQuantity()
                            )
            );
        }

        JsonObject result = Json.createObjectBuilder()
                .add("totalResults", books.size())
                .add("books", arrayBuilder)
                .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Tìm kiếm thành công",
                result
        );
    }


    private String handleGetBookDetail(JsonObject data) {
        String isbn = data.getString("isbn");

        BookTitle title = bookDAO.findByIsbn(isbn);

        if (title == null) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Không tìm thấy đầu sách với ISBN: " + isbn,
                    null
            );
        }

        JsonArrayBuilder itemsArray =
                Json.createArrayBuilder();

        if (title.getBookItems() != null) {
            for (BookItem item : title.getBookItems()) {
                itemsArray.add(
                        Json.createObjectBuilder()
                                .add(
                                        "bookItemId",
                                        item.getBookItemId()
                                )
                                .add(
                                        "status",
                                        item.getStatus().name()
                                )
                );
            }
        }

        JsonObject detailJson = Json.createObjectBuilder()
                .add("isbn", title.getIsbn())
                .add("title", title.getTitle())
                .add(
                        "author",
                        title.getAuthor() != null
                                ? title.getAuthor()
                                : ""
                )
                .add(
                        "location",
                        title.getLocation() != null
                                ? title.getLocation()
                                : ""
                )
                .add("items", itemsArray)
                .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Lấy chi tiết thành công",
                detailJson
        );
    }


    private String handleAddBookTitle(JsonObject data) {
        BookTitle title = new BookTitle();

        title.setIsbn(data.getString("isbn"));
        title.setTitle(data.getString("title"));

        title.setAuthor(
                data.containsKey("author")
                        ? data.getString("author")
                        : ""
        );

        title.setPublisher(
                data.containsKey("publisher")
                        ? data.getString("publisher")
                        : ""
        );

        title.setCategory(
                data.containsKey("category")
                        ? data.getString("category")
                        : ""
        );

        title.setPrice(
                data.containsKey("price")
                        ? data.getJsonNumber("price").doubleValue()
                        : 0.0
        );

        title.setLocation(
                data.containsKey("location")
                        ? data.getString("location")
                        : ""
        );

        if (bookDAO.saveBookTitle(title)) {
            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Thêm đầu sách thành công!",
                    null
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Không thể thêm đầu sách (trùng ISBN)!",
                null
        );
    }


    private String handleCreateBorrowTicket(JsonObject data) {
        String readerUsername = data.getString("readerUsername");
        String librarianUsername = data.getString("librarianUsername");

        List<String> bookItemIds =
                data.getJsonArray("bookItemIds")
                        .getValuesAs(
                                v -> (
                                        (jakarta.json.JsonString) v
                                ).getString()
                        );

        UserAccount reader =
                userDAO.findByUsername(readerUsername);

        UserAccount librarian =
                userDAO.findByUsername(librarianUsername);

        if (reader == null
                || reader.getStatus()
                == UserAccount.AccountStatus.BLOCKED) {

            return JsonUtil.buildResponse(
                    "ERROR",
                    "Thẻ độc giả không hợp lệ hoặc đang bị KHÓA!",
                    null
            );
        }

        SystemConfig config = configDAO.getConfig();

        long activeCount =
                borrowDAO.getActiveBorrowCount(readerUsername);

        if (activeCount + bookItemIds.size()
                > config.getMaxBooksPerReader()) {

            return JsonUtil.buildResponse(
                    "ERROR",
                    "Vượt quá số sách được mượn tối đa ("
                            + config.getMaxBooksPerReader()
                            + " cuốn)!",
                    null
            );
        }

        BorrowTicket ticket = new BorrowTicket();

        ticket.setBorrowId(
                "PM" + System.currentTimeMillis()
        );

        ticket.setReader(reader);
        ticket.setLibrarian(librarian);
        ticket.setBorrowDate(java.time.LocalDateTime.now());
        ticket.setStatus(
                BorrowTicket.BorrowStatus.BORROWING
        );

        boolean success = borrowDAO.createBorrowTicket(
                ticket,
                bookItemIds,
                config.getMaxBorrowDays()
        );

        if (success) {
            JsonObject resData = Json.createObjectBuilder()
                    .add("borrowId", ticket.getBorrowId())
                    .add("readerUsername", readerUsername)
                    .build();

            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Lập phiếu mượn thành công!",
                    resData
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Lập phiếu mượn thất bại (Sách không có sẵn)!",
                null
        );
    }


    private String handleProcessReturnBook(JsonObject data) {
        String borrowId = data.getString("borrowId");
        String bookItemId = data.getString("bookItemId");

        SystemConfig config = configDAO.getConfig();

        double fineAmount = borrowDAO.processReturnBook(
                borrowId,
                bookItemId,
                config.getFinePerDay()
        );

        if (fineAmount < 0) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Sách đã được trả trước đó hoặc mã phiếu không tồn tại!",
                    null
            );
        }

        JsonObject resData = Json.createObjectBuilder()
                .add("fineAmount", fineAmount)
                .add("isOverdue", fineAmount > 0)
                .build();

        String msg = fineAmount > 0
                ? "Trả sách thành công. Phát hiện trễ hạn, tiền phạt: "
                        + fineAmount
                        + " VNĐ"
                : "Trả sách thành công!";

        return JsonUtil.buildResponse(
                "SUCCESS",
                msg,
                resData
        );
    }


    private String handleRenewBook(JsonObject data) {
        String borrowId = data.getString("borrowId");
        String bookItemId = data.getString("bookItemId");

        SystemConfig config = configDAO.getConfig();

        boolean success = borrowDAO.renewBook(
                borrowId,
                bookItemId,
                config.getMaxBorrowDays(),
                config.getMaxRenewTimes()
        );

        if (success) {
            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Gia hạn sách thành công!",
                    null
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Không thể gia hạn (Đã vượt quá số lần gia hạn cho phép)!",
                null
        );
    }


    private String handleGetSystemConfig() {
        SystemConfig config = configDAO.getConfig();

        JsonObject configJson = Json.createObjectBuilder()
                .add(
                        "maxBorrowDays",
                        config.getMaxBorrowDays()
                )
                .add(
                        "maxBooksPerReader",
                        config.getMaxBooksPerReader()
                )
                .add(
                        "finePerDay",
                        config.getFinePerDay()
                )
                .add(
                        "maxRenewTimes",
                        config.getMaxRenewTimes()
                )
                .add(
                        "holdKeepDays",
                        config.getHoldKeepDays()
                )
                .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Tải cấu hình thành công",
                configJson
        );
    }


    private String handleGetReports(JsonObject data) {
        int limit = data.containsKey("limit")
                ? data.getInt("limit")
                : 5;

        List<Object[]> topBooks =
                reportDAO.getTopBorrowedBooks(limit);

        JsonArrayBuilder arrayBuilder =
                Json.createArrayBuilder();

        for (Object[] row : topBooks) {
            arrayBuilder.add(
                    Json.createObjectBuilder()
                            .add(
                                    "title",
                                    row[0].toString()
                            )
                            .add(
                                    "borrowCount",
                                    (Long) row[1]
                            )
            );
        }

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Xuất báo cáo thành công",
                Json.createObjectBuilder()
                        .add("topBooks", arrayBuilder)
                        .build()
        );
    }
}
