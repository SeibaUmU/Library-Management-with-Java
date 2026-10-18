# API Specification - Quản Lý Thư Viện (Socket & JSON-P)

Tài liệu mô tả chi tiết định dạng các thông điệp JSON truyền nhận giữa **GUI Client** và **Java Socket Server** (sử dụng JPA Repository với CSDL `QLThuVien`). Toàn bộ 16 Use Case (UC-01 → UC-16) được ánh xạ sang các `action` ở Mục 2; cấu trúc dữ liệu bám theo ERD 13 bảng.

---

## 1. Giao Thức & Quy Ước Chung

### 1.1. Truyền nhận

* Kết nối TCP tới Server, cổng `8888`, mã hóa UTF-8.
* Mỗi Request / Response là **một chuỗi JSON compact nằm trên đúng 1 dòng**, kết thúc bằng ký tự xuống dòng `\n` (Server đọc bằng `readLine()`).
* Mỗi kết nối TCP là một **phiên làm việc (session)**. Client giữ kết nối mở trong suốt thời gian dùng ứng dụng.

### 1.2. Khung cấu trúc (JSON Envelope)

* **Client gửi Request:**

```
{
  "action": "TÊN_HÀNH_ĐỘNG",
  "data": { /* Dữ liệu tham số gửi kèm, hoặc null */ }
}
```

* **Server trả Response:**

```
{
  "status": "SUCCESS | ERROR | BAD_REQUEST | UNAUTHORIZED | FORBIDDEN",
  "message": "Thông báo hiển thị lên giao diện Client (GUI)",
  "data": { /* Dữ liệu kết quả trả về hoặc null */ }
}
```

### 1.3. Giá trị `status`

| status | Ý nghĩa |
| --- | --- |
| `SUCCESS` | Xử lý thành công |
| `ERROR` | Lỗi nghiệp vụ (vi phạm quy định, không tìm thấy dữ liệu, trùng dữ liệu...) hoặc lỗi hệ thống |
| `BAD_REQUEST` | Thiếu / sai kiểu / sai giá trị tham số, hoặc `action` không được hỗ trợ |
| `UNAUTHORIZED` | Chưa đăng nhập, sai tài khoản / mật khẩu, tài khoản bị khóa |
| `FORBIDDEN` | Đã đăng nhập nhưng vai trò không được phép dùng action này |

### 1.4. Phiên đăng nhập & phân quyền

* Sau `LOGIN` thành công, Server lưu người dùng hiện tại vào phiên. `LOGOUT` hoặc đóng kết nối thì xóa.
* Vì Server đã biết ai đang đăng nhập, Client **không gửi** `maThuThu`, `username` (đổi mật khẩu) hay `maThe` của chính độc giả: Server lấy từ phiên.
* Action cần đăng nhập mà chưa đăng nhập → `UNAUTHORIZED`, message `Vui lòng đăng nhập!`. Sai vai trò → `FORBIDDEN`, message `Bạn không có quyền thực hiện chức năng này!`.

| role | maVaiTro | Tác nhân |
| --- | --- | --- |
| `ADMIN` | 1 | Admin |
| `LIBRARIAN` | 2 | Thủ thư |
| `READER` | 3 | Độc giả |

> `maVaiTro = 2` là Thủ thư theo UC-15; giá trị 1 và 3 là quy ước, cần khớp với dữ liệu bảng `VaiTro`. Server nên phân quyền theo `MaVaiTro`, không so sánh chuỗi `TenVaiTro`.

Cột **Quyền** ở các bảng dưới: `Public` = không cần đăng nhập; `Đăng nhập` = mọi vai trò; còn lại liệt kê vai trò được phép.

### 1.5. Quy ước dữ liệu

* **Ngày:** `yyyy-MM-dd` (ví dụ `2026-10-29`). **Ngày giờ:** `yyyy-MM-dd'T'HH:mm:ss`. Ngày nằm trong `message` hiển thị theo `DD/MM/YYYY`.
* **Tiền:** số thực, đơn vị VNĐ.
* **Phân trang:** request gửi `page` (từ 1, mặc định 1) và `pageSize` (mặc định 10). Response trả `totalResults` (tổng số bản ghi, không phải số dòng của trang), `page`, `pageSize`, `totalPages`.
* Tham số tùy chọn có thể bỏ hoặc gửi `null`.
* Thiếu / sai kiểu tham số bắt buộc → `BAD_REQUEST`, message `Thiếu hoặc sai tham số: <tên tham số>`.

### 1.6. Hằng số trạng thái

| Bảng.Cột | Giá trị |
| --- | --- |
| `CuonSach.TrangThai` | `AVAILABLE` (sẵn sàng), `BORROWED` (đang mượn), `RESERVED` (đang giữ cho người đặt trước), `LOST` (mất), `DAMAGED` (hỏng / thanh lý, không cho mượn) |
| `DatTruoc.TrangThai` | `WAITING`, `RESERVED`, `NOTIFIED`, `COMPLETED`, `CANCELLED` |
| `PhieuMuon.TrangThai` | `BORROWING`, `RETURNED` |
| `TheDocGia.TrangThai` | `ACTIVE`, `LOCKED` |
| `NguoiDung.TrangThai` | `true` = hoạt động, `false` = bị khóa |
| `PhieuPhat.TrangThaiThanhToan` | `true` = đã nộp, `false` = chưa nộp |

Các quy ước đi kèm:

* "Quá hạn" **không lưu** trong DB mà tính khi truy vấn: `ngayHenTra < hôm nay` và chưa trả. Tương tự thẻ "hết hạn": `NgayHetHan < hôm nay`. Trong response, `trangThaiThe` nhận `ACTIVE | LOCKED | EXPIRED | NONE` (`NONE` = chưa có thẻ).
* `DauSach.SoLuongKhaDung` = số cuốn đang `AVAILABLE`. `DauSach.TongSoLuong` = số cuốn có trạng thái khác `LOST`. Mọi action làm đổi `CuonSach.TrangThai` phải cập nhật hai số này **trong cùng transaction**.

---

## 2. Bảng Tổng Hợp Use Case ↔ Action

| UC | Tên Use Case | Action | Quyền |
| --- | --- | --- | --- |
| UC-01 | Lập phiếu mượn sách | `GET_COPY_BY_BARCODE`, `CREATE_BORROW_TICKET` | LIBRARIAN |
| UC-02 | Xử lý trả sách | `GET_BORROW_HISTORY` (tab `BORROWING`), `PROCESS_RETURN_BOOK` | LIBRARIAN |
| UC-03 | Tra cứu & xem tình trạng sách | `SEARCH_BOOKS`, `GET_BOOK_DETAIL`, `GET_CATEGORIES` | Public |
| UC-04 | Cấu hình quy định hệ thống | `GET_SYSTEM_CONFIG` (Đăng nhập), `UPDATE_SYSTEM_CONFIG` (ADMIN) | xem cột Action |
| UC-05 | Đăng nhập / Đổi mật khẩu | `LOGIN` (Public), `CHANGE_PASSWORD` (Đăng nhập) | xem cột Action |
| UC-06 | Đăng xuất | `LOGOUT` | Đăng nhập |
| UC-07 | Đặt trước sách | `RESERVE_BOOK`, `CANCEL_RESERVATION`, `GET_MY_RESERVATIONS` (READER); `GET_WAITING_RESERVATIONS`, `ASSIGN_RESERVATION` (LIBRARIAN) | xem cột Action |
| UC-08 | Xem lịch sử mượn / hạn trả | `GET_BORROW_HISTORY` | READER, LIBRARIAN |
| UC-09 | Gia hạn sách | `RENEW_BOOK` | READER, LIBRARIAN |
| UC-10 | Nhận thông báo sách đặt trước | Không có action riêng: Server tự kích hoạt trong `PROCESS_RETURN_BOOK`, Client nhận qua `GET_MY_RESERVATIONS` | READER |
| UC-11 | Quản lý độc giả (thẻ) | `GET_READERS`, `ADD_READER`, `UPDATE_READER`, `UPDATE_CARD` | LIBRARIAN |
| UC-12 | Quản lý kho sách & danh mục | `ADD_BOOK_TITLE`, `UPDATE_BOOK_TITLE`, `DELETE_BOOK_TITLE`, `ADD_BOOK_COPIES`, `UPDATE_COPY`, `ADD_CATALOG`, `UPDATE_CATALOG`, `DELETE_CATALOG` | LIBRARIAN |
| UC-13 | Kiểm tra điều kiện thẻ | `CHECK_CARD_ELIGIBILITY` (Server cũng gọi nội bộ trong `CREATE_BORROW_TICKET`) | LIBRARIAN |
| UC-14 | Lập phiếu phạt (Mất/Hỏng/Quá hạn) | `GET_FINE_SUGGESTION`, `CREATE_FINE`, `PAY_FINE` (LIBRARIAN); `GET_FINES` (LIBRARIAN, READER) | xem cột Action |
| UC-15 | Quản lý thủ thư | `GET_LIBRARIANS`, `ADD_LIBRARIAN`, `UPDATE_LIBRARIAN` | ADMIN |
| UC-16 | Xem thống kê & báo cáo | `GET_REPORTS` | ADMIN, LIBRARIAN |

---

## 3. Chi Tiết API Request - Response Theo Từng Nghiệp Vụ

## 3.1. Tài Khoản (UC-05, UC-06)

#### 3.1.1. Đăng nhập (`LOGIN`)

* **UC:** UC-05 · **Quyền:** Public
* **Mô tả:** Đăng nhập bằng Email hoặc Số điện thoại (Admin, Thủ thư, Độc giả). Mật khẩu được lưu dạng băm trong CSDL; Server so khớp bằng hàm kiểm tra băm.
* **Request:**

```
{
  "action": "LOGIN",
  "data": {
    "username": "client1@gmail.com",
    "password": "Pass123"
  }
}
```

* **Response thành công (Độc giả có thẻ):**

```
{
  "status": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "maND": 3,
    "fullName": "Nguyen Văn A",
    "email": "client1@gmail.com",
    "phone": "0912345678",
    "maVaiTro": 3,
    "role": "READER",
    "maThe": 1,
    "trangThaiThe": "ACTIVE",
    "ngayHetHanThe": "2027-10-01"
  }
}
```

* **Với Admin / Thủ thư:** `maThe` = `-1`, `trangThaiThe` = `"NONE"`, `ngayHetHanThe` = `null`; `role` = `"ADMIN"` hoặc `"LIBRARIAN"`.
* **Lỗi:**
  * `UNAUTHORIZED` — `Tên đăng nhập hoặc mật khẩu không chính xác!`
  * `UNAUTHORIZED` — `Tài khoản của bạn đã bị khóa. Vui lòng liên hệ Admin`
  * `BAD_REQUEST` — `Dữ liệu yêu cầu trống!`

#### 3.1.2. Đổi mật khẩu (`CHANGE_PASSWORD`)

* **UC:** UC-05 (luồng B) · **Quyền:** Đăng nhập
* **Mô tả:** Đổi mật khẩu của người dùng đang đăng nhập (Server lấy tài khoản từ phiên).
* **Request:**

```
{
  "action": "CHANGE_PASSWORD",
  "data": {
    "oldPassword": "Pass123",
    "newPassword": "NewPass456",
    "confirmPassword": "NewPass456"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Đổi mật khẩu thành công!",
  "data": null
}
```

* **Lỗi:**
  * `ERROR` — `Mật khẩu cũ không chính xác!`
  * `ERROR` — `Mật khẩu xác nhận không khớp!`
  * `BAD_REQUEST` — `Mật khẩu mới không hợp lệ (tối thiểu 6 ký tự)!` *(quy tắc đề xuất, có thể đổi)*

#### 3.1.3. Đăng xuất (`LOGOUT`)

* **UC:** UC-06 · **Quyền:** Đăng nhập
* **Mô tả:** Kết thúc phiên làm việc. Hộp thoại "Bạn có chắc chắn muốn đăng xuất?" do Client hiển thị; Client chỉ gửi action này sau khi người dùng chọn **Đồng ý**. Sau đó Client tự đóng màn hình và quay về trang Đăng nhập.
* **Request:**

```
{
  "action": "LOGOUT",
  "data": null
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Đăng xuất thành công",
  "data": null
}
```

---

## 3.2. Tra Cứu Sách (UC-03)

#### 3.2.1. Tìm kiếm đầu sách (`SEARCH_BOOKS`)

* **UC:** UC-03 · **Quyền:** Public
* **Mô tả:** Tìm theo từ khóa (khớp **Tên sách, Tác giả, Thể loại hoặc NXB**), lọc thêm theo thể loại, có phân trang.
* **Request:**

```
{
  "action": "SEARCH_BOOKS",
  "data": {
    "keyword": "Đầu Sách",
    "maTL": 1,
    "page": 1,
    "pageSize": 10
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tìm kiếm sách thành công",
  "data": {
    "totalResults": 2,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1,
    "books": [
      {
        "maDauSach": 1,
        "tenSach": "Đầu Sách Số 1",
        "giaTien": 51000.0,
        "tacGia": "Nguyễn Văn A",
        "theLoai": "Văn Học",
        "nhaXuatBan": "NXB Kim Đồng",
        "viTri": "Kệ A2",
        "tongSoLuong": 3,
        "soLuongKhaDung": 1
      }
    ]
  }
}
```

* **Không có kết quả (3a):** `status` = `SUCCESS`, `message` = `Không tìm thấy sách phù hợp với từ khóa đã nhập`, `totalResults` = `0`, `books` = `[]`.

#### 3.2.2. Xem chi tiết đầu sách (`GET_BOOK_DETAIL`)

* **UC:** UC-03 · **Quyền:** Public
* **Mô tả:** Lấy thông tin đầu sách, vị trí kệ, số lượng khả dụng và danh sách cuốn sách vật lý.
* **Request:**

```
{
  "action": "GET_BOOK_DETAIL",
  "data": {
    "maDauSach": 1
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Lấy chi tiết đầu sách thành công",
  "data": {
    "maDauSach": 1,
    "tenSach": "Đầu Sách Số 1",
    "giaTien": 51000.0,
    "tacGia": "Nguyễn Văn A",
    "theLoai": "Văn Học",
    "nhaXuatBan": "NXB Kim Đồng",
    "namXuatBan": 2020,
    "viTri": "Kệ A2",
    "tongSoLuong": 2,
    "soLuongKhaDung": 1,
    "choPhepDatTruoc": false,
    "cuonSachList": [
      {
        "maCuonSach": "BARCODE-1",
        "tinhTrangVatLy": "Mới",
        "trangThai": "AVAILABLE"
      },
      {
        "maCuonSach": "BARCODE-2",
        "tinhTrangVatLy": "Tốt",
        "trangThai": "BORROWED"
      }
    ]
  }
}
```

* **Hết sách (5a):** khi `soLuongKhaDung` = `0` thì `choPhepDatTruoc` = `true` và `message` = `Sách hiện tại đã được mượn hết`. Client dựa vào đó hiện nút **Đặt trước sách** (gọi `RESERVE_BOOK`, mục 3.5.1).
* **Lỗi:** `BAD_REQUEST` — `Thiếu hoặc sai tham số: maDauSach`; `ERROR` — `Không tìm thấy đầu sách mã: <maDauSach>`

#### 3.2.3. Lấy danh mục dùng chung (`GET_CATEGORIES`)

* **UC:** UC-03, UC-12 · **Quyền:** Public
* **Mô tả:** Trả toàn bộ Tác giả, NXB, Thể loại để đổ vào combobox / bộ lọc.
* **Request:**

```
{
  "action": "GET_CATEGORIES",
  "data": null
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh mục thành công",
  "data": {
    "tacGiaList": [ { "maTG": 1, "tenTacGia": "Nguyễn Văn A" } ],
    "nhaXuatBanList": [ { "maNXB": 1, "tenNXB": "NXB Kim Đồng", "namXuatBan": 2020 } ],
    "theLoaiList": [ { "maTL": 1, "tenTL": "Văn Học" } ]
  }
}
```

---

## 3.3. Quản Lý Kho Sách & Danh Mục (UC-12)

Quyền của toàn bộ mục này: **LIBRARIAN**.

#### 3.3.1. Thêm đầu sách (`ADD_BOOK_TITLE`)
Bonus: tenSach, maTG, maNXB, maTL bắt buộc (cột NOT NULL trong CSDL; lấy danh sách từ GET_CATEGORIES), giaTien ≥ 0, viTri tùy chọn. Thêm lỗi: Giá tiền phải lớn hơn hoặc bằng 0; Không tìm thấy tác giả / nhà xuất bản / thể loại mã: X. Sửa tham số thiếu thành Thiếu hoặc sai tham số: <tên>
* **Mô tả:** Thêm đầu sách mới. `tenSach` bắt buộc, `giaTien` ≥ 0; các khóa ngoại và `viTri` có thể bỏ trống. Server đặt `tongSoLuong` = 0 và `soLuongKhaDung` = 0 (cuốn vật lý được nhập qua `ADD_BOOK_COPIES`).
* **Request:**

```
{
  "action": "ADD_BOOK_TITLE",
  "data": {
    "tenSach": "Lập trình Java Nâng Cao",
    "giaTien": 150000.0,
    "maTG": 1,
    "maNXB": 1,
    "maTL": 2,
    "viTri": "Kệ B1"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Thêm đầu sách mới thành công!",
  "data": { "maDauSach": 15 }
}
```

#### 3.3.2. Sửa đầu sách (`UPDATE_BOOK_TITLE`)

* **Request:** chỉ gửi các trường cần đổi (ngoài `maDauSach` bắt buộc).

```
{
  "action": "UPDATE_BOOK_TITLE",
  "data": {
    "maDauSach": 15,
    "tenSach": "Lập trình Java Nâng Cao (Tái bản)",
    "giaTien": 165000.0,
    "viTri": "Kệ B2"
  }
}
```

* **Response:** `SUCCESS` — `Cập nhật đầu sách thành công!`, `data` = `null`.

#### 3.3.3. Xóa đầu sách (`DELETE_BOOK_TITLE`)

* **Request:** `{ "action": "DELETE_BOOK_TITLE", "data": { "maDauSach": 15 } }`
* **Response:** `SUCCESS` — `Xóa đầu sách thành công!`
* **Lỗi (4a):**
  * `ERROR` — `Không thể xóa tài liệu/danh mục đang trong giao dịch mượn/trả chưa kết thúc`
  * `ERROR` — `Đầu sách đã phát sinh giao dịch nên không thể xóa. Hãy cập nhật các cuốn sang tình trạng thanh lý.`
* Chỉ xóa được khi **mọi cuốn** thuộc đầu sách chưa từng phát sinh giao dịch; các cuốn đó bị xóa kèm.

#### 3.3.4. Nhập cuốn sách vật lý vào kho (`ADD_BOOK_COPIES`)

* **Mô tả:** Thêm các cuốn sách vật lý (mã vạch) cho một đầu sách. Cuốn mới có `trangThai` = `AVAILABLE`; Server cộng `tongSoLuong` và `soLuongKhaDung` tương ứng. Thao tác "tất cả hoặc không".
* **Request:**

```
{
  "action": "ADD_BOOK_COPIES",
  "data": {
    "maDauSach": 15,
    "maCuonSachList": ["BARCODE-10", "BARCODE-11"],
    "tinhTrangVatLy": "Mới"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Nhập cuốn sách vào kho thành công!",
  "data": {
    "maDauSach": 15,
    "soCuonDaThem": 2,
    "tongSoLuong": 2,
    "soLuongKhaDung": 2
  }
}
```

* **Lỗi:** `ERROR` — `Mã cuốn sách đã tồn tại: BARCODE-10`, Thêm lỗi Danh sách mã cuốn sách trống! và Không tìm thấy đầu sách mã: X. Mã trùng ngay trong danh sách cũng báo Mã cuốn sách đã tồn tại

#### 3.3.5. Cập nhật tình trạng cuốn sách vật lý (`UPDATE_COPY`)
Bonus: Thêm lỗi: Thiếu tham số: tinhTrangVatLy | trangThai; Thiếu hoặc sai tham số: trangThai; Không tìm thấy cuốn sách mã: X
* **Mô tả:** Đổi `tinhTrangVatLy` (Mới, Tốt, Cũ, Hỏng nhẹ, Thanh lý...). `trangThai` (tùy chọn) chỉ nhận `AVAILABLE | DAMAGED | LOST` và **chỉ đổi được khi cuốn không ở `BORROWED` / `RESERVED`**. Cuốn thanh lý: đặt `tinhTrangVatLy` = `Thanh lý`, `trangThai` = `DAMAGED`.
* **Request:**

```
{
  "action": "UPDATE_COPY",
  "data": {
    "maCuonSach": "BARCODE-10",
    "tinhTrangVatLy": "Hỏng nhẹ",
    "trangThai": "DAMAGED"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Cập nhật cuốn sách thành công!",
  "data": {
    "maCuonSach": "BARCODE-10",
    "tinhTrangVatLy": "Hỏng nhẹ",
    "trangThai": "DAMAGED",
    "tongSoLuong": 2,
    "soLuongKhaDung": 1
  }
}
```

* **Lỗi:** `ERROR` — `Cuốn sách đang được mượn/đặt trước, không thể đổi trạng thái`

#### 3.3.6. Danh mục Tác giả / NXB / Thể loại (`ADD_CATALOG`, `UPDATE_CATALOG`, `DELETE_CATALOG`)

* **Mô tả:** Ba action dùng chung cho 3 danh mục, phân biệt bằng `loai` = `AUTHOR | PUBLISHER | CATEGORY`. `namXuatBan` chỉ áp dụng cho `PUBLISHER`.
* **Request thêm:**

```
{
  "action": "ADD_CATALOG",
  "data": {
    "loai": "PUBLISHER",
    "ten": "NXB Trẻ",
    "namXuatBan": 2019
  }
}
```

* **Request sửa:** `{ "action": "UPDATE_CATALOG", "data": { "loai": "AUTHOR", "id": 1, "ten": "Nguyễn Văn An" } }`
* **Request xóa:** `{ "action": "DELETE_CATALOG", "data": { "loai": "CATEGORY", "id": 3 } }`
* **Response (cả ba):**

```
{
  "status": "SUCCESS",
  "message": "Thao tác danh mục thành công!",
  "data": { "loai": "PUBLISHER", "id": 2 }
}
```

* **Lỗi xóa (4a):** `ERROR` — `Không thể xóa danh mục đang được đầu sách sử dụng`

---

## 3.4. Mượn - Trả - Gia Hạn - Lịch Sử (UC-13, UC-01, UC-02, UC-09, UC-08)

#### 3.4.1. Kiểm tra điều kiện thẻ (`CHECK_CARD_ELIGIBILITY`)

* **UC:** UC-13 · **Quyền:** LIBRARIAN
* **Mô tả:** Thủ thư nhập mã thẻ (bước 2 của UC-01); Server kiểm tra lần lượt: trạng thái thẻ → hạn thẻ → nợ phạt → sách quá hạn → hạn mức mượn. Dù đủ hay không đủ điều kiện, nếu thẻ tồn tại thì `status` = `SUCCESS` và kết quả nằm ở `data.eligible`.
* **Request:**

```
{
  "action": "CHECK_CARD_ELIGIBILITY",
  "data": { "maThe": 1 }
}
```

* **Response (đủ điều kiện):**

```
{
  "status": "SUCCESS",
  "message": "Thẻ đủ điều kiện mượn sách",
  "data": {
    "eligible": true,
    "reasonCode": null,
    "docGia": {
      "maND": 3,
      "hoTen": "Nguyen Văn A",
      "email": "client1@gmail.com",
      "soDienThoai": "0912345678",
      "maThe": 1,
      "ngayCap": "2026-01-01",
      "ngayHetHan": "2027-01-01",
      "trangThaiThe": "ACTIVE"
    },
    "dangMuon": 2,
    "soSachToiDa": 5,
    "conDuocMuon": 3
  }
}
```

* **Response (không đủ điều kiện):** `message` = lý do, `eligible` = `false`, `reasonCode` theo bảng:

| reasonCode | message |
| --- | --- |
| `CARD_LOCKED` | `Thẻ đang bị khóa` |
| `CARD_EXPIRED` | `Thẻ hết hạn` |
| `HAS_UNPAID_FINE` | `Đang nợ tiền phạt chưa nộp` |
| `HAS_OVERDUE_BOOK` | `Đang có sách quá hạn chưa trả` |
| `BORROW_LIMIT_REACHED` | `Đã mượn đủ số lượng tối đa` |

* **Lỗi:** `ERROR` — `Không tìm thấy thẻ độc giả mã: <maThe>`

#### 3.4.2. Tra cứu cuốn sách theo mã vạch (`GET_COPY_BY_BARCODE`)

* **UC:** UC-01 (bước 5–6) · **Quyền:** LIBRARIAN
* **Mô tả:** Thủ thư quét mã vạch; Server trả Tên sách, Tác giả và **Ngày hẹn trả dự kiến** (hôm nay + `soNgayMuonToiDa`). Chỉ trả `SUCCESS` khi cuốn **được phép mượn bởi độc giả này** (xem quy tắc ở 3.4.3).
* **Request:**

```
{
  "action": "GET_COPY_BY_BARCODE",
  "data": {
    "maCuonSach": "BARCODE-1",
    "maThe": 1
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Cuốn sách sẵn sàng để mượn",
  "data": {
    "maCuonSach": "BARCODE-1",
    "maDauSach": 1,
    "tenSach": "Đầu Sách Số 1",
    "tacGia": "Nguyễn Văn A",
    "tinhTrangVatLy": "Mới",
    "ngayHenTraDuKien": "2026-10-29"
  }
}
```

* **Lỗi:**
  * `ERROR` — `Cuốn sách không sẵn sàng để mượn` (5a: không tồn tại, đã mượn, mất / hỏng)
  * `ERROR` — `Sách đã được người khác đặt trước, không thể lập phiếu mượn` (5b)

#### 3.4.3. Lập phiếu mượn sách (`CREATE_BORROW_TICKET`)

* **UC:** UC-01 · **Quyền:** LIBRARIAN
* **Mô tả:** Thủ thư lập phiếu mượn cho độc giả. Server tự lấy thủ thư từ phiên. Dùng khóa bi quan (`PESSIMISTIC_WRITE`) trên từng `CuonSach` để chống tranh chấp.
* **Request:**

```
{
  "action": "CREATE_BORROW_TICKET",
  "data": {
    "maThe": 1,
    "danhSachMaCuonSach": ["BARCODE-1", "BARCODE-2"]
  }
}
```

* **Response thành công:**

```
{
  "status": "SUCCESS",
  "message": "Lập phiếu mượn thành công!",
  "data": {
    "maPhieuMuon": 101,
    "maThe": 1,
    "ngayMuon": "2026-10-15T09:30:00",
    "chiTiet": [
      { "maCTPM": 50, "maCuonSach": "BARCODE-1", "tenSach": "Đầu Sách Số 1", "ngayHenTra": "2026-10-29" },
      { "maCTPM": 51, "maCuonSach": "BARCODE-2", "tenSach": "Đầu Sách Số 2", "ngayHenTra": "2026-10-29" }
    ]
  }
}
```

* **Lỗi:**
  * `BAD_REQUEST` — `Danh sách cuốn sách trống!`
  * `ERROR` — lý do không đủ điều kiện thẻ (bảng ở 3.4.1, ví dụ `Thẻ hết hạn`)
  * `ERROR` — `Vượt quá số lượng sách mượn tối đa (5 cuốn)! Hiện đang mượn: 4`
  * `ERROR` — `Cuốn sách không sẵn sàng để mượn: BARCODE-2`
  * `ERROR` — `Sách đã được người khác đặt trước, không thể lập phiếu mượn`
* **Quy tắc xét một cuốn được phép mượn:**
  1. Cuốn tồn tại và `AVAILABLE`; **hoặc** `RESERVED` mà `DatTruoc` của chính độc giả này đang `NOTIFIED` đúng cuốn đó.
  2. Với cuốn `AVAILABLE`: từ chối (5b) nếu số lượt `WAITING` của **người khác** trên đầu sách ≥ số cuốn `AVAILABLE` của đầu sách *(quy tắc đề xuất, docx chỉ nói "chưa tới lượt")*.
* **Xử lý phía Server (cùng 1 transaction):**
  * Gọi UC-13 cho thẻ; lưu `PhieuMuon` (`TrangThai` = `BORROWING`) và từng `ChiTietPhieuMuon` kèm `NgayHenTra` = hôm nay + `soNgayMuonToiDa`, `SoLanGiaHan` = 0.
  * Cuốn `AVAILABLE` → `BORROWED`, `soLuongKhaDung` − 1. Cuốn `RESERVED` → `BORROWED` (`soLuongKhaDung` không đổi).
  * Nếu độc giả có `DatTruoc` tương ứng đầu sách (`NOTIFIED`, hoặc `WAITING` khi mượn cuốn `AVAILABLE`) → chuyển `COMPLETED`.
  * Hỗ trợ in phiếu giấy do Client thực hiện từ dữ liệu `data` trả về.

#### 3.4.4. Xử lý trả sách (`PROCESS_RETURN_BOOK`)

* **UC:** UC-02 (extend UC-14, kích hoạt UC-10) · **Quyền:** LIBRARIAN
* **Mô tả:** Trả một hoặc nhiều cuốn trong một lần xác nhận. Thủ thư tra cứu các cuốn đang mượn bằng `GET_BORROW_HISTORY` (tab `BORROWING`, tìm theo mã phiếu / mã thẻ / mã cuốn), tích chọn rồi gửi. Thao tác "tất cả hoặc không".
* **Request:**

```
{
  "action": "PROCESS_RETURN_BOOK",
  "data": {
    "danhSachTra": [
      { "maCTPM": 50, "tinhTrangKhiTra": "Bình thường" },
      {
        "maCTPM": 51,
        "tinhTrangKhiTra": "Rách bìa",
        "viPham": "DAMAGED",
        "soTienPhat": 30000.0,
        "ghiChu": "Rách bìa"
      }
    ]
  }
}
```

  * `tinhTrangKhiTra`: tình trạng vật lý khi thu hồi (≤ 50 ký tự), ghi vào `TrangThaiSachKhiTra` và `CuonSach.TinhTrangVatLy`.
  * `viPham` (tùy chọn): `DAMAGED` hoặc `LOST` (UC-02 4a → UC-14). `soTienPhat` (tùy chọn) mặc định bằng `DauSach.GiaTien`; `ghiChu` (tùy chọn) nối vào lý do phạt.
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Trả sách thành công. Tổng tiền phạt: 45000.0 VNĐ",
  "data": {
    "tongTienPhat": 45000.0,
    "coPhat": true,
    "ketQua": [
      {
        "maCTPM": 50,
        "maCuonSach": "BARCODE-1",
        "tenSach": "Đầu Sách Số 1",
        "soNgayTre": 3,
        "trangThaiCuonSach": "AVAILABLE",
        "phieuPhat": [
          { "maPhieuPhat": 7, "lyDo": "Trễ hạn 3 ngày", "soTienPhat": 15000.0 }
        ],
        "datTruocDuocThongBao": null
      },
      {
        "maCTPM": 51,
        "maCuonSach": "BARCODE-2",
        "tenSach": "Đầu Sách Số 2",
        "soNgayTre": 0,
        "trangThaiCuonSach": "DAMAGED",
        "phieuPhat": [
          { "maPhieuPhat": 8, "lyDo": "Hỏng sách: Rách bìa", "soTienPhat": 30000.0 }
        ],
        "datTruocDuocThongBao": null
      }
    ]
  }
}
```

* **Trả đúng hạn, không vi phạm:** `message` = `Trả sách thành công!`, `tongTienPhat` = `0.0`, `coPhat` = `false`, `phieuPhat` = `[]`.
* **Lỗi:**
  * `BAD_REQUEST` — `Danh sách trả sách trống!` / `Giá trị viPham không hợp lệ!`
  * `ERROR` — `Chi tiết phiếu mượn không tồn tại hoặc đã được trả trước đó: <maCTPM>` (không dòng nào được lưu)
  * `BAD_REQUEST` — `Số tiền phạt phải lớn hơn hoặc bằng 0`
* **Xử lý phía Server (cùng 1 transaction):**
  * Ghi `NgayTraThucTe` = bây giờ; so với `ChiTietPhieuMuon.NgayHenTra`: trễ N ngày → tự tạo `PhieuPhat` (`LyDo` = `Trễ hạn N ngày`, tiền = N × `tienPhatNgay`).
  * `viPham` có giá trị → tạo thêm `PhieuPhat` (UC-14). Cuốn → `DAMAGED` hoặc `LOST` (`LOST`: `TrangThaiSachKhiTra` = `Mất`, `tongSoLuong` − 1). Cuốn không vi phạm → `AVAILABLE`, `soLuongKhaDung` + 1.
  * **Cuốn đang `RESERVED`:** giữ nguyên `RESERVED` (cất ở quầy, **không** cộng `soLuongKhaDung`), `DatTruoc` `RESERVED` → `NOTIFIED`, ghi `NgayThongBao` và kích hoạt UC-10 (3.5.6); `datTruocDuocThongBao` trả `maDatTruoc` đó. Nếu cuốn `RESERVED` bị mất / hỏng: `DatTruoc` quay về `WAITING` (`MaCuonSach` = `null`).
  * Khi mọi dòng của phiếu đã trả → `PhieuMuon.TrangThai` = `RETURNED`.

#### 3.4.5. Gia hạn sách (`RENEW_BOOK`)

* **UC:** UC-09 · **Quyền:** READER (chỉ cuốn của mình), LIBRARIAN
* **Request:**

```
{
  "action": "RENEW_BOOK",
  "data": { "maCTPM": 50 }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Gia hạn thành công. Ngày hẹn trả mới là: 12/11/2026",
  "data": {
    "maCTPM": 50,
    "maCuonSach": "BARCODE-1",
    "tenSach": "Đầu Sách Số 1",
    "ngayHenTraCu": "2026-10-29",
    "ngayHenTraMoi": "2026-11-12",
    "soLanGiaHan": 1,
    "soLanGiaHanToiDa": 2
  }
}
```

* **Điều kiện (kiểm tra theo thứ tự) và lỗi `ERROR`:**
  * Dòng mượn tồn tại, chưa trả — `Chi tiết phiếu mượn không tồn tại hoặc sách đã được trả`
  * Chưa quá hạn — `Sách đã quá hạn. Vui lòng mang sách đến quầy thủ thư để xử lý` (3a)
  * `soLanGiaHan` < `soLanGiaHanToiDa` — `Sách đã đạt số lần gia hạn tối đa` (3b)
  * Đầu sách không có lượt đặt trước `WAITING` / `RESERVED` và cuốn không ở trạng thái `RESERVED` — `Sách đã được độc giả khác đặt trước, không thể gia hạn thêm` (3c)
  * `FORBIDDEN` — `Bạn không có quyền gia hạn cuốn sách này` (READER gia hạn cuốn của người khác)
* **Xử lý:** `NgayHenTra` mới = `NgayHenTra` cũ + `soNgayMuonToiDa`; `SoLanGiaHan` + 1.

#### 3.4.6. Lịch sử mượn / sách đang mượn (`GET_BORROW_HISTORY`)
Bonus: Khi rỗng: độc giả nhận Bạn chưa có giao dịch mượn sách nào, thủ thư nhận Không có giao dịch phù hợp
* **UC:** UC-08 (Độc giả xem lịch sử), UC-02 (Thủ thư tra cứu sách đang mượn để trả) · **Quyền:** READER, LIBRARIAN
* **Mô tả:** `tab` = `BORROWING` (đang mượn) hoặc `RETURNED` (lịch sử trả).
  * **READER:** Server luôn ép theo thẻ của chính mình (bỏ qua `maThe`, `maPhieuMuon`, `maCuonSach`).
  * **LIBRARIAN:** phải gửi ít nhất một trong `maThe` | `maPhieuMuon` | `maCuonSach`.
  * `keyword` lọc theo tên sách; `tuNgay` / `denNgay` lọc theo ngày mượn.
* **Request:**

```
{
  "action": "GET_BORROW_HISTORY",
  "data": {
    "tab": "BORROWING",
    "maThe": 1,
    "maPhieuMuon": null,
    "maCuonSach": null,
    "keyword": "Java",
    "tuNgay": "2026-09-01",
    "denNgay": "2026-10-31",
    "page": 1,
    "pageSize": 10
  }
}
```

* **Response (`tab` = `BORROWING`):**

```
{
  "status": "SUCCESS",
  "message": "Tải lịch sử mượn trả thành công",
  "data": {
    "tab": "BORROWING",
    "totalResults": 1,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1,
    "items": [
      {
        "maCTPM": 50,
        "maPhieuMuon": 101,
        "maThe": 1,
        "tenDocGia": "Nguyen Văn A",
        "maCuonSach": "BARCODE-1",
        "tenSach": "Đầu Sách Số 1",
        "ngayMuon": "2026-10-15T09:30:00",
        "ngayHenTra": "2026-10-29",
        "soLanGiaHan": 0,
        "quaHan": false,
        "soNgayTre": 0,
        "tienPhatDuKien": 0.0,
        "coTheGiaHan": true
      }
    ]
  }
}
```

* **Một phần tử của `items` khi `tab` = `RETURNED`:**

```
{
  "maCTPM": 49,
  "maThe": 1,
  "tenDocGia": "Nguyen Văn A",
  "maCuonSach": "BARCODE-3",
  "tenSach": "Đầu Sách Số 2",
  "ngayMuon": "2026-09-01T10:00:00",
  "ngayTraThucTe": "2026-09-18T14:20:00",
  "trangThaiSachKhiTra": "Bình thường",
  "tienPhat": 15000.0,
  "daThanhToanPhat": true
}
```

  `tienPhat` = tổng `PhieuPhat` của dòng đó (`0.0` nếu không có); `daThanhToanPhat` = `null` nếu không có phiếu phạt. `coTheGiaHan` = `false` khi cuốn đang `RESERVED`, hết lượt gia hạn, đã quá hạn hoặc đầu sách có người chờ, để Client khóa nút **Gia hạn**.
* **Chưa có giao dịch (3a):** `SUCCESS`, `message` = `Bạn chưa có giao dịch mượn sách nào`, `items` = `[]`.
* **Lỗi:** `ERROR` — `Tài khoản chưa có thẻ độc giả` (READER không có thẻ); `BAD_REQUEST` — `Thiếu tham số: maThe | maPhieuMuon | maCuonSach` (LIBRARIAN không gửi tham số nào)

---

## 3.5. Đặt Trước Sách (UC-07, UC-10)

Luồng 4 giai đoạn trong tài liệu phân tích được ánh xạ như sau:

| Giai đoạn | Trạng thái đặt trước | Action |
| --- | --- | --- |
| 1. Tạo phiếu chờ | `WAITING` | `RESERVE_BOOK` (READER) |
| 2. Khóa mục tiêu sách | `WAITING` → `RESERVED` | `GET_WAITING_RESERVATIONS`, `ASSIGN_RESERVATION` (LIBRARIAN) |
| 3. Trả sách & thông báo | `RESERVED` → `NOTIFIED` | Tự động trong `PROCESS_RETURN_BOOK`; Client đọc qua `GET_MY_RESERVATIONS` (UC-10) |
| 4a. Mượn thành công | `NOTIFIED` → `COMPLETED` | Tự động trong `CREATE_BORROW_TICKET` |
| 4b. Hủy / hết hạn giữ | → `CANCELLED` | `CANCEL_RESERVATION`, hoặc Server tự hủy khi quá hạn giữ sách |

#### 3.5.1. Đặt trước sách (`RESERVE_BOOK`)

* **UC:** UC-07 · **Quyền:** READER
* **Mô tả:** Chỉ đặt được khi đầu sách hết cuốn `AVAILABLE`. Server lấy `maThe` từ phiên, tạo `DatTruoc` với `TrangThai` = `WAITING`, `NgayDat` = bây giờ, chưa gán cuốn vật lý.
* **Request:**

```
{
  "action": "RESERVE_BOOK",
  "data": { "maDauSach": 1 }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Đặt trước sách thành công. Hệ thống sẽ thông báo khi sách sẵn sàng",
  "data": {
    "maDatTruoc": 12,
    "maDauSach": 1,
    "tenSach": "Đầu Sách Số 1",
    "ngayDat": "2026-10-15T10:05:00",
    "trangThai": "WAITING",
    "viTriHangCho": 2
  }
}
```

* **Lỗi `ERROR`:**
  * `Thẻ của bạn không đủ điều kiện thực hiện đặt trước` (3a: không có thẻ / thẻ khóa / hết hạn)
  * `Bạn đã đặt trước đầu sách này rồi và đang trong hàng chờ` (3b: đã có lượt `WAITING` / `RESERVED` / `NOTIFIED`)
  * `Đầu sách vẫn còn cuốn sẵn sàng, không cần đặt trước`
  * `Không tìm thấy đầu sách mã: <maDauSach>`

#### 3.5.2. Hủy đặt trước (`CANCEL_RESERVATION`)

* **UC:** UC-07 (giai đoạn 4b) · **Quyền:** READER (chỉ lượt của mình), LIBRARIAN
* **Request:** `{ "action": "CANCEL_RESERVATION", "data": { "maDatTruoc": 12 } }`
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Hủy đặt trước thành công",
  "data": { "maDatTruoc": 12, "trangThai": "CANCELLED" }
}
```

* **Xử lý theo trạng thái trước khi hủy:**

| Trạng thái cũ | Kết quả |
| --- | --- |
| `WAITING` | → `CANCELLED` |
| `RESERVED` (cuốn chưa trả) | → `CANCELLED`; cuốn chuyển cho lượt `WAITING` kế tiếp (→ `RESERVED`), nếu không còn thì về `BORROWED` (mở lại gia hạn) |
| `NOTIFIED` (cuốn đang giữ ở quầy) | → `CANCELLED`; cuốn chuyển cho lượt `WAITING` kế tiếp (→ `NOTIFIED`, ghi `NgayThongBao`), nếu không còn thì về `AVAILABLE` (`soLuongKhaDung` + 1) |
| `COMPLETED` / `CANCELLED` | `ERROR` — `Lượt đặt trước đã kết thúc` |

* **Lỗi:** `FORBIDDEN` — `Bạn không có quyền hủy lượt đặt trước này`; `ERROR` — `Không tìm thấy lượt đặt trước mã: <maDatTruoc>`
* **Hết hạn giữ sách:** lượt `NOTIFIED` quá `NgayThongBao` + `soNgayGiuSachDatTruoc` ngày thì Server tự xử lý như hủy ở trên (quét định kỳ, hoặc kiểm tra khi có `RESERVE_BOOK` / `CREATE_BORROW_TICKET` / `GET_MY_RESERVATIONS`).

#### 3.5.3. Xem các lượt đặt trước của tôi (`GET_MY_RESERVATIONS`)

* **UC:** UC-07, UC-10 · **Quyền:** READER
* **Mô tả:** Mặc định trả các lượt `WAITING` / `RESERVED` / `NOTIFIED`; gửi `trangThai` để lọc một trạng thái cụ thể (kể cả `COMPLETED`, `CANCELLED`). Client gọi khi đăng nhập và định kỳ để hiển thị thông báo (xem 3.5.6).
* **Request:** `{ "action": "GET_MY_RESERVATIONS", "data": null }`
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh sách đặt trước thành công",
  "data": {
    "danhSach": [
      {
        "maDatTruoc": 12,
        "maDauSach": 1,
        "tenSach": "Đầu Sách Số 1",
        "ngayDat": "2026-10-15T10:05:00",
        "trangThai": "WAITING",
        "viTriHangCho": 2,
        "maCuonSach": null,
        "ngayThongBao": null,
        "hanNhanSach": null,
        "thongBao": null
      },
      {
        "maDatTruoc": 9,
        "maDauSach": 4,
        "tenSach": "Lập trình Java Nâng Cao",
        "ngayDat": "2026-10-10T08:00:00",
        "trangThai": "NOTIFIED",
        "viTriHangCho": null,
        "maCuonSach": "BARCODE-21",
        "ngayThongBao": "2026-10-15T09:00:00",
        "hanNhanSach": "2026-10-18",
        "thongBao": "Sách [Lập trình Java Nâng Cao] bạn đặt trước đã có tại quầy. Vui lòng đến nhận trong vòng 3 ngày"
      }
    ]
  }
}
```

  `maCuonSach`, `ngayThongBao`, `hanNhanSach`, `thongBao` chỉ có giá trị khi `trangThai` = `NOTIFIED`.

#### 3.5.4. Thủ thư xem danh sách chờ (`GET_WAITING_RESERVATIONS`)

* **UC:** UC-07 (giai đoạn 2) · **Quyền:** LIBRARIAN
* **Mô tả:** Danh sách các lượt đặt trước, sắp theo `ngayDat` tăng dần. Mặc định `trangThai` = `WAITING`; `maDauSach` tùy chọn. `cuonSachGoiY` là cuốn `BORROWED` có ngày hẹn trả gần nhất của đầu sách đó.
* **Request:**

```
{
  "action": "GET_WAITING_RESERVATIONS",
  "data": { "maDauSach": 1, "trangThai": "WAITING" }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh sách chờ thành công",
  "data": {
    "danhSach": [
      {
        "maDatTruoc": 12,
        "maThe": 1,
        "tenDocGia": "Nguyen Văn A",
        "maDauSach": 1,
        "tenSach": "Đầu Sách Số 1",
        "ngayDat": "2026-10-15T10:05:00",
        "trangThai": "WAITING",
        "cuonSachGoiY": { "maCuonSach": "BARCODE-2", "ngayHenTra": "2026-10-20" }
      }
    ]
  }
}
```

#### 3.5.5. Thủ thư khóa cuốn sách cho lượt chờ (`ASSIGN_RESERVATION`)
bonus: Trường hợp gán thẳng cuốn đã sẵn sàng: ngayHenTraCuaCuon = null, message Đã giữ cuốn sách tại quầy và thông báo cho độc giả
* **UC:** UC-07 (giai đoạn 2) · **Quyền:** LIBRARIAN
* **Mô tả:** `maCuonSach` tùy chọn; bỏ trống thì Server chọn cuốn `BORROWED` có ngày hẹn trả gần nhất.
* **Request:**

```
{
  "action": "ASSIGN_RESERVATION",
  "data": { "maDatTruoc": 12, "maCuonSach": "BARCODE-2" }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Đã khóa cuốn sách cho lượt đặt trước",
  "data": {
    "maDatTruoc": 12,
    "trangThai": "RESERVED",
    "maCuonSach": "BARCODE-2",
    "ngayHenTraCuaCuon": "2026-10-20"
  }
}
```

* **Xử lý:** cuốn `BORROWED` → `RESERVED`; `DatTruoc` `WAITING` → `RESERVED` và ghi `MaCuonSach`. Cuốn ở trạng thái `RESERVED` tự động **vô hiệu hóa gia hạn** (`RENEW_BOOK` trả lỗi 3c).
* **Trường hợp đặc biệt:** nếu đầu sách đã có cuốn `AVAILABLE` (phát sinh sau khi đặt) thì gán thẳng cuốn đó: cuốn `AVAILABLE` → `RESERVED`, `soLuongKhaDung` − 1, `DatTruoc` → `NOTIFIED` và kích hoạt UC-10.
* **Lỗi `ERROR`:** `Lượt đặt trước không ở trạng thái WAITING`; `Phải xử lý lượt đặt trước cũ hơn của đầu sách này trước`; `Đầu sách không có cuốn nào đang được mượn để giữ chỗ`

#### 3.5.6. Thông báo sách đặt trước (UC-10)

Không có action riêng. Luồng chạy phía Server khi `PROCESS_RETURN_BOOK` trả một cuốn `RESERVED`:

1. `DatTruoc` `RESERVED` → `NOTIFIED`, ghi `NgayThongBao`.
2. Tạo nội dung: `Sách [Tên sách] bạn đặt trước đã có tại quầy. Vui lòng đến nhận trong vòng [soNgayGiuSachDatTruoc] ngày`.
3. Gửi Email tới `NguoiDung.Email`. Nếu gửi lỗi (4a): ghi log, **không** làm hỏng giao dịch trả sách.
4. Thông báo trong ứng dụng: Client đọc từ `GET_MY_RESERVATIONS` (các lượt `NOTIFIED`, trường `thongBao`). Vì giao thức là request / response nên Client **chủ động hỏi định kỳ** (khi đăng nhập và mỗi vài phút) thay vì Server push.

---

## 3.6. Phiếu Phạt (UC-14)

#### 3.6.1. Gợi ý số tiền phạt (`GET_FINE_SUGGESTION`)

* **Quyền:** LIBRARIAN
* **Mô tả:** `loaiPhat` = `LATE` (trễ hạn), `DAMAGED` (hỏng) hoặc `LOST` (mất).
* **Request:**

```
{
  "action": "GET_FINE_SUGGESTION",
  "data": { "maCTPM": 50, "loaiPhat": "LATE" }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Gợi ý số tiền phạt",
  "data": {
    "loaiPhat": "LATE",
    "soTienGoiY": 15000.0,
    "giaiThich": "3 ngày trễ × 5000.0 VNĐ/ngày"
  }
}
```

* **Quy tắc gợi ý:** `LATE` = số ngày trễ × `tienPhatNgay`; `DAMAGED` / `LOST` = `DauSach.GiaTien` (giá bìa).
* **Lỗi `ERROR`:** `Chi tiết phiếu mượn không tồn tại`; `Cuốn sách không trễ hạn` (khi `LATE` nhưng chưa trễ)

#### 3.6.2. Lập phiếu phạt (`CREATE_FINE`)
Bonus: Thêm lỗi Cuốn sách không trễ hạn và Dòng mượn này đã có phiếu phạt trễ hạn (cho LATE). Ghi chú LOST khi chưa trả: nếu dòng mượn đang quá hạn thì đồng thời sinh thêm phiếu phạt trễ hạn
* **Quyền:** LIBRARIAN
* **Mô tả:** Lập phiếu phạt riêng lẻ. Trường hợp trả sách có hỏng / mất nên khai báo ngay `viPham` trong `PROCESS_RETURN_BOOK`; action này dùng khi cần lập phiếu bổ sung. `soTienPhat` bỏ trống thì dùng số gợi ý; `daThanhToan` mặc định `false`. Server ghi `MaThuThu` từ phiên, `MaCuonSach` và `MaND` (độc giả bị phạt) suy ra từ `maCTPM`.
* **Request:**

```
{
  "action": "CREATE_FINE",
  "data": {
    "maCTPM": 51,
    "loaiPhat": "DAMAGED",
    "soTienPhat": 30000.0,
    "daThanhToan": false,
    "ghiChu": "Rách bìa"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Lập phiếu phạt thành công!",
  "data": {
    "maPhieuPhat": 8,
    "maCTPM": 51,
    "maCuonSach": "BARCODE-2",
    "maND": 3,
    "lyDo": "Hỏng sách: Rách bìa",
    "soTienPhat": 30000.0,
    "daThanhToan": false,
    "ngayLapPhieu": "2026-10-15T11:00:00"
  }
}
```

* **Lỗi:** `BAD_REQUEST` — `Số tiền phạt phải lớn hơn hoặc bằng 0` (4a); `ERROR` — `Chi tiết phiếu mượn không tồn tại`
* **Ghi chú:** `loaiPhat` = `LOST` mà dòng mượn chưa trả → Server kết thúc dòng đó như khi trả sách mất (xem 3.4.4). Phiếu chưa thanh toán sẽ chặn độc giả mượn tiếp qua UC-13 (`HAS_UNPAID_FINE`).

#### 3.6.3. Danh sách phiếu phạt (`GET_FINES`)

* **Quyền:** LIBRARIAN (xem theo `maThe` tùy chọn), READER (Server ép theo thẻ của mình)
* **Request:**

```
{
  "action": "GET_FINES",
  "data": {
    "maThe": 1,
    "daThanhToan": false,
    "page": 1,
    "pageSize": 10
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh sách phiếu phạt thành công",
  "data": {
    "totalResults": 1,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1,
    "tongChuaThanhToan": 30000.0,
    "items": [
      {
        "maPhieuPhat": 8,
        "maCTPM": 51,
        "maCuonSach": "BARCODE-2",
        "tenSach": "Đầu Sách Số 2",
        "maThe": 1,
        "tenDocGia": "Nguyen Văn A",
        "ngayLapPhieu": "2026-10-15T11:00:00",
        "lyDo": "Hỏng sách: Rách bìa",
        "soTienPhat": 30000.0,
        "daThanhToan": false
      }
    ]
  }
}
```

  `daThanhToan` bỏ trống hoặc `null` = lấy cả hai loại.

#### 3.6.4. Thu tiền phạt (`PAY_FINE`)

* **Quyền:** LIBRARIAN
* **Request:** `{ "action": "PAY_FINE", "data": { "maPhieuPhat": 8 } }`
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Thanh toán phiếu phạt thành công",
  "data": { "maPhieuPhat": 8, "daThanhToan": true }
}
```

* **Lỗi `ERROR`:** `Không tìm thấy phiếu phạt mã: <maPhieuPhat>`; `Phiếu phạt đã được thanh toán trước đó`

---

## 3.7. Quản Lý Độc Giả & Thủ Thư (UC-11, UC-15)
bonus: Thêm kiểm tra đầu vào: Họ tên không hợp lệ (rỗng hoặc > 100 ký tự), Email không hợp lệ (thiếu @ hoặc > 100), Số điện thoại không hợp lệ (8–15 chữ số), Mật khẩu không hợp lệ (tối thiểu 6 ký tự)!. Các trường này cũng được kiểm tra khi sửa (UPDATE_READER, UPDATE_LIBRARIAN); matKhau của UPDATE_LIBRARIAN tối thiểu 6 ký tự
### 3.7.1. Độc giả - Thẻ (UC-11) · Quyền: LIBRARIAN

#### Danh sách độc giả (`GET_READERS`)

* **Request:**

```
{
  "action": "GET_READERS",
  "data": {
    "keyword": "Nguyen",
    "trangThaiThe": "ACTIVE",
    "page": 1,
    "pageSize": 10
  }
}
```

  `keyword` khớp họ tên / email / SĐT; `trangThaiThe` (tùy chọn) = `ACTIVE | LOCKED | EXPIRED`.
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh sách độc giả thành công",
  "data": {
    "totalResults": 1,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1,
    "items": [
      {
        "maND": 3,
        "hoTen": "Nguyen Văn A",
        "email": "client1@gmail.com",
        "soDienThoai": "0912345678",
        "trangThaiTaiKhoan": true,
        "maThe": 1,
        "ngayCap": "2026-01-01",
        "ngayHetHan": "2027-01-01",
        "trangThaiThe": "ACTIVE",
        "dangMuon": 2
      }
    ]
  }
}
```

#### Cấp tài khoản + thẻ mới (`ADD_READER`)

* **Mô tả:** Tạo `NguoiDung` (`MaVaiTro` = 3) và `TheDocGia` trong **một transaction**. `ngayCap` bỏ trống = hôm nay.
* **Request:**

```
{
  "action": "ADD_READER",
  "data": {
    "hoTen": "Tran Thi B",
    "email": "client2@gmail.com",
    "soDienThoai": "0987654321",
    "matKhau": "Pass123",
    "ngayCap": "2026-10-15",
    "ngayHetHan": "2027-10-15"
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Cấp thẻ độc giả thành công!",
  "data": { "maND": 8, "maThe": 5 }
}
```

* **Lỗi:** `ERROR` — `Email hoặc Số điện thoại đã tồn tại trên hệ thống` (6a); `BAD_REQUEST` — `Ngày hết hạn phải sau ngày cấp`; `BAD_REQUEST` — `Sai định dạng ngày (yyyy-MM-dd)`

#### Sửa thông tin độc giả (`UPDATE_READER`)

* **Request:**

```
{
  "action": "UPDATE_READER",
  "data": {
    "maND": 8,
    "hoTen": "Tran Thi Bich",
    "email": "client2@gmail.com",
    "soDienThoai": "0987654321"
  }
}
```

* **Response:** `SUCCESS` — `Cập nhật thông tin độc giả thành công!`. **Lỗi:** `ERROR` — `Email hoặc Số điện thoại đã tồn tại trên hệ thống` (kiểm tra trùng, loại trừ chính độc giả này).

#### Gia hạn thẻ / Khóa / Mở khóa thẻ (`UPDATE_CARD`)

* **Mô tả:** Gửi `ngayHetHan` để gia hạn thẻ, gửi `trangThai` (`ACTIVE` | `LOCKED`) để mở khóa / khóa thẻ; cần ít nhất một trong hai. `lyDo` (lý do khóa) tùy chọn, hiện chỉ ghi log Server vì ERD chưa có cột lưu.
* **Request:**

```
{
  "action": "UPDATE_CARD",
  "data": {
    "maThe": 5,
    "ngayHetHan": "2028-10-15",
    "trangThai": "ACTIVE",
    "lyDo": null
  }
}
```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Cập nhật thẻ độc giả thành công!",
  "data": {
    "maThe": 5,
    "ngayCap": "2026-10-15",
    "ngayHetHan": "2028-10-15",
    "trangThai": "ACTIVE"
  }
}
```

* **Lỗi:** `ERROR` — `Không tìm thấy thẻ độc giả mã: <maThe>`; `BAD_REQUEST` — `Ngày hết hạn phải sau ngày cấp`

### 3.7.2. Thủ thư (UC-15) · Quyền: ADMIN

Các action thao tác trên `NguoiDung` có `MaVaiTro` = 2.

#### Danh sách thủ thư (`GET_LIBRARIANS`)

* **Request:** `{ "action": "GET_LIBRARIANS", "data": { "keyword": "", "page": 1, "pageSize": 10 } }`
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải danh sách thủ thư thành công",
  "data": {
    "totalResults": 1,
    "page": 1,
    "pageSize": 10,
    "totalPages": 1,
    "items": [
      {
        "maND": 2,
        "hoTen": "Le Van C",
        "email": "thuthu1@thuvien.vn",
        "soDienThoai": "0900000002",
        "trangThai": true
      }
    ]
  }
}
```

#### Thêm thủ thư (`ADD_LIBRARIAN`)

* **Request:**

```
{
  "action": "ADD_LIBRARIAN",
  "data": {
    "hoTen": "Le Van C",
    "email": "thuthu2@thuvien.vn",
    "soDienThoai": "0900000003",
    "matKhau": "Init123"
  }
}
```

* **Response:** `SUCCESS` — `Thêm thủ thư thành công!`, `data` = `{ "maND": 9 }`.
* **Lỗi:** `ERROR` — `Email hoặc Số điện thoại đã được sử dụng bởi tài khoản khác` (6a)

#### Sửa thông tin / Khóa tài khoản thủ thư (`UPDATE_LIBRARIAN`)

* **Mô tả:** Chỉ gửi các trường cần đổi (ngoài `maND`). `matKhau` = đặt lại mật khẩu. `trangThai` = `false` để **khóa**, `true` để mở khóa; thủ thư bị khóa không đăng nhập được.
* **Request:**

```
{
  "action": "UPDATE_LIBRARIAN",
  "data": {
    "maND": 9,
    "hoTen": "Le Van Cuong",
    "soDienThoai": "0900000009",
    "trangThai": false
  }
}
```

* **Response:** `SUCCESS` — `Cập nhật thủ thư thành công!`, `data` = `null`.
* **Lỗi `ERROR`:** `Không tìm thấy thủ thư mã: <maND>`; `Email hoặc Số điện thoại đã được sử dụng bởi tài khoản khác`

---

## 3.8. Cấu Hình & Báo Cáo Thống Kê (UC-04, UC-16)

#### 3.8.1. Lấy cấu hình hệ thống (`GET_SYSTEM_CONFIG`)

* **UC:** UC-04 · **Quyền:** Đăng nhập
* **Request:** `{ "action": "GET_SYSTEM_CONFIG", "data": null }`
* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải cấu hình thành công",
  "data": {
    "soSachToiDa": 5,
    "soNgayMuonToiDa": 14,
    "tienPhatNgay": 5000.0,
    "soLanGiaHanToiDa": 2,
    "soNgayGiuSachDatTruoc": 3
  }
}
```

#### 3.8.2. Cập nhật cấu hình hệ thống (`UPDATE_SYSTEM_CONFIG`)

* **UC:** UC-04 · **Quyền:** ADMIN
* **Mô tả:** Gửi đủ 5 tham số; có hiệu lực ngay cho các giao dịch phát sinh sau.
* **Request:**

```
{
  "action": "UPDATE_SYSTEM_CONFIG",
  "data": {
    "soSachToiDa": 6,
    "soNgayMuonToiDa": 14,
    "tienPhatNgay": 5000.0,
    "soLanGiaHanToiDa": 2,
    "soNgayGiuSachDatTruoc": 3
  }
}
```

* **Response:** `SUCCESS` — `Cập nhật quy định thành công`, `data` có cấu trúc như `GET_SYSTEM_CONFIG`.
* **Lỗi (5a):** `BAD_REQUEST` — `Tham số cấu hình phải là số lớn hơn 0` (chữ, số âm, 0, để trống). Server giữ nguyên tham số cũ.

#### 3.8.3. Xem báo cáo thống kê (`GET_REPORTS`)
bonus: limit giới hạn 1–100; groupBy mặc định MONTH; thêm message rỗng của OVERDUE_READERS nếu bạn dùng chỉnh sửa ở phần đầu
* **UC:** UC-16 · **Quyền:** ADMIN, LIBRARIAN
* **Mô tả:** Chọn loại báo cáo bằng `loaiBaoCao`. Bỏ trống `loaiBaoCao` = `TOP_BOOKS` (tương thích bản cũ). `tuNgay` / `denNgay` bỏ trống = toàn bộ thời gian.
* **Request:**

```
{
  "action": "GET_REPORTS",
  "data": {
    "loaiBaoCao": "TOP_BOOKS",
    "tuNgay": "2026-01-01",
    "denNgay": "2026-12-31",
    "limit": 5,
    "groupBy": "MONTH"
  }
}
```

| `loaiBaoCao` | Nội dung | Tham số dùng |
| --- | --- | --- |
| `TOP_BOOKS` | Top đầu sách mượn nhiều nhất | `tuNgay`, `denNgay`, `limit` (mặc định 5) |
| `BORROW_STATS` | Lượt mượn / trả theo thời gian | `tuNgay`, `denNgay`, `groupBy` = `MONTH \| QUARTER \| YEAR` |
| `OVERDUE_READERS` | Độc giả trễ hạn / nợ tiền phạt | Không dùng khoảng ngày (số liệu tại thời điểm hiện tại) |
| `FINE_REVENUE` | Doanh thu tiền phạt | `tuNgay`, `denNgay`, `groupBy` |

* **Response `TOP_BOOKS`:**

```
{
  "status": "SUCCESS",
  "message": "Xuất báo cáo thống kê thành công",
  "data": {
    "loaiBaoCao": "TOP_BOOKS",
    "topSach": [
      { "maDauSach": 1, "tenSach": "Đầu Sách Số 1", "soLuotMuon": 12 },
      { "maDauSach": 2, "tenSach": "Đầu Sách Số 2", "soLuotMuon": 8 }
    ]
  }
}
```

* **Response `BORROW_STATS`** (`data`):

```
{
  "loaiBaoCao": "BORROW_STATS",
  "groupBy": "MONTH",
  "rows": [
    { "ky": "2026-09", "soLuotMuon": 40, "soLuotTra": 35 },
    { "ky": "2026-10", "soLuotMuon": 22, "soLuotTra": 18 }
  ]
}
```

  `ky` có dạng `2026-09` (tháng), `2026-Q3` (quý) hoặc `2026` (năm). `soLuotMuon` đếm theo `ChiTietPhieuMuon` trong kỳ của `NgayMuon`; `soLuotTra` đếm theo kỳ của `NgayTraThucTe`.
* **Response `OVERDUE_READERS`** (`data`):

```
{
  "loaiBaoCao": "OVERDUE_READERS",
  "danhSach": [
    {
      "maThe": 1,
      "hoTen": "Nguyen Văn A",
      "email": "client1@gmail.com",
      "soDienThoai": "0912345678",
      "soSachQuaHan": 2,
      "tienPhatChuaNop": 15000.0
    }
  ]
}
```

* **Response `FINE_REVENUE`** (`data`):

```
{
  "loaiBaoCao": "FINE_REVENUE",
  "tongSoPhieu": 12,
  "tongTienPhat": 180000.0,
  "daThu": 120000.0,
  "chuaThu": 60000.0,
  "theoKy": [
    { "ky": "2026-09", "soPhieu": 5, "tongTienPhat": 75000.0 }
  ]
}
```

* **Không có dữ liệu (3a):** `SUCCESS`, `message` = `Không có dữ liệu thống kê phù hợp với khoảng thời gian này`, các mảng trả về rỗng.
* **Lỗi:** `BAD_REQUEST` — `Khoảng thời gian không hợp lệ` (`tuNgay` > `denNgay`); `BAD_REQUEST` — `Loại báo cáo không được hỗ trợ`
* **Xuất Excel / PDF:** do Client thực hiện từ dữ liệu đã nhận, không có action riêng.

---
