# API Specification - Quản Lý Thư Viện (Socket &amp; JSON-P)

Tài liệu tả chi tiết định dạng các thông điệp JSON truyền nhận giữa **GUI Client** và **Java Socket Server** (sử dụng JPA Repository với CSDL `QLThuVien`).

---

## 1\. Khung Cấu Trúc Tổng Quát (JSON Envelope Protocol)

* **Client gửi Request:**

```
{
  "action": "TÊN_HÀNH_ĐỘNG",
  "data": { /* Dữ liệu tham số gửi kèm */ }
}

```

* **Server trả Response:**

```
{
  "status": "SUCCESS | ERROR | BAD_REQUEST | UNAUTHORIZED",
  "message": "Thông báo hiển thị lên giao diện Client (GUI)",
  "data": { /* Dữ liệu kết quả trả về hoặc null */ }
}

```

---

## 2\. Chi Tiết API Request - Response Theo Từng Nghiệp Vụ

### 2.1\. Xác Thực &amp; Tài Khoản (`ActionType`)

#### **2.1.1\. Đăng Nhập (** **LOGIN** **)**

* **Mô tả:** Đăng nhập hệ thống bằng Email hoặc Số điện thoại (dành cho Admin, Thủ thư, Độc giả).
* **Request (Client):**

```
{
  "action": "LOGIN",
  "data": {
    "username": "admin1@thuvien.vn",
    "password": "Pass123"
  }
}

```

* **Response Thành công (Server - Admin/Thủ thư):**

```
{
  "status": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "maND": 1,
    "fullName": "Phan Ngoc",
    "email": "admin1@thuvien.vn",
    "phone": "0900000001",
    "role": "Admin",
    "maThe": -1,
    "trangThaiThe": "NONE"
  }
}

```

* **Response Thành công (Server - Độc giả có thẻ):**

```
{
  "status": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "maND": 3,
    "fullName": "Nguyen Văn A",
    "email": "client1@gmail.com",
    "phone": "0912345678",
    "role": "Client",
    "maThe": 1,
    "trangThaiThe": "ACTIVE"
  }
}

```

* **Response Thất bại (Mật khẩu sai hoặc tài khoản bị khóa):**

```
{
  "status": "UNAUTHORIZED",
  "message": "Tài khoản hoặc mật khẩu không chính xác!",
  "data": null
}

```

#### **2.1.2\. Đổi Mật Khẩu (** **CHANGE\_PASSWORD** **)**

* **Mô tả:** Thay đổi mật khẩu người dùng đang đăng nhập.
* **Request:**

```
{
  "action": "CHANGE_PASSWORD",
  "data": {
    "username": "admin1@thuvien.vn",
    "oldPassword": "Pass123",
    "newPassword": "NewPass456"
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

---

### 2.2\. Tra Cứu &amp; Quản Lý Kho Sách

#### **2.2.1\. Tìm Kiếm Đầu Sách (** **SEARCH\_BOOKS** **)**

* **Mô tả:** Tìm kiếm đầu sách theo từ khóa (Tên sách/Tác giả) hoặc Thể loại, hỗ trợ phân trang.
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
    "books": [
      {
        "maDauSach": 1,
        "tenSach": "Đầu Sách Số 1",
        "giaTien": 51000.0,
        "tacGia": "Nguyễn Văn A",
        "theLoai": "Văn Học",
        "nhaXuatBan": "NXB Kim Đồng"
      },
      {
        "maDauSach": 2,
        "tenSach": "Đầu Sách Số 2",
        "giaTien": 75000.0,
        "tacGia": "Trần Văn B",
        "theLoai": "Công Nghệ",
        "nhaXuatBan": "NXB Trẻ"
      }
    ]
  }
}

```

#### **2.2.2\. Xem Chi Tiết Đầu Sách (** **GET\_BOOK\_DETAIL** **)**

* **Mô tả:** Lấy thông tin đầu sách và danh sách tất cả cuốn sách vật lý (mã vạch/tình trạng) thuộc đầu sách đó.
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
    "cuonSachList": [
      {
        "maCuonSach": "BARCODE-1",
        "tinhTrangValLy": "Mới",
        "trangThai": "Sẵn sàng"
      },
      {
        "maCuonSach": "BARCODE-2",
        "tinhTrangValLy": "Tốt",
        "trangThai": "BORROWED"
      }
    ]
  }
}

```

#### **2.2.3\. Thêm Đầu Sách Mới (** **ADD\_BOOK\_TITLE** **)**

* **Mô tả:** Thủ thư/Admin nhập đầu sách mới vào hệ thống.
* **Request:**

```
{
  "action": "ADD_BOOK_TITLE",
  "data": {
    "tenSach": "Lập trình Java Nâng Cao",
    "giaTien": 150000.0
  }
}

```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Thêm đầu sách mới thành công!",
  "data": null
}

```

---

### 2.3\. Nghiệp Vụ Mượn &amp; Trả Sách

#### **2.3.1\. Lập Phiếu Mượn Sách (** **CREATE\_BORROW\_TICKET** **)**

* **Mô tả:** Thủ thư lập phiếu mượn sách cho Độc giả. Sử dụng Khóa bi quan (`PESSIMISTIC_WRITE`) dưới CSDL để chống tranh chấp khi mượn đồng thời.
* **Request:**

```
{
  "action": "CREATE_BORROW_TICKET",
  "data": {
    "maThe": 1,
    "maThuThu": 2,
    "danhSachMaCuonSach": [
      "BARCODE-1",
      "BARCODE-2"
    ]
  }
}

```

* **Response Thành công:**

```
{
  "status": "SUCCESS",
  "message": "Lập phiếu mượn thành công!",
  "data": {
    "maPhieuMuon": 101,
    "maThe": 1
  }
}

```

* **Response Thất bại (Vượt quá hạn mức mượn hoặc sách không sẵn sàng):**

```
{
  "status": "ERROR",
  "message": "Vượt quá số lượng sách mượn tối đa (5 cuốn)! Hiện đang mượn: 4",
  "data": null
}

```

#### **2.3.2\. Xử Lý Trả Sách (** **PROCESS\_RETURN\_BOOK** **)**

* **Mô tả:** Thủ thư nhận lại sách trả, cập nhật tình trạng vật lý và tự động tính tiền phạt trễ hạn (nếu quá hạn trả).
* **Request:**

```
{
  "action": "PROCESS_RETURN_BOOK",
  "data": {
    "maCTPM": 50,
    "maThuThu": 2,
    "tinhTrangKhiTra": "Bình thường"
  }
}

```

* **Response (Trả đúng hạn):**

```
{
  "status": "SUCCESS",
  "message": "Trả sách thành công!",
  "data": {
    "tienPhat": 0.0,
    "coPhat": false
  }
}

```

* **Response (Trả trễ hạn - Tự động sinh Phiếu Phạt):**

```
{
  "status": "SUCCESS",
  "message": "Trả sách thành công. Phát hiện trễ hạn, tiền phạt: 15000.0 VNĐ",
  "data": {
    "tienPhat": 15000.0,
    "coPhat": true
  }
}

```

---

### 2.4\. Cấu Hình &amp; Báo Cáo Thống Kê

#### **2.4.1\. Lấy Cấu Hình Hệ Thống (** **GET\_SYSTEM\_CONFIG** **)**

* **Mô tả:** Tải các tham số quy định hệ thống (số sách mượn tối đa, số ngày mượn tối đa, tiền phạt/ngày).
* **Request:**

```
{
  "action": "GET_SYSTEM_CONFIG",
  "data": null
}

```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Tải cấu hình thành công",
  "data": {
    "soSachToiDa": 5,
    "soNgayMuonToiDa": 14,
    "tienPhatNgay": 5000.0
  }
}

```

#### **2.4.2\. Xem Báo Cáo Thống Kê (** **GET\_REPORTS** **)**

* **Mô tả:** Lấy danh sách Top các đầu sách được mượn nhiều nhất.
* **Request:**

```
{
  "action": "GET_REPORTS",
  "data": {
    "limit": 5
  }
}

```

* **Response:**

```
{
  "status": "SUCCESS",
  "message": "Xuất báo cáo thống kê thành công",
  "data": {
    "topSach": [
      {
        "tenSach": "Đầu Sách Số 1",
        "soLuotMuon": 12
      },
      {
        "tenSach": "Đầu Sách Số 2",
        "soLuotMuon": 8
      }
    ]
  }
}
```
