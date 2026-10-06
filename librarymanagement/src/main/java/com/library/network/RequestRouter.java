package com.library.network;

import java.util.ArrayList;
import java.util.List;

import com.library.common.ActionType;
import com.library.common.JsonUtil;
import com.library.entities.CauHinh;
import com.library.entities.CuonSach;
import com.library.entities.DauSach;
import com.library.entities.NguoiDung;
import com.library.entities.PhieuMuon;
import com.library.entities.TheDocGia;
import com.library.repository.CauHinhRepository;
import com.library.repository.DatTruocRepository;
import com.library.repository.DauSachRepository;
import com.library.repository.NguoiDungRepository;
import com.library.repository.PhieuMuonRepository;
import com.library.repository.PhieuPhatRepository;
import com.library.repository.ThongKeRepository;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;

public class RequestRouter {

    private final NguoiDungRepository nguoiDungRepo =
            new NguoiDungRepository();

    private final DauSachRepository dauSachRepo =
            new DauSachRepository();

    private final PhieuMuonRepository phieuMuonRepo =
            new PhieuMuonRepository();

    private final PhieuPhatRepository phieuPhatRepo =
            new PhieuPhatRepository();

    private final DatTruocRepository datTruocRepo =
            new DatTruocRepository();

    private final CauHinhRepository cauHinhRepo =
            new CauHinhRepository();

    private final ThongKeRepository thongKeRepo =
            new ThongKeRepository();


    public String route(String requestJsonStr) {
        try {
            JsonObject request =
                    JsonUtil.parseJson(requestJsonStr);

            String action =
                    request.getString("action");

            JsonObject data =
                    request.containsKey("data")
                            && !request.isNull("data")
                            ? request.getJsonObject("data")
                            : null;

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


    // --- Các hàm xử lý nghiệp vụ ---

    private String handleLogin(JsonObject data) {

        if (data == null) {
            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Dữ liệu yêu cầu trống!",
                    null
            );
        }

        String account =
                data.getString("username"); // Nhận Email hoặc Số điện thoại

        String password =
                data.getString("password");

        NguoiDung nd =
                nguoiDungRepo.authenticate(
                        account,
                        password
                );

        if (nd == null) {
            return JsonUtil.buildResponse(
                    "UNAUTHORIZED",
                    "Tài khoản hoặc mật khẩu không chính xác!",
                    null
            );
        }

        if (Boolean.FALSE.equals(nd.getTrangThai())) {
            return JsonUtil.buildResponse(
                    "UNAUTHORIZED",
                    "Tài khoản hiện đang bị KHÓA!",
                    null
            );
        }

        // Tìm thông tin thẻ độc giả nếu có
        TheDocGia the =
                nguoiDungRepo.findTheDocGiaByMaND(
                        nd.getMaND()
                );

        JsonObject userJson =
                Json.createObjectBuilder()
                        .add("maND", nd.getMaND())
                        .add(
                                "fullName",
                                nd.getHoTen() != null
                                        ? nd.getHoTen()
                                        : ""
                        )
                        .add(
                                "email",
                                nd.getEmail() != null
                                        ? nd.getEmail()
                                        : ""
                        )
                        .add(
                                "phone",
                                nd.getSoDienThoai() != null
                                        ? nd.getSoDienThoai()
                                        : ""
                        )
                        .add(
                                "role",
                                nd.getVaiTro() != null
                                        ? nd.getVaiTro().getTenVaiTro()
                                        : "READER"
                        )
                        .add(
                                "maThe",
                                the != null
                                        ? the.getMaThe()
                                        : -1
                        )
                        .add(
                                "trangThaiThe",
                                the != null
                                        ? (
                                                the.getTrangThai() != null
                                                        ? the.getTrangThai()
                                                        : "ACTIVE"
                                        )
                                        : "NONE"
                        )
                        .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Đăng nhập thành công",
                userJson
        );
    }


    private String handleChangePassword(JsonObject data) {

        if (data == null) {
            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Dữ liệu yêu cầu trống!",
                    null
            );
        }

        String account =
                data.getString("username");

        String oldPassword =
                data.getString("oldPassword");

        String newPassword =
                data.getString("newPassword");

        NguoiDung nd =
                nguoiDungRepo.authenticate(
                        account,
                        oldPassword
                );

        if (nd == null) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Mật khẩu cũ không chính xác!",
                    null
            );
        }

        nd.setMatKhau(newPassword);

        if (nguoiDungRepo.updateNguoiDung(nd)) {
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

        String keyword =
                (
                        data != null
                                && data.containsKey("keyword")
                                && !data.isNull("keyword")
                )
                        ? data.getString("keyword")
                        : "";

        Integer maTL =
                (
                        data != null
                                && data.containsKey("maTL")
                                && !data.isNull("maTL")
                )
                        ? data.getInt("maTL")
                        : null;

        int page =
                (
                        data != null
                                && data.containsKey("page")
                )
                        ? data.getInt("page")
                        : 1;

        int pageSize =
                (
                        data != null
                                && data.containsKey("pageSize")
                )
                        ? data.getInt("pageSize")
                        : 10;

        List<DauSach> danhSach =
                dauSachRepo.search(
                        keyword,
                        maTL,
                        page,
                        pageSize
                );

        JsonArrayBuilder arrayBuilder =
                Json.createArrayBuilder();

        for (DauSach d : danhSach) {

            arrayBuilder.add(
                    Json.createObjectBuilder()
                            .add(
                                    "maDauSach",
                                    d.getMaDauSach()
                            )
                            .add(
                                    "tenSach",
                                    d.getTenSach() != null
                                            ? d.getTenSach()
                                            : ""
                            )
                            .add(
                                    "giaTien",
                                    d.getGiaTien() != null
                                            ? d.getGiaTien()
                                            : 0.0
                            )
                            .add(
                                    "tacGia",
                                    d.getTacGia() != null
                                            ? d.getTacGia().getTenTacGia()
                                            : ""
                            )
                            .add(
                                    "theLoai",
                                    d.getTheLoai() != null
                                            ? d.getTheLoai().getTenTL()
                                            : ""
                            )
                            .add(
                                    "nhaXuatBan",
                                    d.getNhaXuatBan() != null
                                            ? d.getNhaXuatBan().getTenNXB()
                                            : ""
                            )
            );
        }

        JsonObject result =
                Json.createObjectBuilder()
                        .add(
                                "totalResults",
                                danhSach.size()
                        )
                        .add(
                                "books",
                                arrayBuilder
                        )
                        .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Tìm kiếm sách thành công",
                result
        );
    }


    private String handleGetBookDetail(JsonObject data) {

        if (data == null
                || !data.containsKey("maDauSach")) {

            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Thiếu tham số maDauSach!",
                    null
            );
        }

        Integer maDauSach =
                data.getInt("maDauSach");

        DauSach ds =
                dauSachRepo.findById(maDauSach);

        if (ds == null) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Không tìm thấy đầu sách mã: "
                            + maDauSach,
                    null
            );
        }

        List<CuonSach> danhSachCuon =
                dauSachRepo.getCuonSachByDauSach(
                        maDauSach
                );

        JsonArrayBuilder itemsArray =
                Json.createArrayBuilder();

        for (CuonSach cs : danhSachCuon) {

            itemsArray.add(
                    Json.createObjectBuilder()
                            .add(
                                    "maCuonSach",
                                    cs.getMaCuonSach()
                            )
                            .add(
                                    "tinhTrangValLy",
                                    cs.getTinhTrangValLy() != null
                                            ? cs.getTinhTrangValLy()
                                            : "Bình thường"
                            )
                            .add(
                                    "trangThai",
                                    cs.getTrangThai() != null
                                            ? cs.getTrangThai()
                                            : "AVAILABLE"
                            )
            );
        }

        JsonObject detailJson =
                Json.createObjectBuilder()
                        .add(
                                "maDauSach",
                                ds.getMaDauSach()
                        )
                        .add(
                                "tenSach",
                                ds.getTenSach()
                        )
                        .add(
                                "giaTien",
                                ds.getGiaTien() != null
                                        ? ds.getGiaTien()
                                        : 0.0
                        )
                        .add(
                                "tacGia",
                                ds.getTacGia() != null
                                        ? ds.getTacGia().getTenTacGia()
                                        : ""
                        )
                        .add(
                                "theLoai",
                                ds.getTheLoai() != null
                                        ? ds.getTheLoai().getTenTL()
                                        : ""
                        )
                        .add(
                                "cuonSachList",
                                itemsArray
                        )
                        .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Lấy chi tiết đầu sách thành công",
                detailJson
        );
    }


    private String handleAddBookTitle(JsonObject data) {

        if (data == null) {
            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Dữ liệu yêu cầu trống!",
                    null
            );
        }

        DauSach ds = new DauSach();

        ds.setTenSach(
                data.getString("tenSach")
        );

        ds.setGiaTien(
                data.containsKey("giaTien")
                        ? data.getJsonNumber("giaTien")
                                .doubleValue()
                        : 0.0
        );

        if (dauSachRepo.saveDauSach(ds)) {
            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Thêm đầu sách mới thành công!",
                    null
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Không thể thêm đầu sách!",
                null
        );
    }


    private String handleCreateBorrowTicket(JsonObject data) {

        if (data == null) {
            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Dữ liệu yêu cầu trống!",
                    null
            );
        }

        Integer maThe =
                data.getInt("maThe");

        Integer maThuThu =
                data.getInt("maThuThu");

        List<String> danhSachMaCuonSach =
                new ArrayList<>();

        if (data.containsKey("danhSachMaCuonSach")) {

            data.getJsonArray("danhSachMaCuonSach")
                    .forEach(
                            val ->
                                    danhSachMaCuonSach.add(
                                            (
                                                    (jakarta.json.JsonString) val
                                            ).getString()
                                    )
                    );
        }

        CauHinh cauHinh =
                cauHinhRepo.getCauHinh();

        long currentBorrowing =
                phieuMuonRepo.countActiveBorrowingByMaThe(
                        maThe
                );

        if (currentBorrowing
                + danhSachMaCuonSach.size()
                > cauHinh.getSoSachToiDa()) {

            return JsonUtil.buildResponse(
                    "ERROR",
                    "Vượt quá số lượng sách mượn tối đa ("
                            + cauHinh.getSoSachToiDa()
                            + " cuốn)! Hiện đang mượn: "
                            + currentBorrowing,
                    null
            );
        }

        NguoiDung thuThu =
                nguoiDungRepo.findById(maThuThu);

        TheDocGia the = new TheDocGia();

        the.setMaThe(maThe);

        PhieuMuon pm = new PhieuMuon();

        pm.setTheDocGia(the);
        pm.setThuThu(thuThu);

        boolean success =
                phieuMuonRepo.taoPhieuMuonSafe(
                        pm,
                        danhSachMaCuonSach,
                        cauHinh.getSoNgayMuonToiDa()
                );

        if (success) {

            JsonObject resData =
                    Json.createObjectBuilder()
                            .add(
                                    "maPhieuMuon",
                                    pm.getMaPhieuMuon()
                            )
                            .add(
                                    "maThe",
                                    maThe
                            )
                            .build();

            return JsonUtil.buildResponse(
                    "SUCCESS",
                    "Lập phiếu mượn thành công!",
                    resData
            );
        }

        return JsonUtil.buildResponse(
                "ERROR",
                "Không thể lập phiếu mượn (Sách đã bị mượn hoặc không khả dụng)!",
                null
        );
    }


    private String handleProcessReturnBook(JsonObject data) {

        if (data == null) {
            return JsonUtil.buildResponse(
                    "BAD_REQUEST",
                    "Dữ liệu yêu cầu trống!",
                    null
            );
        }

        Integer maCTPM =
                data.getInt("maCTPM");

        Integer maThuThu =
                data.getInt("maThuThu");

        String tinhTrangKhiTra =
                data.containsKey("tinhTrangKhiTra")
                        ? data.getString("tinhTrangKhiTra")
                        : "Bình thường";

        CauHinh cauHinh =
                cauHinhRepo.getCauHinh();

        double tienPhat =
                phieuMuonRepo.traSach(
                        maCTPM,
                        maThuThu,
                        tinhTrangKhiTra,
                        cauHinh.getTienPhatNgay()
                );

        if (tienPhat < 0) {
            return JsonUtil.buildResponse(
                    "ERROR",
                    "Chi tiết phiếu mượn không tồn tại hoặc đã được trả trước đó!",
                    null
            );
        }

        JsonObject resData =
                Json.createObjectBuilder()
                        .add(
                                "tienPhat",
                                tienPhat
                        )
                        .add(
                                "coPhat",
                                tienPhat > 0
                        )
                        .build();

        String msg =
                tienPhat > 0
                        ? "Trả sách thành công. Phát hiện trễ hạn, tiền phạt: "
                                + tienPhat
                                + " VNĐ"
                        : "Trả sách thành công!";

        return JsonUtil.buildResponse(
                "SUCCESS",
                msg,
                resData
        );
    }


    private String handleGetSystemConfig() {

        CauHinh ch =
                cauHinhRepo.getCauHinh();

        JsonObject configJson =
                Json.createObjectBuilder()
                        .add(
                                "soSachToiDa",
                                ch.getSoSachToiDa()
                        )
                        .add(
                                "soNgayMuonToiDa",
                                ch.getSoNgayMuonToiDa()
                        )
                        .add(
                                "tienPhatNgay",
                                ch.getTienPhatNgay()
                        )
                        .build();

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Tải cấu hình thành công",
                configJson
        );
    }


    private String handleGetReports(JsonObject data) {

        int limit =
                (
                        data != null
                                && data.containsKey("limit")
                )
                        ? data.getInt("limit")
                        : 5;

        List<Object[]> topSach =
                thongKeRepo.getTopSachMuonNhieuNhat(
                        limit
                );

        JsonArrayBuilder arrayBuilder =
                Json.createArrayBuilder();

        for (Object[] row : topSach) {

            arrayBuilder.add(
                    Json.createObjectBuilder()
                            .add(
                                    "tenSach",
                                    row != null
                                            ? row.toString()
                                            : ""
                            )
                            .add(
                                    "soLuotMuon",
                                    (Long) row[1]
                            )
            );
        }

        return JsonUtil.buildResponse(
                "SUCCESS",
                "Xuất báo cáo thống kê thành công",
                Json.createObjectBuilder()
                        .add(
                                "topSach",
                                arrayBuilder
                        )
                        .build()
        );
    }
}