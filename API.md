### 1\. Khung Cấu trúc Tổng quát (JSON Envelope Protocol)

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
  "data": { /* Dữ liệu kết quả trả về */ }
}

```
### 2\. Chi tiết JSON Request - Response cho từng Use Case

#### **2.1\. Lập Phiếu mượn sách (** **UC-01** **)**[11][12]

* **Action:** **CREATE\_BORROW\_TICKET**
  * **Request (Thủ thư quét mã thẻ và quét mã vạch các cuốn sách):**[11][12]

```
{
  "action": "CREATE_BORROW_TICKET",
  "data": {
    "readerUsername": "docgia01",
    "librarianUsername": "thuthu01",
    "bookItemIds": [
      "BS001-01",
      "BS002-01"
    ]
  }
}

```

* **Response thành công:**[12]

```
{
  "status": "SUCCESS",
  "message": "Lập phiếu mượn thành công!",
  "data": {
    "borrowId": "PM2026092701",
    "readerUsername": "docgia01",
    "librarianUsername": "thuthu01",
    "borrowDate": "2026-09-27T10:15:00",
    "status": "BORROWING",
    "details": [
      {
        "bookItemId": "BS001-01",
        "dueDate": "2026-10-11",
        "renewCount": 0,
        "status": "BORROWING"
      },
      {
        "bookItemId": "BS002-01",
        "dueDate": "2026-10-11",
        "renewCount": 0,
        "status": "BORROWING"
      }
    ]
  }
}

```

* **Response luồng ngoại lệ (Ví dụ: Thẻ bị khóa do nợ phạt hoặc sách đã có người đặt trước):**[12][15]

```
{
  "status": "ERROR",
  "message": "Không thể lập phiếu mượn: Thẻ độc giả hiện đang bị KHÓA do nợ phạt chưa thanh toán hoặc sách BS001-01 đã được người khác đặt giữ chỗ.",
  "data": null
}

```

#### **2.2\. Xử lý Trả sách &amp; Lập phiếu phạt (** **UC-02** **/** **UC-14** **)**[15]

* **Action:** **PROCESS\_RETURN\_BOOK**
  * **Request (Thủ thư tích chọn sách trả):**[16]

```
{
  "action": "PROCESS_RETURN_BOOK",
  "data": {
    "borrowId": "PM2026092701",
    "librarianUsername": "thuthu01",
    "returnedItems": [
      {
        "bookItemId": "BS001-01",
        "condition": "NORMAL"
      },
      {
        "bookItemId": "BS002-01",
        "condition": "OVERDUE"
      }
    ]
  }
}

```

* **Response (Hệ thống tính tiền phạt trễ hạn tự động):**[16]

```
{
  "status": "SUCCESS",
  "message": "Xử lý trả sách hoàn tất. Phát hiện 1 cuốn trễ hạn 3 ngày.",
  "data": {
    "borrowId": "PM2026092701",
    "returnDate": "2026-10-14",
    "totalFineAmount": 15000.0,
    "returnedDetails": [
      {
        "bookItemId": "BS001-01",
        "status": "RETURNED",
        "overdueDays": 0,
        "fineAmount": 0.0
      },
      {
        "bookItemId": "BS002-01",
        "status": "RETURNED",
        "overdueDays": 3,
        "fineAmount": 15000.0
      }
    ]
  }
}

```

* **Action:** **CREATE\_FINE\_TICKET** **(Lập phiếu phạt hư hỏng / mất sách):**[17][18]
  * **Request:**[18]

```
{
  "action": "CREATE_FINE_TICKET",
  "data": {
    "borrowId": "PM2026092701",
    "bookItemId": "BS001-01",
    "username": "docgia01",
    "fineReason": "LOST",
    "fineAmount": 150000.0,
    "paidStatus": true
  }
}

```

* **Response:**[18][19]

```
{
  "status": "SUCCESS",
  "message": "Lập phiếu phạt đền bù thành công",
  "data": {
    "fineId": 102,
    "borrowId": "PM2026092701",
    "bookItemId": "BS001-01",
    "username": "docgia01",
    "fineReason": "LOST",
    "fineAmount": 150000.0,
    "paidStatus": true,
    "createdDate": "2026-09-27T11:00:00"
  }
}

```

---

#### **2.3\. Tra cứu &amp; Xem tình trạng sách (** **UC-03** **)**[7][8]

* **Action:** **SEARCH\_BOOKS**
  * **Request:**[8]

```
{
  "action": "SEARCH_BOOKS",
  "data": {
    "keyword": "Java",
    "category": "Công nghệ",
    "page": 1,
    "pageSize": 10
  }
}

```

* **Response:**[9][10]

```
{
  "status": "SUCCESS",
  "message": "Tìm thấy 2 kết quả phù hợp",
  "data": {
    "totalResults": 2,
    "books": [
      {
        "isbn": "978-604-01",
        "title": "Lập trình Java Nâng Cao",
        "author": "Nguyễn Văn B",
        "publisher": "NXB Giáo Dục",
        "publishYear": 2023,
        "category": "Công nghệ",
        "price": 150000.0,
        "location": "Kệ A1 - Tầng 2",
        "totalQuantity": 5,
        "availableQuantity": 3
      },
      {
        "isbn": "978-604-02",
        "title": "Java Persistence API (JPA)",
        "author": "Trần Văn C",
        "publisher": "NXB Thống Kê",
        "publishYear": 2024,
        "category": "Công nghệ",
        "price": 180000.0,
        "location": "Kệ A2 - Tầng 2",
        "totalQuantity": 2,
        "availableQuantity": 0
      }
    ]
  }
}

```

* **Action:** **GET\_BOOK\_DETAIL**
  * **Request:**[8]

```
{
  "action": "GET_BOOK_DETAIL",
  "data": {
    "isbn": "978-604-01"
  }
}

```

* **Response:** (Xem chi tiết từng cuốn sách vật lý `BookItem`)[9][10]

```
{
  "status": "SUCCESS",
  "message": "Lấy thông tin chi tiết thành công",
  "data": {
    "titleInfo": {
      "isbn": "978-604-01",
      "title": "Lập trình Java Nâng Cao",
      "author": "Nguyễn Văn B",
      "publisher": "NXB Giáo Dục",
      "category": "Công nghệ",
      "price": 150000.0,
      "location": "Kệ A1 - Tầng 2",
      "totalQuantity": 5,
      "availableQuantity": 3
    },
    "items": [
      {
        "bookItemId": "BS001-01",
        "status": "AVAILABLE"
      },
      {
        "bookItemId": "BS001-02",
        "status": "BORROWED"
      },
      {
        "bookItemId": "BS001-03",
        "status": "AVAILABLE"
      }
    ]
  }
}

```
---

#### **2.4\. Cấu hình quy định hệ thống (** **UC-04** **)**[21][24]

* **Action:** **GET\_SYSTEM\_CONFIG**
  * **Request:**

```
{
  "action": "GET_SYSTEM_CONFIG",
  "data": null
}

```

* **Response:**[23][25]

```
{
  "status": "SUCCESS",
  "message": "Tải cấu hình thành công",
  "data": {
    "configId": 1,
    "maxBorrowDays": 14,
    "maxBooksPerReader": 5,
    "finePerDay": 5000.0,
    "maxRenewTimes": 2,
    "holdKeepDays": 3
  }
}

```

* **Action:** **UPDATE\_SYSTEM\_CONFIG**
  * **Request (Admin chỉnh sửa tham số):**[24]

```
{
  "action": "UPDATE_SYSTEM_CONFIG",
  "data": {
    "configId": 1,
    "maxBorrowDays": 21,
    "maxBooksPerReader": 5,
    "finePerDay": 10000.0,
    "maxRenewTimes": 2,
    "holdKeepDays": 3
  }
}

```

* **Response:**[2]

```
{
  "status": "SUCCESS",
  "message": "Cập nhật quy định hệ thống thành công!",
  "data": null
}

```
---

#### **2.5\. Đăng nhập &amp; Đổi mật khẩu (** **UC-05** **)**[5]

* **Action:** **LOGIN**
  * **Request (Client gửi):**

```
{
  "action": "LOGIN",
  "data": {
    "username": "docgia01",
    "password": "mypassword123"
  }
}

```

* **Response (Server trả về):**[2][6]

```
{
  "status": "SUCCESS",
  "message": "Đăng nhập thành công",
  "data": {
    "username": "docgia01",
    "fullName": "Nguyễn Văn A",
    "email": "nguyenvana@gmail.com",
    "phone": "0901234567",
    "role": "READER",
    "status": "ACTIVE",
    "createdDate": "2024-01-15T08:00:00",
    "expiryDate": "2026-12-31T23:59:59"
  }
}

```

* **Action:** **CHANGE\_PASSWORD**
  * **Request:**

```
{
  "action": "CHANGE_PASSWORD",
  "data": {
    "username": "docgia01",
    "oldPassword": "mypassword123",
    "newPassword": "newpassword456"
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
#### **2.6\. Đặt trước &amp; Gia hạn sách (** **UC-07** **/** **UC-09** **)**[1][20]

* **Action:** **RESERVE\_BOOK**
  * **Request (Độc giả đặt trước khi sách hết trong kho):**[21][22]

```
{
  "action": "RESERVE_BOOK",
  "data": {
    "username": "docgia01",
    "isbn": "978-604-02"
  }
}

```

* **Response:**[19][22]

```
{
  "status": "SUCCESS",
  "message": "Đặt giữ chỗ sách thành công. Bạn đang ở vị trí chờ số 1.",
  "data": {
    "reservationId": 501,
    "username": "docgia01",
    "isbn": "978-604-02",
    "reservedDate": "2026-09-27T11:30:00",
    "status": "WAITING"
  }
}

```

* **Action:** **RENEW\_BOOK**
  * **Request (Độc giả yêu cầu gia hạn sách đang mượn):**[20]

```
{
  "action": "RENEW_BOOK",
  "data": {
    "borrowId": "PM2026092701",
    "bookItemId": "BS001-01"
  }
}

```

* **Response:**[14][23]

```
{
  "status": "SUCCESS",
  "message": "Gia hạn sách thành công thêm 14 ngày",
  "data": {
    "bookItemId": "BS001-01",
    "newDueDate": "2026-10-25",
    "renewCount": 1
  }
}

```

---

#### **2.7\. Thống kê &amp; Báo cáo (** **UC-16** **)**[1][2]

* **Action:** **GET\_REPORTS**
  * **Request:**[2]

```
{
  "action": "GET_REPORTS",
  "data": {
    "reportType": "TOP_BORROWED_BOOKS",
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
    "reportType": "TOP_BORROWED_BOOKS",
    "reportData": [
      {
        "isbn": "978-604-01",
        "title": "Lập trình Java Nâng Cao",
        "borrowCount": 42
      },
      {
        "isbn": "978-604-02",
        "title": "Java Persistence API (JPA)",
        "borrowCount": 35
      }
    ]
  }
}
```
