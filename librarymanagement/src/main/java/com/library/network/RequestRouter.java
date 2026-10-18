package com.library.network;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.library.common.ActionType;
import com.library.common.BadRequestException;
import static com.library.common.JsonParams.addPaging;
import static com.library.common.JsonParams.optBoolean;
import static com.library.common.JsonParams.optBooleanOrNull;
import static com.library.common.JsonParams.optDate;
import static com.library.common.JsonParams.optDouble;
import static com.library.common.JsonParams.optDoubleOrNull;
import static com.library.common.JsonParams.optInt;
import static com.library.common.JsonParams.optInteger;
import static com.library.common.JsonParams.optString;
import static com.library.common.JsonParams.pageOf;
import static com.library.common.JsonParams.pageSizeOf;
import static com.library.common.JsonParams.reqDouble;
import static com.library.common.JsonParams.reqInt;
import static com.library.common.JsonParams.reqObjectList;
import static com.library.common.JsonParams.reqString;
import static com.library.common.JsonParams.reqStringList;
import com.library.common.JsonUtil;
import com.library.common.NghiepVuException;
import com.library.common.PasswordUtil;
import com.library.common.Permission;
import com.library.common.Role;
import com.library.common.TrangThai;
import com.library.entities.CauHinh;
import com.library.entities.ChiTietPhieuMuon;
import com.library.entities.CuonSach;
import com.library.entities.DatTruoc;
import com.library.entities.DauSach;
import com.library.entities.NguoiDung;
import com.library.entities.NhaXuatBan;
import com.library.entities.PhieuMuon;
import com.library.entities.PhieuPhat;
import com.library.entities.TacGia;
import com.library.entities.TheDocGia;
import com.library.entities.TheLoai;
import com.library.repository.CauHinhRepository;
import com.library.repository.DanhMucRepository;
import com.library.repository.DatTruocRepository;
import com.library.repository.DauSachRepository;
import com.library.repository.NguoiDungRepository;
import com.library.repository.PhieuMuonRepository;
import com.library.repository.PhieuPhatRepository;
import com.library.repository.TheDocGiaRepository;
import com.library.repository.ThongKeRepository;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonException;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import jakarta.json.JsonValue.ValueType;

public class RequestRouter {
        private final TheDocGiaRepository theDocGiaRepo = new TheDocGiaRepository();
        private final DanhMucRepository danhMucRepo = new DanhMucRepository();
        private final NguoiDungRepository nguoiDungRepo = new NguoiDungRepository();

        private final DauSachRepository dauSachRepo = new DauSachRepository();

        private final PhieuMuonRepository phieuMuonRepo = new PhieuMuonRepository();

        private final PhieuPhatRepository phieuPhatRepo = new PhieuPhatRepository();

        private final DatTruocRepository datTruocRepo = new DatTruocRepository();

        private final CauHinhRepository cauHinhRepo = new CauHinhRepository();

        private final ThongKeRepository thongKeRepo = new ThongKeRepository();
        // Phiên đăng nhập của kết nối này (mỗi ClientHandler có 1 RequestRouter riêng)
        private NguoiDung currentUser; // null = chưa đăng nhập
        private Integer currentMaThe; // thẻ của độc giả đang đăng nhập, null nếu không có
        private final ThongBaoService thongBaoService = new ThongBaoService();

        public String route(String requestJsonStr) {
                try {
                        JsonObject request = JsonUtil.parseJson(requestJsonStr);

                        String action = reqString(request, "action");
                        JsonObject data = dataOf(request);

                        String loiQuyen = kiemTraQuyen(action);
                        if (loiQuyen != null) {
                                return loiQuyen;
                        }

                        switch (action) {

                                // ==================== TÀI KHOẢN ====================

                                case ActionType.LOGIN:
                                        return handleLogin(data);

                                case ActionType.LOGOUT:
                                        return handleLogout();

                                case ActionType.CHANGE_PASSWORD:
                                        return handleChangePassword(data);

                                // ==================== TRA CỨU & QUẢN LÝ SÁCH ====================

                                case ActionType.SEARCH_BOOKS:
                                        return handleSearchBooks(data);

                                case ActionType.GET_BOOK_DETAIL:
                                        return handleGetBookDetail(data);

                                case ActionType.ADD_BOOK_TITLE:
                                        return handleAddBookTitle(data);

                                // ==================== MƯỢN TRẢ ====================

                                case ActionType.CREATE_BORROW_TICKET:
                                        return handleCreateBorrowTicket(data);

                                case ActionType.PROCESS_RETURN_BOOK:
                                        return handleProcessReturnBook(data);

                                // ==================== CẤU HÌNH & BÁO CÁO ====================

                                case ActionType.GET_SYSTEM_CONFIG:
                                        return handleGetSystemConfig();

                                case ActionType.GET_REPORTS:
                                        return handleGetReports(data);
                                case ActionType.GET_CATEGORIES:
                                        return handleGetCategories();

                                case ActionType.UPDATE_SYSTEM_CONFIG:
                                        return handleUpdateSystemConfig(data);

                                case ActionType.CHECK_CARD_ELIGIBILITY:
                                        return handleCheckCardEligibility(data);
                                case ActionType.GET_COPY_BY_BARCODE:
                                        return handleGetCopyByBarcode(data);

                                case ActionType.GET_BORROW_HISTORY:
                                        return handleGetBorrowHistory(data);

                                case ActionType.RENEW_BOOK:
                                        return handleRenewBook(data);
                                case ActionType.RESERVE_BOOK:
                                        return handleReserveBook(data);

                                case ActionType.CANCEL_RESERVATION:
                                        return handleCancelReservation(data);

                                case ActionType.GET_MY_RESERVATIONS:
                                        return handleGetMyReservations(data);

                                case ActionType.GET_WAITING_RESERVATIONS:
                                        return handleGetWaitingReservations(data);

                                case ActionType.ASSIGN_RESERVATION:
                                        return handleAssignReservation(data);

                                case ActionType.GET_FINE_SUGGESTION:
                                        return handleGetFineSuggestion(data);

                                case ActionType.CREATE_FINE:
                                        return handleCreateFine(data);

                                case ActionType.GET_FINES:
                                        return handleGetFines(data);

                                case ActionType.PAY_FINE:
                                        return handlePayFine(data);
                                case ActionType.UPDATE_BOOK_TITLE:
                                        return handleUpdateBookTitle(data);

                                case ActionType.DELETE_BOOK_TITLE:
                                        return handleDeleteBookTitle(data);

                                case ActionType.ADD_BOOK_COPIES:
                                        return handleAddBookCopies(data);

                                case ActionType.UPDATE_COPY:
                                        return handleUpdateCopy(data);

                                case ActionType.ADD_CATALOG:
                                        return handleAddCatalog(data);

                                case ActionType.UPDATE_CATALOG:
                                        return handleUpdateCatalog(data);

                                case ActionType.DELETE_CATALOG:
                                        return handleDeleteCatalog(data);

                                case ActionType.GET_READERS:
                                        return handleGetReaders(data);

                                case ActionType.ADD_READER:
                                        return handleAddReader(data);

                                case ActionType.UPDATE_READER:
                                        return handleUpdateReader(data);

                                case ActionType.UPDATE_CARD:
                                        return handleUpdateCard(data);

                                case ActionType.GET_LIBRARIANS:
                                        return handleGetLibrarians(data);

                                case ActionType.ADD_LIBRARIAN:
                                        return handleAddLibrarian(data);

                                case ActionType.UPDATE_LIBRARIAN:
                                        return handleUpdateLibrarian(data);
                                default:
                                        // Action đã khai báo nhưng chưa được cài đặt
                                        return JsonUtil.buildResponse(
                                                        "BAD_REQUEST",
                                                        "Hành động chưa được hỗ trợ: " + action,
                                                        null);
                        }

                } catch (BadRequestException e) {
                        return JsonUtil.buildResponse("BAD_REQUEST", e.getMessage(), null);
                } catch (NghiepVuException e) {
                        return JsonUtil.buildResponse(e.getStatus(), e.getMessage(), null);
                } catch (JsonException e) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Dữ liệu gửi lên không phải JSON hợp lệ!",
                                        null);

                } catch (Exception e) {
                        e.printStackTrace();

                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Lỗi xử lý Server: " + e.getMessage(),
                                        null);
                }
        }

        // Lấy "data" từ request: null nếu thiếu / JSON null, lỗi nếu không phải object
        private JsonObject dataOf(JsonObject request) {
                if (!request.containsKey("data") || request.isNull("data")) {
                        return null;
                }
                JsonValue v = request.get("data");
                if (v.getValueType() != ValueType.OBJECT) {
                        throw new BadRequestException("Thiếu hoặc sai tham số: data");
                }
                return (JsonObject) v;
        }

        private int currentRoleId() {
                if (currentUser == null || currentUser.getVaiTro() == null) {
                        return Role.READER_ID;
                }
                return currentUser.getVaiTro().getMaVaiTro();
        }

        // Trả về chuỗi response lỗi, hoặc null nếu được phép
        private String kiemTraQuyen(String action) {

                if (!Permission.isKnown(action)) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Hành động không được hỗ trợ: " + action,
                                        null);
                }

                if (Permission.isPublic(action)) {
                        return null;
                }

                if (currentUser == null) {
                        return JsonUtil.buildResponse(
                                        "UNAUTHORIZED",
                                        "Vui lòng đăng nhập!",
                                        null);
                }

                if (!Permission.isAllowed(action, currentRoleId())) {
                        return JsonUtil.buildResponse(
                                        "FORBIDDEN",
                                        "Bạn không có quyền thực hiện chức năng này!",
                                        null);
                }

                return null;
        }

        private static String str(String s) {
                return s != null ? s : "";
        }

        private static int num(Integer i) {
                return i != null ? i : 0;
        }

        private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

        private static final DateTimeFormatter DMY_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        private static void addDateTime(JsonObjectBuilder b, String key, LocalDateTime d) {
                if (d != null) {
                        b.add(key, d.format(DT_FMT));
                } else {
                        b.addNull(key);
                }
        }

        private static void addDate(JsonObjectBuilder b, String key, LocalDate d) {
                if (d != null) {
                        b.add(key, d.toString()); // yyyy-MM-dd
                } else {
                        b.addNull(key);
                }
        }

        // --- Các hàm xử lý nghiệp vụ ---
        private String handleGetCopyByBarcode(JsonObject data) {

                String maCuonSach = reqString(data, "maCuonSach").trim();
                int maThe = reqInt(data, "maThe");

                String loi = phieuMuonRepo.kiemTraChoMuon(maCuonSach, maThe);

                CuonSach cs = loi == null
                                ? dauSachRepo.findCuonSachFetch(maCuonSach)
                                : null;

                if (loi != null || cs == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        loi != null ? loi : PhieuMuonRepository.LOI_KHONG_SAN_SANG,
                                        null);
                }

                CauHinh ch = cauHinhRepo.getCauHinh();
                DauSach ds = cs.getDauSach();

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maCuonSach", cs.getMaCuonSach())
                                .add("maDauSach", ds.getMaDauSach())
                                .add("tenSach", str(ds.getTenSach()))
                                .add("tacGia", ds.getTacGia() != null ? str(ds.getTacGia().getTenTacGia()) : "")
                                .add("tinhTrangVatLy", str(cs.getTinhTrangVatLy()));

                addDate(
                                result,
                                "ngayHenTraDuKien",
                                LocalDate.now().plusDays(ch.getSoNgayMuonToiDa()));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cuốn sách sẵn sàng để mượn",
                                result.build());
        }

        private String handleLogin(JsonObject data) {

                if (data == null) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Dữ liệu yêu cầu trống!",
                                        null);
                }

                String account = reqString(data, "username"); // Email hoặc Số điện thoại
                String password = reqString(data, "password");

                NguoiDung nd = nguoiDungRepo.authenticate(account, password);

                if (nd == null) {
                        return JsonUtil.buildResponse(
                                        "UNAUTHORIZED",
                                        "Tên đăng nhập hoặc mật khẩu không chính xác!",
                                        null);
                }

                if (Boolean.FALSE.equals(nd.getTrangThai())) {
                        return JsonUtil.buildResponse(
                                        "UNAUTHORIZED",
                                        "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin",
                                        null);
                }

                TheDocGia the = nguoiDungRepo.findTheDocGiaByMaND(nd.getMaND());

                // Lưu phiên đăng nhập của kết nối này
                currentUser = nd;
                currentMaThe = the != null ? the.getMaThe() : null;

                Integer maVaiTro = nd.getVaiTro() != null
                                ? nd.getVaiTro().getMaVaiTro()
                                : null;

                JsonObjectBuilder user = Json.createObjectBuilder()
                                .add("maND", nd.getMaND())
                                .add("fullName", str(nd.getHoTen()))
                                .add("email", str(nd.getEmail()))
                                .add("phone", str(nd.getSoDienThoai()))
                                .add("maVaiTro", maVaiTro != null ? maVaiTro : Role.READER_ID)
                                .add("role", Role.fromMaVaiTro(maVaiTro))
                                .add("maThe", the != null ? the.getMaThe() : -1)
                                .add("trangThaiThe", TheDocGiaRepository.trangThaiHienThi(the));

                addDate(user, "ngayHetHanThe", the != null ? the.getNgayHetHan() : null);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Đăng nhập thành công",
                                user.build());
        }

        private String handleLogout() {
                currentUser = null;
                currentMaThe = null;

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Đăng xuất thành công",
                                null);
        }

        private String handleChangePassword(JsonObject data) {

                String oldPassword = reqString(data, "oldPassword");
                String newPassword = reqString(data, "newPassword");
                String confirmPassword = reqString(data, "confirmPassword");

                if (newPassword.length() < 6) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Mật khẩu mới không hợp lệ (tối thiểu 6 ký tự)!",
                                        null);
                }

                if (!newPassword.equals(confirmPassword)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Mật khẩu xác nhận không khớp!",
                                        null);
                }

                NguoiDung nd = nguoiDungRepo.findById(currentUser.getMaND());

                if (nd == null
                                || !PasswordUtil.verify(oldPassword, nd.getMatKhau())) {

                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Mật khẩu cũ không chính xác!",
                                        null);
                }

                nd.setMatKhau(PasswordUtil.hash(newPassword));

                if (nguoiDungRepo.updateNguoiDung(nd)) {
                        return JsonUtil.buildResponse(
                                        "SUCCESS",
                                        "Đổi mật khẩu thành công!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "ERROR",
                                "Lỗi cập nhật mật khẩu!",
                                null);
        }

        private String handleSearchBooks(JsonObject data) {

                String keyword = optString(data, "keyword", "");
                Integer maTL = optInteger(data, "maTL");
                int page = pageOf(data);
                int pageSize = pageSizeOf(data);

                List<DauSach> danhSach = dauSachRepo.search(keyword, maTL, page, pageSize);

                long total = dauSachRepo.countSearch(keyword, maTL);

                JsonArrayBuilder books = Json.createArrayBuilder();

                for (DauSach d : danhSach) {
                        books.add(
                                        Json.createObjectBuilder()
                                                        .add("maDauSach", d.getMaDauSach())
                                                        .add("tenSach", str(d.getTenSach()))
                                                        .add("giaTien", d.getGiaTien() != null ? d.getGiaTien() : 0.0)
                                                        .add("tacGia",
                                                                        d.getTacGia() != null
                                                                                        ? str(d.getTacGia()
                                                                                                        .getTenTacGia())
                                                                                        : "")
                                                        .add("theLoai",
                                                                        d.getTheLoai() != null
                                                                                        ? str(d.getTheLoai().getTenTL())
                                                                                        : "")
                                                        .add("nhaXuatBan",
                                                                        d.getNhaXuatBan() != null
                                                                                        ? str(d.getNhaXuatBan()
                                                                                                        .getTenNXB())
                                                                                        : "")
                                                        .add("viTri", str(d.getViTri()))
                                                        .add("tongSoLuong", num(d.getTongSoLuong()))
                                                        .add("soLuongKhaDung", num(d.getSoLuongKhaDung())));
                }

                JsonObjectBuilder result = Json.createObjectBuilder();
                addPaging(result, total, page, pageSize);
                result.add("books", books);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                total == 0
                                                ? "Không tìm thấy sách phù hợp với từ khóa đã nhập"
                                                : "Tìm kiếm sách thành công",
                                result.build());
        }

        private String handleGetBookDetail(JsonObject data) {

                int maDauSach = reqInt(data, "maDauSach");

                DauSach ds = dauSachRepo.findById(maDauSach);

                if (ds == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy đầu sách mã: " + maDauSach,
                                        null);
                }

                JsonArrayBuilder cuonSachList = Json.createArrayBuilder();

                for (CuonSach cs : dauSachRepo.getCuonSachByDauSach(maDauSach)) {
                        cuonSachList.add(
                                        Json.createObjectBuilder()
                                                        .add("maCuonSach", cs.getMaCuonSach())
                                                        .add("tinhTrangVatLy",
                                                                        cs.getTinhTrangVatLy() != null
                                                                                        ? cs.getTinhTrangVatLy()
                                                                                        : "Bình thường")
                                                        .add("trangThai",
                                                                        cs.getTrangThai() != null
                                                                                        ? cs.getTrangThai()
                                                                                        : TrangThai.CUON_AVAILABLE));
                }

                int khaDung = num(ds.getSoLuongKhaDung());

                JsonObjectBuilder detail = Json.createObjectBuilder()
                                .add("maDauSach", ds.getMaDauSach())
                                .add("tenSach", str(ds.getTenSach()))
                                .add("giaTien", ds.getGiaTien() != null ? ds.getGiaTien() : 0.0)
                                .add("tacGia",
                                                ds.getTacGia() != null ? str(ds.getTacGia().getTenTacGia()) : "")
                                .add("theLoai",
                                                ds.getTheLoai() != null ? str(ds.getTheLoai().getTenTL()) : "")
                                .add("nhaXuatBan",
                                                ds.getNhaXuatBan() != null ? str(ds.getNhaXuatBan().getTenNXB()) : "");

                if (ds.getNhaXuatBan() != null && ds.getNhaXuatBan().getNamXuatBan() != null) {
                        detail.add("namXuatBan", ds.getNhaXuatBan().getNamXuatBan());
                } else {
                        detail.addNull("namXuatBan");
                }

                detail.add("viTri", str(ds.getViTri()))
                                .add("tongSoLuong", num(ds.getTongSoLuong()))
                                .add("soLuongKhaDung", khaDung)
                                .add("choPhepDatTruoc", khaDung == 0)
                                .add("cuonSachList", cuonSachList);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                khaDung == 0
                                                ? "Sách hiện tại đã được mượn hết"
                                                : "Lấy chi tiết đầu sách thành công",
                                detail.build());
        }

        private String handleGetCategories() {

                JsonArrayBuilder tacGiaList = Json.createArrayBuilder();
                for (TacGia t : danhMucRepo.getAllTacGia()) {
                        tacGiaList.add(
                                        Json.createObjectBuilder()
                                                        .add("maTG", t.getMaTG())
                                                        .add("tenTacGia", str(t.getTenTacGia())));
                }

                JsonArrayBuilder nxbList = Json.createArrayBuilder();
                for (NhaXuatBan n : danhMucRepo.getAllNhaXuatBan()) {
                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maNXB", n.getMaNXB())
                                        .add("tenNXB", str(n.getTenNXB()));

                        if (n.getNamXuatBan() != null) {
                                item.add("namXuatBan", n.getNamXuatBan());
                        } else {
                                item.addNull("namXuatBan");
                        }

                        nxbList.add(item);
                }

                JsonArrayBuilder theLoaiList = Json.createArrayBuilder();
                for (TheLoai t : danhMucRepo.getAllTheLoai()) {
                        theLoaiList.add(
                                        Json.createObjectBuilder()
                                                        .add("maTL", t.getMaTL())
                                                        .add("tenTL", str(t.getTenTL())));
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh mục thành công",
                                Json.createObjectBuilder()
                                                .add("tacGiaList", tacGiaList)
                                                .add("nhaXuatBanList", nxbList)
                                                .add("theLoaiList", theLoaiList)
                                                .build());
        }

        private String handleAddBookTitle(JsonObject data) {

                String tenSach = reqString(data, "tenSach").trim();
                double giaTien = optDouble(data, "giaTien", 0.0);
                int maTG = reqInt(data, "maTG"); // bắt buộc (DB: NOT NULL)
                int maNXB = reqInt(data, "maNXB"); // bắt buộc (DB: NOT NULL)
                int maTL = reqInt(data, "maTL"); // bắt buộc

                if (giaTien < 0) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Giá tiền phải lớn hơn hoặc bằng 0",
                                        null);
                }

                DauSach ds = dauSachRepo.themDauSach(
                                tenSach,
                                giaTien,
                                maTG,
                                maNXB,
                                maTL,
                                optString(data, "viTri", null));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Thêm đầu sách mới thành công!",
                                Json.createObjectBuilder()
                                                .add("maDauSach", ds.getMaDauSach())
                                                .build());
        }

        private String handleUpdateBookTitle(JsonObject data) {

                int maDauSach = reqInt(data, "maDauSach");
                String tenSach = optString(data, "tenSach", null);
                Double giaTien = optDoubleOrNull(data, "giaTien");

                if (tenSach != null && tenSach.trim().isEmpty()) {
                        throw new BadRequestException("Thiếu hoặc sai tham số: tenSach");
                }

                if (giaTien != null && giaTien < 0) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Giá tiền phải lớn hơn hoặc bằng 0",
                                        null);
                }

                dauSachRepo.capNhatDauSach(
                                maDauSach,
                                tenSach != null ? tenSach.trim() : null,
                                giaTien,
                                optInteger(data, "maTG"),
                                optInteger(data, "maNXB"),
                                optInteger(data, "maTL"),
                                optString(data, "viTri", null));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật đầu sách thành công!",
                                null);
        }

        private String handleDeleteBookTitle(JsonObject data) {

                dauSachRepo.xoaDauSach(reqInt(data, "maDauSach"));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Xóa đầu sách thành công!",
                                null);
        }

        private String handleAddBookCopies(JsonObject data) {

                int maDauSach = reqInt(data, "maDauSach");
                List<String> maCuonSachList = reqStringList(data, "maCuonSachList");
                String tinhTrangVatLy = optString(data, "tinhTrangVatLy", "Mới");

                if (maCuonSachList.isEmpty()) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Danh sách mã cuốn sách trống!",
                                        null);
                }

                DauSachRepository.KetQuaThemCuon kq = dauSachRepo.themCuonSach(maDauSach, maCuonSachList,
                                tinhTrangVatLy);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Nhập cuốn sách vào kho thành công!",
                                Json.createObjectBuilder()
                                                .add("maDauSach", maDauSach)
                                                .add("soCuonDaThem", kq.soCuonDaThem)
                                                .add("tongSoLuong", kq.tongSoLuong)
                                                .add("soLuongKhaDung", kq.soLuongKhaDung)
                                                .build());
        }

        private String handleUpdateCopy(JsonObject data) {

                String maCuonSach = reqString(data, "maCuonSach").trim();
                String tinhTrangVatLy = optString(data, "tinhTrangVatLy", null);
                String trangThai = optString(data, "trangThai", null);

                if (tinhTrangVatLy != null && tinhTrangVatLy.trim().isEmpty()) {
                        tinhTrangVatLy = null;
                }
                if (trangThai != null && trangThai.trim().isEmpty()) {
                        trangThai = null;
                }

                if (tinhTrangVatLy == null && trangThai == null) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Thiếu tham số: tinhTrangVatLy | trangThai",
                                        null);
                }

                DauSachRepository.KetQuaCuon kq = dauSachRepo.capNhatCuonSach(maCuonSach, tinhTrangVatLy, trangThai);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật cuốn sách thành công!",
                                Json.createObjectBuilder()
                                                .add("maCuonSach", kq.maCuonSach)
                                                .add("tinhTrangVatLy", str(kq.tinhTrangVatLy))
                                                .add("trangThai", kq.trangThai)
                                                .add("tongSoLuong", kq.tongSoLuong)
                                                .add("soLuongKhaDung", kq.soLuongKhaDung)
                                                .build());
        }

        // ----- Danh mục Tác giả / NXB / Thể loại -----

        private String handleAddCatalog(JsonObject data) {

                String loai = reqString(data, "loai");
                String ten = reqString(data, "ten").trim();
                Integer namXuatBan = optInteger(data, "namXuatBan");

                Integer id = danhMucRepo.them(loai, ten, namXuatBan);

                if (id == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không thể thêm danh mục!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Thao tác danh mục thành công!",
                                Json.createObjectBuilder()
                                                .add("loai", loai)
                                                .add("id", id)
                                                .build());
        }

        private String handleUpdateCatalog(JsonObject data) {

                String loai = reqString(data, "loai");
                int id = reqInt(data, "id");

                if (!danhMucRepo.sua(
                                loai,
                                id,
                                optString(data, "ten", null),
                                optInteger(data, "namXuatBan"))) {

                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy danh mục mã: " + id,
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Thao tác danh mục thành công!",
                                Json.createObjectBuilder()
                                                .add("loai", loai)
                                                .add("id", id)
                                                .build());
        }

        private String handleDeleteCatalog(JsonObject data) {

                String loai = reqString(data, "loai");
                int id = reqInt(data, "id");

                switch (danhMucRepo.xoa(loai, id)) {

                        case DanhMucRepository.XOA_OK:
                                return JsonUtil.buildResponse(
                                                "SUCCESS",
                                                "Thao tác danh mục thành công!",
                                                Json.createObjectBuilder()
                                                                .add("loai", loai)
                                                                .add("id", id)
                                                                .build());

                        case DanhMucRepository.XOA_KHONG_TIM_THAY:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Không tìm thấy danh mục mã: " + id,
                                                null);

                        case DanhMucRepository.XOA_DANG_DUNG:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Không thể xóa danh mục đang được đầu sách sử dụng",
                                                null);

                        default:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Lỗi xóa danh mục!",
                                                null);
                }
        }

        private String handleCreateBorrowTicket(JsonObject data) {

                int maThe = reqInt(data, "maThe");
                List<String> danhSachMaCuonSach = reqStringList(data, "danhSachMaCuonSach");

                if (danhSachMaCuonSach.isEmpty()) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Danh sách cuốn sách trống!",
                                        null);
                }

                // UC-13: kiểm tra điều kiện thẻ
                TheDocGiaRepository.KetQuaKiemTra kt = theDocGiaRepo.kiemTraDieuKien(maThe);

                if (kt == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy thẻ độc giả mã: " + maThe,
                                        null);
                }

                if (!kt.dat) {
                        return JsonUtil.buildResponse("ERROR", kt.message, null);
                }

                if (kt.dangMuon + danhSachMaCuonSach.size() > kt.soSachToiDa) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Vượt quá số lượng sách mượn tối đa ("
                                                        + kt.soSachToiDa
                                                        + " cuốn)! Hiện đang mượn: "
                                                        + kt.dangMuon,
                                        null);
                }

                CauHinh ch = cauHinhRepo.getCauHinh();

                PhieuMuonRepository.KetQuaPhieuMuon kq = phieuMuonRepo.taoPhieuMuon(
                                maThe,
                                currentUser.getMaND(), // thủ thư lấy từ phiên
                                danhSachMaCuonSach,
                                ch.getSoNgayMuonToiDa());

                JsonArrayBuilder chiTiet = Json.createArrayBuilder();

                for (PhieuMuonRepository.DongMuon d : kq.chiTiet) {
                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maCTPM", d.maCTPM)
                                        .add("maCuonSach", d.maCuonSach)
                                        .add("tenSach", str(d.tenSach));

                        addDate(item, "ngayHenTra", d.ngayHenTra);
                        chiTiet.add(item);
                }

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maPhieuMuon", kq.maPhieuMuon)
                                .add("maThe", maThe);

                addDateTime(result, "ngayMuon", kq.ngayMuon);
                result.add("chiTiet", chiTiet);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Lập phiếu mượn thành công!",
                                result.build());
        }

        private String handleProcessReturnBook(JsonObject data) {

                List<JsonObject> items = reqObjectList(data, "danhSachTra");

                if (items.isEmpty()) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Danh sách trả sách trống!",
                                        null);
                }

                List<PhieuMuonRepository.YeuCauTra> danhSach = new ArrayList<>();

                for (JsonObject it : items) {
                        PhieuMuonRepository.YeuCauTra yc = new PhieuMuonRepository.YeuCauTra();

                        yc.maCTPM = reqInt(it, "maCTPM");
                        yc.tinhTrangKhiTra = optString(it, "tinhTrangKhiTra", "Bình thường");
                        yc.ghiChu = optString(it, "ghiChu", null);
                        yc.soTienPhat = optDoubleOrNull(it, "soTienPhat");

                        String vp = optString(it, "viPham", null);

                        if (vp != null && !vp.trim().isEmpty()) {
                                if (!TrangThai.VI_PHAM_DAMAGED.equals(vp)
                                                && !TrangThai.VI_PHAM_LOST.equals(vp)) {

                                        return JsonUtil.buildResponse(
                                                        "BAD_REQUEST",
                                                        "Giá trị viPham không hợp lệ!",
                                                        null);
                                }
                                yc.viPham = vp;
                        }

                        if (yc.soTienPhat != null && yc.soTienPhat < 0) {
                                return JsonUtil.buildResponse(
                                                "BAD_REQUEST",
                                                "Số tiền phạt phải lớn hơn hoặc bằng 0",
                                                null);
                        }

                        danhSach.add(yc);
                }

                CauHinh ch = cauHinhRepo.getCauHinh();

                List<PhieuMuonRepository.KetQuaTra> ketQuaTra = phieuMuonRepo.traSach(
                                currentUser.getMaND(), // thủ thư lấy từ phiên
                                danhSach,
                                ch.getTienPhatNgay());
                // UC-10: cuốn giữ chỗ vừa về quầy -> báo cho người đặt trước
                for (PhieuMuonRepository.KetQuaTra k : ketQuaTra) {
                        if (k.datTruocDuocThongBao != null) {
                                thongBaoService.guiSachDatTruocDaVe(k.datTruocDuocThongBao);
                        }
                }

                double tongPhat = 0;
                JsonArrayBuilder ketQua = Json.createArrayBuilder();

                for (PhieuMuonRepository.KetQuaTra k : ketQuaTra) {

                        JsonArrayBuilder phieuPhat = Json.createArrayBuilder();

                        for (PhieuMuonRepository.PhieuPhatTao p : k.phieuPhat) {
                                tongPhat += p.soTienPhat;

                                phieuPhat.add(
                                                Json.createObjectBuilder()
                                                                .add("maPhieuPhat", p.maPhieuPhat)
                                                                .add("lyDo", str(p.lyDo))
                                                                .add("soTienPhat", p.soTienPhat));
                        }

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maCTPM", k.maCTPM)
                                        .add("maCuonSach", k.maCuonSach)
                                        .add("tenSach", str(k.tenSach))
                                        .add("soNgayTre", k.soNgayTre)
                                        .add("trangThaiCuonSach", k.trangThaiCuonSach)
                                        .add("phieuPhat", phieuPhat);

                        if (k.datTruocDuocThongBao != null) {
                                item.add("datTruocDuocThongBao", k.datTruocDuocThongBao);
                        } else {
                                item.addNull("datTruocDuocThongBao");
                        }

                        ketQua.add(item);
                }

                JsonObject result = Json.createObjectBuilder()
                                .add("tongTienPhat", tongPhat)
                                .add("coPhat", tongPhat > 0)
                                .add("ketQua", ketQua)
                                .build();

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                tongPhat > 0
                                                ? "Trả sách thành công. Tổng tiền phạt: " + tongPhat + " VNĐ"
                                                : "Trả sách thành công!",
                                result);
        }

        private LocalDate hanTraCua(ChiTietPhieuMuon ct) {
                return ct.getNgayHenTra() != null
                                ? ct.getNgayHenTra()
                                : ct.getPhieuMuon().getNgayHenTra();
        }

        private String handleGetBorrowHistory(JsonObject data) {

                String tab = optString(data, "tab", "BORROWING");

                if (!"BORROWING".equals(tab) && !"RETURNED".equals(tab)) {
                        throw new BadRequestException("Thiếu hoặc sai tham số: tab");
                }

                boolean dangMuon = "BORROWING".equals(tab);
                boolean laDocGia = currentRoleId() == Role.READER_ID;

                PhieuMuonRepository.LichSuFilter f = new PhieuMuonRepository.LichSuFilter();

                f.dangMuon = dangMuon;
                f.keyword = optString(data, "keyword", null);
                f.tuNgay = optDate(data, "tuNgay");
                f.denNgay = optDate(data, "denNgay");

                if (laDocGia) {
                        // Độc giả luôn chỉ xem được thẻ của chính mình
                        if (currentMaThe == null) {
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Tài khoản chưa có thẻ độc giả",
                                                null);
                        }
                        f.maThe = currentMaThe;

                } else {
                        f.maThe = optInteger(data, "maThe");
                        f.maPhieuMuon = optInteger(data, "maPhieuMuon");
                        f.maCuonSach = optString(data, "maCuonSach", null);

                        if (f.maThe == null
                                        && f.maPhieuMuon == null
                                        && (f.maCuonSach == null || f.maCuonSach.trim().isEmpty())) {

                                return JsonUtil.buildResponse(
                                                "BAD_REQUEST",
                                                "Thiếu tham số: maThe | maPhieuMuon | maCuonSach",
                                                null);
                        }
                }

                int page = pageOf(data);
                int pageSize = pageSizeOf(data);

                List<ChiTietPhieuMuon> danhSach = phieuMuonRepo.searchLichSu(f, page, pageSize);

                long total = phieuMuonRepo.countLichSu(f);

                CauHinh ch = cauHinhRepo.getCauHinh();
                LocalDate homNay = LocalDate.now();

                // Dữ liệu phụ lấy theo lô (tránh truy vấn lặp từng dòng)
                Set<Integer> dauSachCoNguoiCho = Collections.emptySet();
                Map<Integer, PhieuPhatRepository.TongPhat> tongPhat = Collections.emptyMap();

                List<Integer> ids = new ArrayList<>();

                for (ChiTietPhieuMuon ct : danhSach) {
                        ids.add(dangMuon
                                        ? ct.getCuonSach().getDauSach().getMaDauSach()
                                        : ct.getMaCTPM());
                }

                if (dangMuon) {
                        dauSachCoNguoiCho = datTruocRepo.getDauSachCoNguoiCho(ids);
                } else {
                        tongPhat = phieuPhatRepo.getTongPhatTheoCTPM(ids);
                }

                JsonArrayBuilder items = Json.createArrayBuilder();

                for (ChiTietPhieuMuon ct : danhSach) {

                        PhieuMuon pm = ct.getPhieuMuon();
                        CuonSach cs = ct.getCuonSach();
                        DauSach ds = cs.getDauSach();
                        NguoiDung docGia = pm.getTheDocGia().getNguoiDung();

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maCTPM", ct.getMaCTPM())
                                        .add("maThe", pm.getTheDocGia().getMaThe())
                                        .add("tenDocGia", docGia != null ? str(docGia.getHoTen()) : "")
                                        .add("maCuonSach", cs.getMaCuonSach())
                                        .add("tenSach", str(ds.getTenSach()));

                        addDateTime(item, "ngayMuon", pm.getNgayMuon());

                        if (dangMuon) {
                                LocalDate han = hanTraCua(ct);

                                long soNgayTre = (han != null && homNay.isAfter(han))
                                                ? ChronoUnit.DAYS.between(han, homNay)
                                                : 0;

                                int daGiaHan = num(ct.getSoLanGiaHan());

                                boolean coTheGiaHan = soNgayTre == 0
                                                && daGiaHan < ch.getSoLanGiaHanToiDa()
                                                && !dauSachCoNguoiCho.contains(ds.getMaDauSach())
                                                && !TrangThai.CUON_RESERVED.equals(cs.getTrangThai());

                                item.add("maPhieuMuon", pm.getMaPhieuMuon());
                                addDate(item, "ngayHenTra", han);
                                item.add("soLanGiaHan", daGiaHan)
                                                .add("quaHan", soNgayTre > 0)
                                                .add("soNgayTre", soNgayTre)
                                                .add("tienPhatDuKien", soNgayTre * ch.getTienPhatNgay())
                                                .add("coTheGiaHan", coTheGiaHan);

                        } else {
                                addDateTime(item, "ngayTraThucTe", ct.getNgayTraThucTe());
                                item.add("trangThaiSachKhiTra", str(ct.getTrangThaiSachKhiTra()));

                                PhieuPhatRepository.TongPhat tp = tongPhat.get(ct.getMaCTPM());

                                item.add("tienPhat", tp != null ? tp.tongTien : 0.0);

                                if (tp != null) {
                                        item.add("daThanhToanPhat", tp.daNopHet);
                                } else {
                                        item.addNull("daThanhToanPhat");
                                }
                        }

                        items.add(item);
                }

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("tab", tab);

                addPaging(result, total, page, pageSize);
                result.add("items", items);

                String message = total > 0
                                ? "Tải lịch sử mượn trả thành công"
                                : (laDocGia
                                                ? "Bạn chưa có giao dịch mượn sách nào"
                                                : "Không có giao dịch phù hợp");

                return JsonUtil.buildResponse("SUCCESS", message, result.build());
        }

        private String handleRenewBook(JsonObject data) {

                int maCTPM = reqInt(data, "maCTPM");

                // Độc giả chỉ được gia hạn cuốn của mình; thủ thư thì không bị giới hạn
                Integer maTheChu = null;

                if (currentRoleId() == Role.READER_ID) {
                        if (currentMaThe == null) {
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Tài khoản chưa có thẻ độc giả",
                                                null);
                        }
                        maTheChu = currentMaThe;
                }

                CauHinh ch = cauHinhRepo.getCauHinh();

                PhieuMuonRepository.KetQuaGiaHan r = phieuMuonRepo.giaHan(
                                maCTPM,
                                maTheChu,
                                ch.getSoNgayMuonToiDa(),
                                ch.getSoLanGiaHanToiDa());

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maCTPM", r.maCTPM)
                                .add("maCuonSach", r.maCuonSach)
                                .add("tenSach", str(r.tenSach));

                addDate(result, "ngayHenTraCu", r.ngayHenTraCu);
                addDate(result, "ngayHenTraMoi", r.ngayHenTraMoi);

                result.add("soLanGiaHan", r.soLanGiaHan)
                                .add("soLanGiaHanToiDa", ch.getSoLanGiaHanToiDa());

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Gia hạn thành công. Ngày hẹn trả mới là: "
                                                + r.ngayHenTraMoi.format(DMY_FMT),
                                result.build());
        }

        private JsonObject configToJson(CauHinh ch) {
                return Json.createObjectBuilder()
                                .add("soSachToiDa", ch.getSoSachToiDa())
                                .add("soNgayMuonToiDa", ch.getSoNgayMuonToiDa())
                                .add("tienPhatNgay", ch.getTienPhatNgay())
                                .add("soLanGiaHanToiDa", ch.getSoLanGiaHanToiDa())
                                .add("soNgayGiuSachDatTruoc", ch.getSoNgayGiuSachDatTruoc())
                                .build();
        }

        private String handleGetSystemConfig() {

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải cấu hình thành công",
                                configToJson(cauHinhRepo.getCauHinh()));
        }

        private static final String MSG_CAU_HINH = "Tham số cấu hình phải là số lớn hơn 0";

        private static int cauHinhInt(JsonObject data, String key) {
                int v;
                try {
                        v = reqInt(data, key);
                } catch (BadRequestException e) {
                        throw new BadRequestException(MSG_CAU_HINH);
                }
                if (v <= 0) {
                        throw new BadRequestException(MSG_CAU_HINH);
                }
                return v;
        }

        private static double cauHinhDouble(JsonObject data, String key) {
                double v;
                try {
                        v = reqDouble(data, key);
                } catch (BadRequestException e) {
                        throw new BadRequestException(MSG_CAU_HINH);
                }
                if (v <= 0) {
                        throw new BadRequestException(MSG_CAU_HINH);
                }
                return v;
        }

        private String handleUpdateSystemConfig(JsonObject data) {

                int soSachToiDa = cauHinhInt(data, "soSachToiDa");
                int soNgayMuonToiDa = cauHinhInt(data, "soNgayMuonToiDa");
                double tienPhatNgay = cauHinhDouble(data, "tienPhatNgay");
                int soLanGiaHanToiDa = cauHinhInt(data, "soLanGiaHanToiDa");
                int soNgayGiuSachDatTruoc = cauHinhInt(data, "soNgayGiuSachDatTruoc");

                CauHinh ch = cauHinhRepo.getCauHinh();

                ch.setSoSachToiDa(soSachToiDa);
                ch.setSoNgayMuonToiDa(soNgayMuonToiDa);
                ch.setTienPhatNgay(tienPhatNgay);
                ch.setSoLanGiaHanToiDa(soLanGiaHanToiDa);
                ch.setSoNgayGiuSachDatTruoc(soNgayGiuSachDatTruoc);

                if (!cauHinhRepo.saveCauHinh(ch)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Lỗi cập nhật cấu hình!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật quy định thành công",
                                configToJson(ch));
        }

        private String handleGetReports(JsonObject data) {

                String loai = optString(data, "loaiBaoCao", "TOP_BOOKS");
                LocalDate tuNgay = optDate(data, "tuNgay");
                LocalDate denNgay = optDate(data, "denNgay");
                String groupBy = optString(data, "groupBy", "MONTH");
                int limit = Math.min(100, Math.max(1, optInt(data, "limit", 5)));

                if (tuNgay != null && denNgay != null && tuNgay.isAfter(denNgay)) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Khoảng thời gian không hợp lệ",
                                        null);
                }

                JsonObjectBuilder result = Json.createObjectBuilder().add("loaiBaoCao", loai);

                boolean coDuLieu;

                switch (loai) {

                        case "TOP_BOOKS": {
                                List<Object[]> rows = thongKeRepo.getTopSach(tuNgay, denNgay, limit);

                                JsonArrayBuilder arr = Json.createArrayBuilder();

                                for (Object[] r : rows) {
                                        arr.add(
                                                        Json.createObjectBuilder()
                                                                        .add("maDauSach", ((Number) r[0]).intValue())
                                                                        .add("tenSach", r[1] != null ? (String) r[1]
                                                                                        : "")
                                                                        .add("soLuotMuon",
                                                                                        ((Number) r[2]).longValue()));
                                }

                                result.add("topSach", arr);
                                coDuLieu = !rows.isEmpty();
                                break;
                        }

                        case "BORROW_STATS": {
                                List<Object[]> rows = thongKeRepo.thongKeMuonTra(tuNgay, denNgay, groupBy);

                                JsonArrayBuilder arr = Json.createArrayBuilder();

                                for (Object[] r : rows) {
                                        arr.add(
                                                        Json.createObjectBuilder()
                                                                        .add("ky", (String) r[0])
                                                                        .add("soLuotMuon", ((Number) r[1]).longValue())
                                                                        .add("soLuotTra", ((Number) r[2]).longValue()));
                                }

                                result.add("groupBy", groupBy).add("rows", arr);
                                coDuLieu = !rows.isEmpty();
                                break;
                        }

                        case "OVERDUE_READERS": {
                                List<Object[]> rows = thongKeRepo.getDocGiaTreHanHoacNoPhat();

                                JsonArrayBuilder arr = Json.createArrayBuilder();

                                for (Object[] r : rows) {
                                        arr.add(
                                                        Json.createObjectBuilder()
                                                                        .add("maThe", ((Number) r[0]).intValue())
                                                                        .add("hoTen", r[1] != null ? (String) r[1] : "")
                                                                        .add("email", r[2] != null ? (String) r[2] : "")
                                                                        .add("soDienThoai",
                                                                                        r[3] != null ? (String) r[3]
                                                                                                        : "")
                                                                        .add("soSachQuaHan",
                                                                                        ((Number) r[4]).longValue())
                                                                        .add("tienPhatChuaNop",
                                                                                        ((Number) r[5]).doubleValue()));
                                }

                                result.add("danhSach", arr);
                                coDuLieu = !rows.isEmpty();
                                break;
                        }

                        case "FINE_REVENUE": {
                                Object[] tong = thongKeRepo.tongHopDoanhThuPhat(tuNgay, denNgay);

                                long tongSoPhieu = (Long) tong[0];
                                double tongTien = (Double) tong[1];
                                double daThu = (Double) tong[2];

                                List<Object[]> rows = thongKeRepo.doanhThuPhatTheoKy(tuNgay, denNgay, groupBy);

                                JsonArrayBuilder arr = Json.createArrayBuilder();

                                for (Object[] r : rows) {
                                        arr.add(
                                                        Json.createObjectBuilder()
                                                                        .add("ky", (String) r[0])
                                                                        .add("soPhieu", ((Number) r[1]).longValue())
                                                                        .add("tongTienPhat",
                                                                                        ((Number) r[2]).doubleValue()));
                                }

                                result.add("tongSoPhieu", tongSoPhieu)
                                                .add("tongTienPhat", tongTien)
                                                .add("daThu", daThu)
                                                .add("chuaThu", tongTien - daThu)
                                                .add("theoKy", arr);

                                coDuLieu = tongSoPhieu > 0;
                                break;
                        }

                        default:
                                return JsonUtil.buildResponse(
                                                "BAD_REQUEST",
                                                "Loại báo cáo không được hỗ trợ",
                                                null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                coDuLieu
                                                ? "Xuất báo cáo thống kê thành công"
                                                : "Không có dữ liệu thống kê phù hợp với khoảng thời gian này",
                                result.build());
        }

        private String handleCheckCardEligibility(JsonObject data) {

                int maThe = reqInt(data, "maThe");

                TheDocGiaRepository.KetQuaKiemTra kq = theDocGiaRepo.kiemTraDieuKien(maThe);

                if (kq == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy thẻ độc giả mã: " + maThe,
                                        null);
                }

                TheDocGia the = kq.the;
                NguoiDung nd = the.getNguoiDung();

                JsonObjectBuilder docGia = Json.createObjectBuilder()
                                .add("maND", nd != null ? nd.getMaND() : -1)
                                .add("hoTen", nd != null ? str(nd.getHoTen()) : "")
                                .add("email", nd != null ? str(nd.getEmail()) : "")
                                .add("soDienThoai", nd != null ? str(nd.getSoDienThoai()) : "")
                                .add("maThe", the.getMaThe());

                addDate(docGia, "ngayCap", the.getNgayCap());
                addDate(docGia, "ngayHetHan", the.getNgayHetHan());
                docGia.add("trangThaiThe", TheDocGiaRepository.trangThaiHienThi(the));

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("eligible", kq.dat);

                if (kq.reasonCode != null) {
                        result.add("reasonCode", kq.reasonCode);
                } else {
                        result.addNull("reasonCode");
                }

                result.add("docGia", docGia)
                                .add("dangMuon", kq.dangMuon)
                                .add("soSachToiDa", kq.soSachToiDa)
                                .add("conDuocMuon", Math.max(0, kq.soSachToiDa - kq.dangMuon));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                kq.message,
                                result.build());
        }

        private static final List<String> DAT_TRUOC_TT = List.of(
                        TrangThai.DATTRUOC_WAITING,
                        TrangThai.DATTRUOC_RESERVED,
                        TrangThai.DATTRUOC_NOTIFIED,
                        TrangThai.DATTRUOC_COMPLETED,
                        TrangThai.DATTRUOC_CANCELLED);

        private static String optTrangThaiDatTruoc(JsonObject data, String def) {
                String tt = optString(data, "trangThai", def);

                if (tt != null && !DAT_TRUOC_TT.contains(tt)) {
                        throw new BadRequestException("Thiếu hoặc sai tham số: trangThai");
                }

                return tt;
        }

        // ---------- RESERVE_BOOK (UC-07) ----------
        private String handleReserveBook(JsonObject data) {

                int maDauSach = reqInt(data, "maDauSach");

                final String khongDuDieuKien = "Thẻ của bạn không đủ điều kiện thực hiện đặt trước";

                if (currentMaThe == null) {
                        return JsonUtil.buildResponse("ERROR", khongDuDieuKien, null);
                }

                DauSach ds = dauSachRepo.findById(maDauSach);

                if (ds == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy đầu sách mã: " + maDauSach,
                                        null);
                }

                TheDocGia the = theDocGiaRepo.findById(currentMaThe);

                if (the == null
                                || !TrangThai.THE_ACTIVE.equals(
                                                TheDocGiaRepository.trangThaiHienThi(the))) {

                        return JsonUtil.buildResponse("ERROR", khongDuDieuKien, null);
                }

                if (datTruocRepo.existsHieuLuc(currentMaThe, maDauSach)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Bạn đã đặt trước đầu sách này rồi và đang trong hàng chờ",
                                        null);
                }

                if (num(ds.getSoLuongKhaDung()) > 0) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Đầu sách vẫn còn cuốn sẵn sàng, không cần đặt trước",
                                        null);
                }

                DatTruoc dt = datTruocRepo.taoLuotDat(currentMaThe, maDauSach);

                long viTri = datTruocRepo.getViTriHangCho(
                                dt.getMaDatTruoc(), maDauSach, dt.getNgayDat());

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maDatTruoc", dt.getMaDatTruoc())
                                .add("maDauSach", maDauSach)
                                .add("tenSach", str(ds.getTenSach()));

                addDateTime(result, "ngayDat", dt.getNgayDat());

                result.add("trangThai", dt.getTrangThai())
                                .add("viTriHangCho", viTri);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Đặt trước sách thành công. Hệ thống sẽ thông báo khi sách sẵn sàng",
                                result.build());
        }

        // ---------- CANCEL_RESERVATION ----------
        private String handleCancelReservation(JsonObject data) {

                int maDatTruoc = reqInt(data, "maDatTruoc");

                Integer maTheChu = null;

                if (currentRoleId() == Role.READER_ID) {
                        if (currentMaThe == null) {
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Tài khoản chưa có thẻ độc giả",
                                                null);
                        }
                        maTheChu = currentMaThe;
                }

                Integer keTiep = datTruocRepo.huyLuotDat(maDatTruoc, maTheChu);

                if (keTiep != null) {
                        thongBaoService.guiSachDatTruocDaVe(keTiep);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Hủy đặt trước thành công",
                                Json.createObjectBuilder()
                                                .add("maDatTruoc", maDatTruoc)
                                                .add("trangThai", TrangThai.DATTRUOC_CANCELLED)
                                                .build());
        }

        // ---------- GET_MY_RESERVATIONS (UC-07, đồng thời là kênh thông báo UC-10)
        // ----------
        private String handleGetMyReservations(JsonObject data) {

                if (currentMaThe == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Tài khoản chưa có thẻ độc giả",
                                        null);
                }

                String trangThai = optTrangThaiDatTruoc(data, null);
                CauHinh ch = cauHinhRepo.getCauHinh();

                JsonArrayBuilder danhSach = Json.createArrayBuilder();

                for (DatTruoc d : datTruocRepo.getByThe(currentMaThe, trangThai)) {

                        DauSach dau = d.getDauSach();
                        boolean choXep = TrangThai.DATTRUOC_WAITING.equals(d.getTrangThai());
                        boolean daBao = TrangThai.DATTRUOC_NOTIFIED.equals(d.getTrangThai());

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maDatTruoc", d.getMaDatTruoc())
                                        .add("maDauSach", dau.getMaDauSach())
                                        .add("tenSach", str(dau.getTenSach()));

                        addDateTime(item, "ngayDat", d.getNgayDat());
                        item.add("trangThai", d.getTrangThai());

                        if (choXep) {
                                item.add("viTriHangCho",
                                                datTruocRepo.getViTriHangCho(
                                                                d.getMaDatTruoc(), dau.getMaDauSach(), d.getNgayDat()));
                        } else {
                                item.addNull("viTriHangCho");
                        }

                        if (daBao && d.getCuonSach() != null) {
                                item.add("maCuonSach", d.getCuonSach().getMaCuonSach());
                        } else {
                                item.addNull("maCuonSach");
                        }

                        addDateTime(item, "ngayThongBao", daBao ? d.getNgayThongBao() : null);

                        if (daBao && d.getNgayThongBao() != null) {
                                addDate(
                                                item,
                                                "hanNhanSach",
                                                d.getNgayThongBao().toLocalDate()
                                                                .plusDays(ch.getSoNgayGiuSachDatTruoc()));
                                item.add(
                                                "thongBao",
                                                ThongBaoService.noiDungSachVe(
                                                                str(dau.getTenSach()),
                                                                ch.getSoNgayGiuSachDatTruoc()));
                        } else {
                                item.addNull("hanNhanSach");
                                item.addNull("thongBao");
                        }

                        danhSach.add(item);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh sách đặt trước thành công",
                                Json.createObjectBuilder().add("danhSach", danhSach).build());
        }

        // ---------- GET_WAITING_RESERVATIONS (thủ thư) ----------
        private String handleGetWaitingReservations(JsonObject data) {

                String trangThai = optTrangThaiDatTruoc(data, TrangThai.DATTRUOC_WAITING);
                Integer maDauSach = optInteger(data, "maDauSach");

                List<DatTruoc> ds = datTruocRepo.getByDauSachVaTrangThai(maDauSach, trangThai);

                Map<Integer, DatTruocRepository.CuonGoiY> goiY = new HashMap<>();

                if (TrangThai.DATTRUOC_WAITING.equals(trangThai)) {
                        Set<Integer> maDauSachList = new HashSet<>();
                        for (DatTruoc d : ds) {
                                maDauSachList.add(d.getDauSach().getMaDauSach());
                        }
                        goiY = datTruocRepo.getCuonGoiY(maDauSachList);
                }

                JsonArrayBuilder danhSach = Json.createArrayBuilder();

                for (DatTruoc d : ds) {

                        NguoiDung nd = d.getTheDocGia().getNguoiDung();

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maDatTruoc", d.getMaDatTruoc())
                                        .add("maThe", d.getTheDocGia().getMaThe())
                                        .add("tenDocGia", nd != null ? str(nd.getHoTen()) : "")
                                        .add("maDauSach", d.getDauSach().getMaDauSach())
                                        .add("tenSach", str(d.getDauSach().getTenSach()));

                        addDateTime(item, "ngayDat", d.getNgayDat());
                        item.add("trangThai", d.getTrangThai());

                        DatTruocRepository.CuonGoiY g = goiY.get(d.getDauSach().getMaDauSach());

                        if (g != null) {
                                JsonObjectBuilder goiYJson = Json.createObjectBuilder()
                                                .add("maCuonSach", g.maCuonSach);
                                addDate(goiYJson, "ngayHenTra", g.ngayHenTra);
                                item.add("cuonSachGoiY", goiYJson);
                        } else {
                                item.addNull("cuonSachGoiY");
                        }

                        danhSach.add(item);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh sách chờ thành công",
                                Json.createObjectBuilder().add("danhSach", danhSach).build());
        }

        // ---------- ASSIGN_RESERVATION (thủ thư) ----------
        private String handleAssignReservation(JsonObject data) {

                int maDatTruoc = reqInt(data, "maDatTruoc");
                String maCuonSach = optString(data, "maCuonSach", null);

                if (maCuonSach != null && maCuonSach.trim().isEmpty()) {
                        maCuonSach = null;
                }

                DatTruocRepository.KetQuaGan r = datTruocRepo.ganCuonChoLuotDat(maDatTruoc, maCuonSach);

                if (r.daThongBao) {
                        thongBaoService.guiSachDatTruocDaVe(r.maDatTruoc);
                }

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maDatTruoc", r.maDatTruoc)
                                .add("trangThai", r.trangThai)
                                .add("maCuonSach", r.maCuonSach);

                addDate(result, "ngayHenTraCuaCuon", r.ngayHenTraCuaCuon);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                r.daThongBao
                                                ? "Đã giữ cuốn sách tại quầy và thông báo cho độc giả"
                                                : "Đã khóa cuốn sách cho lượt đặt trước",
                                result.build());
        }

        private long soNgayTreCua(ChiTietPhieuMuon ct) {
                LocalDate han = hanTraCua(ct);

                LocalDate moc = ct.getNgayTraThucTe() != null
                                ? ct.getNgayTraThucTe().toLocalDate()
                                : LocalDate.now();

                return (han != null && moc.isAfter(han))
                                ? ChronoUnit.DAYS.between(han, moc)
                                : 0;
        }

        private static String lyDoPhat(String tienTo, String ghiChu) {
                return ghiChu != null && !ghiChu.trim().isEmpty()
                                ? tienTo + ": " + ghiChu.trim()
                                : tienTo;
        }

        private static String reqLoaiPhat(JsonObject data) {
                String loai = reqString(data, "loaiPhat");

                if (!TrangThai.LOAI_PHAT_LATE.equals(loai)
                                && !TrangThai.VI_PHAM_DAMAGED.equals(loai)
                                && !TrangThai.VI_PHAM_LOST.equals(loai)) {

                        throw new BadRequestException("Thiếu hoặc sai tham số: loaiPhat");
                }

                return loai;
        }

        // ---------- GET_FINE_SUGGESTION ----------
        private String handleGetFineSuggestion(JsonObject data) {

                int maCTPM = reqInt(data, "maCTPM");
                String loai = reqLoaiPhat(data);

                ChiTietPhieuMuon ct = phieuMuonRepo.findChiTietFetch(maCTPM);

                if (ct == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Chi tiết phiếu mượn không tồn tại",
                                        null);
                }

                double soTien;
                String giaiThich;

                if (TrangThai.LOAI_PHAT_LATE.equals(loai)) {

                        long soNgayTre = soNgayTreCua(ct);

                        if (soNgayTre == 0) {
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Cuốn sách không trễ hạn",
                                                null);
                        }

                        double donGia = cauHinhRepo.getCauHinh().getTienPhatNgay();

                        soTien = soNgayTre * donGia;
                        giaiThich = soNgayTre + " ngày trễ × " + donGia + " VNĐ/ngày";

                } else {
                        Double giaBia = ct.getCuonSach().getDauSach().getGiaTien();

                        soTien = giaBia != null ? giaBia : 0.0;
                        giaiThich = "Giá bìa sách";
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Gợi ý số tiền phạt",
                                Json.createObjectBuilder()
                                                .add("loaiPhat", loai)
                                                .add("soTienGoiY", soTien)
                                                .add("giaiThich", giaiThich)
                                                .build());
        }

        // ---------- CREATE_FINE ----------
        private String handleCreateFine(JsonObject data) {

                int maCTPM = reqInt(data, "maCTPM");
                String loai = reqLoaiPhat(data);
                Double soTienGui = optDoubleOrNull(data, "soTienPhat");
                boolean daThanhToan = optBoolean(data, "daThanhToan", false);
                String ghiChu = optString(data, "ghiChu", null);

                if (soTienGui != null && soTienGui < 0) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Số tiền phạt phải lớn hơn hoặc bằng 0",
                                        null);
                }

                ChiTietPhieuMuon ct = phieuMuonRepo.findChiTietFetch(maCTPM);

                if (ct == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Chi tiết phiếu mượn không tồn tại",
                                        null);
                }

                CauHinh ch = cauHinhRepo.getCauHinh();
                Integer maPhieuPhat;

                if (TrangThai.VI_PHAM_LOST.equals(loai) && ct.getNgayTraThucTe() == null) {

                        // Mất sách khi chưa trả: kết thúc dòng mượn như khi trả sách mất
                        PhieuMuonRepository.YeuCauTra yc = new PhieuMuonRepository.YeuCauTra();
                        yc.maCTPM = maCTPM;
                        yc.tinhTrangKhiTra = "Mất";
                        yc.viPham = TrangThai.VI_PHAM_LOST;
                        yc.soTienPhat = soTienGui;
                        yc.ghiChu = ghiChu;

                        List<PhieuMuonRepository.KetQuaTra> kq = phieuMuonRepo.traSach(
                                        currentUser.getMaND(),
                                        List.of(yc),
                                        ch.getTienPhatNgay());

                        // Phiếu "Mất sách" luôn được tạo sau phiếu trễ hạn (nếu có)
                        List<PhieuMuonRepository.PhieuPhatTao> dsPhat = kq.get(0).phieuPhat;
                        maPhieuPhat = dsPhat.get(dsPhat.size() - 1).maPhieuPhat;

                        if (daThanhToan) {
                                phieuPhatRepo.thanhToanPhieuPhat(maPhieuPhat);
                        }

                } else {
                        double soTien;
                        String lyDo;

                        if (TrangThai.LOAI_PHAT_LATE.equals(loai)) {

                                long soNgayTre = soNgayTreCua(ct);

                                if (soNgayTre == 0) {
                                        return JsonUtil.buildResponse(
                                                        "ERROR",
                                                        "Cuốn sách không trễ hạn",
                                                        null);
                                }

                                if (phieuPhatRepo.daCoPhieuTreHan(maCTPM)) {
                                        return JsonUtil.buildResponse(
                                                        "ERROR",
                                                        "Dòng mượn này đã có phiếu phạt trễ hạn",
                                                        null);
                                }

                                soTien = soTienGui != null
                                                ? soTienGui
                                                : soNgayTre * ch.getTienPhatNgay();
                                lyDo = lyDoPhat("Trễ hạn " + soNgayTre + " ngày", ghiChu);

                        } else {
                                Double giaBia = ct.getCuonSach().getDauSach().getGiaTien();

                                soTien = soTienGui != null
                                                ? soTienGui
                                                : (giaBia != null ? giaBia : 0.0);
                                lyDo = lyDoPhat(
                                                TrangThai.VI_PHAM_LOST.equals(loai) ? "Mất sách" : "Hỏng sách",
                                                ghiChu);
                        }

                        PhieuPhat tao = phieuPhatRepo.taoPhieuPhat(
                                        maCTPM, currentUser.getMaND(), lyDo, soTien, daThanhToan);

                        maPhieuPhat = tao.getMaPhieuPhat();
                }

                PhieuPhat pp = phieuPhatRepo.findByIdFetch(maPhieuPhat);

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maPhieuPhat", pp.getMaPhieuPhat())
                                .add("maCTPM", maCTPM)
                                .add("maCuonSach",
                                                pp.getCuonSach() != null ? pp.getCuonSach().getMaCuonSach() : "")
                                .add("maND",
                                                pp.getNguoiDung() != null ? pp.getNguoiDung().getMaND() : -1)
                                .add("lyDo", str(pp.getLyDo()))
                                .add("soTienPhat", pp.getSoTienPhat())
                                .add("daThanhToan", Boolean.TRUE.equals(pp.getTrangThaiThanhToan()));

                addDateTime(result, "ngayLapPhieu", pp.getNgayLapPhieu());

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Lập phiếu phạt thành công!",
                                result.build());
        }

        // ---------- GET_FINES ----------
        private String handleGetFines(JsonObject data) {

                Boolean daThanhToan = optBooleanOrNull(data, "daThanhToan");
                Integer maThe;

                if (currentRoleId() == Role.READER_ID) {
                        if (currentMaThe == null) {
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Tài khoản chưa có thẻ độc giả",
                                                null);
                        }
                        maThe = currentMaThe; // độc giả chỉ xem phiếu của chính mình
                } else {
                        maThe = optInteger(data, "maThe");
                }

                int page = pageOf(data);
                int pageSize = pageSizeOf(data);

                List<PhieuPhat> ds = phieuPhatRepo.search(maThe, daThanhToan, page, pageSize);

                long total = phieuPhatRepo.count(maThe, daThanhToan);
                double tongChua = phieuPhatRepo.tongChuaThanhToan(maThe);

                JsonArrayBuilder items = Json.createArrayBuilder();

                for (PhieuPhat p : ds) {

                        ChiTietPhieuMuon ct = p.getChiTietPhieuMuon();
                        TheDocGia the = ct.getPhieuMuon().getTheDocGia();
                        NguoiDung nd = the.getNguoiDung();

                        CuonSach cs = p.getCuonSach();

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maPhieuPhat", p.getMaPhieuPhat())
                                        .add("maCTPM", ct.getMaCTPM())
                                        .add("maCuonSach", cs != null ? cs.getMaCuonSach() : "")
                                        .add("tenSach",
                                                        cs != null && cs.getDauSach() != null
                                                                        ? str(cs.getDauSach().getTenSach())
                                                                        : "")
                                        .add("maThe", the.getMaThe())
                                        .add("tenDocGia", nd != null ? str(nd.getHoTen()) : "");

                        addDateTime(item, "ngayLapPhieu", p.getNgayLapPhieu());

                        item.add("lyDo", str(p.getLyDo()))
                                        .add("soTienPhat", p.getSoTienPhat() != null ? p.getSoTienPhat() : 0.0)
                                        .add("daThanhToan", Boolean.TRUE.equals(p.getTrangThaiThanhToan()));

                        items.add(item);
                }

                JsonObjectBuilder result = Json.createObjectBuilder();
                addPaging(result, total, page, pageSize);
                result.add("tongChuaThanhToan", tongChua)
                                .add("items", items);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh sách phiếu phạt thành công",
                                result.build());
        }

        // ---------- PAY_FINE ----------
        private String handlePayFine(JsonObject data) {

                int maPhieuPhat = reqInt(data, "maPhieuPhat");

                switch (phieuPhatRepo.thanhToanPhieuPhat(maPhieuPhat)) {

                        case PhieuPhatRepository.THANH_TOAN_OK:
                                return JsonUtil.buildResponse(
                                                "SUCCESS",
                                                "Thanh toán phiếu phạt thành công",
                                                Json.createObjectBuilder()
                                                                .add("maPhieuPhat", maPhieuPhat)
                                                                .add("daThanhToan", true)
                                                                .build());

                        case PhieuPhatRepository.THANH_TOAN_KHONG_TIM_THAY:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Không tìm thấy phiếu phạt mã: " + maPhieuPhat,
                                                null);

                        case PhieuPhatRepository.THANH_TOAN_DA_NOP:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Phiếu phạt đã được thanh toán trước đó",
                                                null);

                        default:
                                return JsonUtil.buildResponse(
                                                "ERROR",
                                                "Lỗi thanh toán phiếu phạt!",
                                                null);
                }
        }

        private static final String MSG_TRUNG_DOC_GIA = "Email hoặc Số điện thoại đã tồn tại trên hệ thống";

        private static final String MSG_TRUNG_THU_THU = "Email hoặc Số điện thoại đã được sử dụng bởi tài khoản khác";

        // Chỉ kiểm tra các giá trị khác null (dùng cho cả thêm mới và sửa)
        private static void kiemTraThongTin(String hoTen, String email, String sdt) {
                if (hoTen != null && (hoTen.trim().isEmpty() || hoTen.length() > 100)) {
                        throw new BadRequestException("Họ tên không hợp lệ");
                }
                if (email != null && (!email.contains("@") || email.length() > 100)) {
                        throw new BadRequestException("Email không hợp lệ");
                }
                if (sdt != null && !sdt.matches("\\d{8,15}")) {
                        throw new BadRequestException("Số điện thoại không hợp lệ");
                }
        }

        private static void kiemTraMatKhau(String matKhau) {
                if (matKhau.length() < 6) {
                        throw new BadRequestException(
                                        "Mật khẩu không hợp lệ (tối thiểu 6 ký tự)!");
                }
        }

        // Chuỗi tùy chọn: trim, rỗng coi như không gửi
        private static String optTrim(JsonObject data, String key) {
                String s = optString(data, key, null);
                if (s == null) {
                        return null;
                }
                s = s.trim();
                return s.isEmpty() ? null : s;
        }

        private String handleGetReaders(JsonObject data) {

                String keyword = optString(data, "keyword", null);
                String trangThaiThe = optString(data, "trangThaiThe", null);
                int page = pageOf(data);
                int pageSize = pageSizeOf(data);

                List<TheDocGia> ds = theDocGiaRepo.search(keyword, trangThaiThe, page, pageSize);

                long total = theDocGiaRepo.count(keyword, trangThaiThe);

                List<Integer> maTheList = new ArrayList<>();
                for (TheDocGia t : ds) {
                        maTheList.add(t.getMaThe());
                }

                Map<Integer, Long> dangMuon = phieuMuonRepo.countActiveBorrowingByMaTheList(maTheList);

                JsonArrayBuilder items = Json.createArrayBuilder();

                for (TheDocGia t : ds) {
                        NguoiDung nd = t.getNguoiDung();

                        JsonObjectBuilder item = Json.createObjectBuilder()
                                        .add("maND", nd != null ? nd.getMaND() : -1)
                                        .add("hoTen", nd != null ? str(nd.getHoTen()) : "")
                                        .add("email", nd != null ? str(nd.getEmail()) : "")
                                        .add("soDienThoai", nd != null ? str(nd.getSoDienThoai()) : "")
                                        .add("trangThaiTaiKhoan",
                                                        nd != null && !Boolean.FALSE.equals(nd.getTrangThai()))
                                        .add("maThe", t.getMaThe());

                        addDate(item, "ngayCap", t.getNgayCap());
                        addDate(item, "ngayHetHan", t.getNgayHetHan());

                        item.add("trangThaiThe", TheDocGiaRepository.trangThaiHienThi(t))
                                        .add("dangMuon", dangMuon.getOrDefault(t.getMaThe(), 0L));

                        items.add(item);
                }

                JsonObjectBuilder result = Json.createObjectBuilder();
                addPaging(result, total, page, pageSize);
                result.add("items", items);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh sách độc giả thành công",
                                result.build());
        }

        private String handleAddReader(JsonObject data) {

                String hoTen = reqString(data, "hoTen").trim();
                String email = reqString(data, "email").trim();
                String soDienThoai = reqString(data, "soDienThoai").trim();
                String matKhau = reqString(data, "matKhau");

                LocalDate ngayCap = optDate(data, "ngayCap");
                LocalDate ngayHetHan = optDate(data, "ngayHetHan");

                if (ngayCap == null) {
                        ngayCap = LocalDate.now();
                }
                if (ngayHetHan == null) {
                        throw new BadRequestException("Thiếu hoặc sai tham số: ngayHetHan");
                }

                kiemTraThongTin(hoTen, email, soDienThoai);
                kiemTraMatKhau(matKhau);

                if (!ngayHetHan.isAfter(ngayCap)) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Ngày hết hạn phải sau ngày cấp",
                                        null);
                }

                if (nguoiDungRepo.existsEmailHoacSdt(email, soDienThoai, null)) {
                        return JsonUtil.buildResponse("ERROR", MSG_TRUNG_DOC_GIA, null);
                }

                NguoiDung nd = new NguoiDung();
                nd.setHoTen(hoTen);
                nd.setEmail(email);
                nd.setSoDienThoai(soDienThoai);
                nd.setMatKhau(PasswordUtil.hash(matKhau));

                TheDocGia the = new TheDocGia();
                the.setNgayCap(ngayCap);
                the.setNgayHetHan(ngayHetHan);

                if (!nguoiDungRepo.taoDocGia(nd, the)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không thể cấp thẻ độc giả!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cấp thẻ độc giả thành công!",
                                Json.createObjectBuilder()
                                                .add("maND", nd.getMaND())
                                                .add("maThe", the.getMaThe())
                                                .build());
        }

        private String handleUpdateReader(JsonObject data) {

                int maND = reqInt(data, "maND");
                String hoTen = optTrim(data, "hoTen");
                String email = optTrim(data, "email");
                String soDienThoai = optTrim(data, "soDienThoai");

                kiemTraThongTin(hoTen, email, soDienThoai);

                NguoiDung nd = nguoiDungRepo.findByIdAndVaiTro(maND, Role.READER_ID);

                if (nd == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy độc giả mã: " + maND,
                                        null);
                }

                if ((email != null || soDienThoai != null)
                                && nguoiDungRepo.existsEmailHoacSdt(email, soDienThoai, maND)) {

                        return JsonUtil.buildResponse("ERROR", MSG_TRUNG_DOC_GIA, null);
                }

                if (hoTen != null) {
                        nd.setHoTen(hoTen);
                }
                if (email != null) {
                        nd.setEmail(email);
                }
                if (soDienThoai != null) {
                        nd.setSoDienThoai(soDienThoai);
                }

                if (!nguoiDungRepo.updateNguoiDung(nd)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Lỗi cập nhật thông tin độc giả!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật thông tin độc giả thành công!",
                                null);
        }

        // Gia hạn thẻ (ngayHetHan) và/hoặc khóa / mở khóa (trangThai)
        private String handleUpdateCard(JsonObject data) {

                int maThe = reqInt(data, "maThe");
                LocalDate ngayHetHan = optDate(data, "ngayHetHan");
                String trangThai = optTrim(data, "trangThai");
                String lyDo = optTrim(data, "lyDo");

                if (ngayHetHan == null && trangThai == null) {
                        return JsonUtil.buildResponse(
                                        "BAD_REQUEST",
                                        "Thiếu tham số: ngayHetHan | trangThai",
                                        null);
                }

                TheDocGia the = theDocGiaRepo.capNhatThe(maThe, ngayHetHan, trangThai);

                if (the == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy thẻ độc giả mã: " + maThe,
                                        null);
                }

                // ERD chưa có cột lưu lý do khóa nên chỉ ghi log
                if (lyDo != null) {
                        System.out.println("🔒 [Thẻ] Thẻ " + maThe + " -> " + trangThai
                                        + " | lý do: " + lyDo);
                }

                JsonObjectBuilder result = Json.createObjectBuilder()
                                .add("maThe", the.getMaThe());

                addDate(result, "ngayCap", the.getNgayCap());
                addDate(result, "ngayHetHan", the.getNgayHetHan());
                result.add("trangThai", str(the.getTrangThai()));

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật thẻ độc giả thành công!",
                                result.build());
        }

        private String handleGetLibrarians(JsonObject data) {

                String keyword = optString(data, "keyword", null);
                int page = pageOf(data);
                int pageSize = pageSizeOf(data);

                List<NguoiDung> ds = nguoiDungRepo.searchByVaiTro(
                                Role.LIBRARIAN_ID, keyword, page, pageSize);

                long total = nguoiDungRepo.countByVaiTro(Role.LIBRARIAN_ID, keyword);

                JsonArrayBuilder items = Json.createArrayBuilder();

                for (NguoiDung n : ds) {
                        items.add(
                                        Json.createObjectBuilder()
                                                        .add("maND", n.getMaND())
                                                        .add("hoTen", str(n.getHoTen()))
                                                        .add("email", str(n.getEmail()))
                                                        .add("soDienThoai", str(n.getSoDienThoai()))
                                                        .add("trangThai", !Boolean.FALSE.equals(n.getTrangThai())));
                }

                JsonObjectBuilder result = Json.createObjectBuilder();
                addPaging(result, total, page, pageSize);
                result.add("items", items);

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Tải danh sách thủ thư thành công",
                                result.build());
        }

        private String handleAddLibrarian(JsonObject data) {

                String hoTen = reqString(data, "hoTen").trim();
                String email = reqString(data, "email").trim();
                String soDienThoai = reqString(data, "soDienThoai").trim();
                String matKhau = reqString(data, "matKhau");

                kiemTraThongTin(hoTen, email, soDienThoai);
                kiemTraMatKhau(matKhau);

                if (nguoiDungRepo.existsEmailHoacSdt(email, soDienThoai, null)) {
                        return JsonUtil.buildResponse("ERROR", MSG_TRUNG_THU_THU, null);
                }

                NguoiDung nd = new NguoiDung();
                nd.setHoTen(hoTen);
                nd.setEmail(email);
                nd.setSoDienThoai(soDienThoai);
                nd.setMatKhau(PasswordUtil.hash(matKhau));

                if (!nguoiDungRepo.saveNguoiDungVoiVaiTro(nd, Role.LIBRARIAN_ID)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không thể thêm thủ thư!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Thêm thủ thư thành công!",
                                Json.createObjectBuilder().add("maND", nd.getMaND()).build());
        }

        // Sửa thông tin, đặt lại mật khẩu, khóa / mở khóa (trangThai: false = khóa)
        private String handleUpdateLibrarian(JsonObject data) {

                int maND = reqInt(data, "maND");
                String hoTen = optTrim(data, "hoTen");
                String email = optTrim(data, "email");
                String soDienThoai = optTrim(data, "soDienThoai");
                String matKhau = optString(data, "matKhau", null);
                Boolean trangThai = optBooleanOrNull(data, "trangThai");

                kiemTraThongTin(hoTen, email, soDienThoai);

                if (matKhau != null) {
                        kiemTraMatKhau(matKhau);
                }

                NguoiDung nd = nguoiDungRepo.findByIdAndVaiTro(maND, Role.LIBRARIAN_ID);

                if (nd == null) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Không tìm thấy thủ thư mã: " + maND,
                                        null);
                }

                if ((email != null || soDienThoai != null)
                                && nguoiDungRepo.existsEmailHoacSdt(email, soDienThoai, maND)) {

                        return JsonUtil.buildResponse("ERROR", MSG_TRUNG_THU_THU, null);
                }

                if (hoTen != null) {
                        nd.setHoTen(hoTen);
                }
                if (email != null) {
                        nd.setEmail(email);
                }
                if (soDienThoai != null) {
                        nd.setSoDienThoai(soDienThoai);
                }
                if (matKhau != null) {
                        nd.setMatKhau(PasswordUtil.hash(matKhau));
                }
                if (trangThai != null) {
                        nd.setTrangThai(trangThai);
                }

                if (!nguoiDungRepo.updateNguoiDung(nd)) {
                        return JsonUtil.buildResponse(
                                        "ERROR",
                                        "Lỗi cập nhật thủ thư!",
                                        null);
                }

                return JsonUtil.buildResponse(
                                "SUCCESS",
                                "Cập nhật thủ thư thành công!",
                                null);
        }
}