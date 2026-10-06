-- ==========================================================
-- SCRIPT TẠO DATABASE QUẢN LÝ THƯ VIỆN HOÀN CHỈNH
-- ==========================================================

USE QLThuVien;
GO

-- 1. TẠO 13 BẢNG THEO ERD
CREATE TABLE VaiTro (
    MaVaiTro INT PRIMARY KEY,
    TenVaiTro VARCHAR(50)
);

CREATE TABLE NguoiDung (
    MaND INT PRIMARY KEY,
    MaVaiTro INT FOREIGN KEY REFERENCES VaiTro(MaVaiTro),
    HoTen NVARCHAR(100),
    Email VARCHAR(100),
    SoDienThoai VARCHAR(15),
    MatKhau VARCHAR(255),
    TrangThai BIT
);

CREATE TABLE TheDocGia (
    MaThe INT PRIMARY KEY,
    MaND INT FOREIGN KEY REFERENCES NguoiDung(MaND),
    NgayCap DATE,
    NgayHetHan DATE,
    TrangThai NVARCHAR(20)
);

CREATE TABLE NhaXuatBan (
    MaNXB INT PRIMARY KEY,
    TenNXB NVARCHAR(100)
);

CREATE TABLE TheLoai (
    MaTL INT PRIMARY KEY,
    TenTL NVARCHAR(50)
);

CREATE TABLE TacGia (
    MaTG INT PRIMARY KEY,
    TenTacGia NVARCHAR(100)
);

CREATE TABLE DauSach (
    MaDauSach INT PRIMARY KEY,
    TenSach NVARCHAR(200),
    GiaTien DECIMAL(18,2),
    MaTG INT FOREIGN KEY REFERENCES TacGia(MaTG),
    MaNXB INT FOREIGN KEY REFERENCES NhaXuatBan(MaNXB),
    MaTL INT FOREIGN KEY REFERENCES TheLoai(MaTL)
);

CREATE TABLE CuonSach (
    MaCuonSach VARCHAR(50) PRIMARY KEY,
    MaDauSach INT FOREIGN KEY REFERENCES DauSach(MaDauSach),
    TinhTrangVatLy NVARCHAR(100),
    TrangThai NVARCHAR(30)
);

CREATE TABLE DatTruoc (
    MaDatTruoc INT PRIMARY KEY,
    MaThe INT FOREIGN KEY REFERENCES TheDocGia(MaThe),
    MaDauSach INT FOREIGN KEY REFERENCES DauSach(MaDauSach),
    NgayDat DATETIME,
    TrangThai NVARCHAR(30)
);

CREATE TABLE PhieuMuon (
    MaPhieuMuon INT PRIMARY KEY,
    MaThe INT FOREIGN KEY REFERENCES TheDocGia(MaThe),
    MaThuThu INT FOREIGN KEY REFERENCES NguoiDung(MaND),
    NgayMuon DATETIME,
    NgayHenTra DATE,
    TrangThai NVARCHAR(30)
);

CREATE TABLE ChiTietPhieuMuon (
    MaCTPM INT PRIMARY KEY,
    MaPhieuMuon INT FOREIGN KEY REFERENCES PhieuMuon(MaPhieuMuon),
    MaCuonSach VARCHAR(50) FOREIGN KEY REFERENCES CuonSach(MaCuonSach),
    NgayTraThucTe DATETIME,
    TrangThaiSachKhiTra NVARCHAR(50)
);

CREATE TABLE PhieuPhat (
    MaPhieuPhat INT PRIMARY KEY,
    MaCTPM INT FOREIGN KEY REFERENCES ChiTietPhieuMuon(MaCTPM),
    MaThuThu INT FOREIGN KEY REFERENCES NguoiDung(MaND),
    NgayLapPhieu DATETIME,
    LyDo NVARCHAR(200),
    SoTienPhat DECIMAL(18,2),
    TrangThaiThanhToan BIT
);

CREATE TABLE CauHinh (
    MaCauHinh INT PRIMARY KEY,
    SoSachToiDa INT,
    SoNgayMuonToiDa INT,
    TienPhatNgay DECIMAL(18,2)
);
GO

-- 2. BƠM DỮ LIỆU USER VÀ SÁCH
INSERT INTO VaiTro VALUES (1, 'Admin'), (2, 'ThuThu'), (3, 'Client');
GO

DECLARE @i INT = 1;
WHILE @i <= 5
BEGIN
    INSERT INTO NguoiDung VALUES (@i, 1, CONCAT(N'Admin ', @i), CONCAT('admin', @i, '@thuvien.vn'), '090000000' + CAST(@i AS VARCHAR), 'Pass123', 1);
    SET @i = @i + 1;
END

WHILE @i <= 15
BEGIN
    INSERT INTO NguoiDung VALUES (@i, 2, CONCAT(N'Thủ Thư ', @i - 5), CONCAT('thuthu', @i - 5, '@thuvien.vn'), '091000000' + CAST(@i - 5 AS VARCHAR), 'Pass123', 1);
    SET @i = @i + 1;
END

WHILE @i <= 500
BEGIN
    INSERT INTO NguoiDung VALUES (@i, 3, CONCAT(N'Client ', @i - 15), CONCAT('client', @i - 15, '@gmail.com'), '0920000' + CAST(@i AS VARCHAR), 'Pass123', 1);
    INSERT INTO TheDocGia VALUES (@i, @i, GETDATE(), DATEADD(YEAR, 2, GETDATE()), N'Hoạt động');
    SET @i = @i + 1;
END
GO

-- 3. ĐỔI TÊN USER THÀNH TÊN TIẾNG VIỆT THẬT
UPDATE NguoiDung
SET 
    HoTen = CONCAT(
        CHOOSE(ABS(CHECKSUM(NEWID())) % 10 + 1, N'Nguyễn', N'Trần', N'Lê', N'Phạm', N'Hoàng', N'Huỳnh', N'Phan', N'Vũ', N'Võ', N'Đặng'),
        ' ',
        CHOOSE(ABS(CHECKSUM(NEWID())) % 10 + 1, N'Văn', N'Thị', N'Ngọc', N'Hữu', N'Minh', N'Đức', N'Xuân', N'Thanh', N'Bảo', N'Quang'),
        ' ',
        CHOOSE(ABS(CHECKSUM(NEWID())) % 15 + 1, N'Anh', N'Tuấn', N'Linh', N'Trang', N'Thiên', N'Nam', N'Khoa', N'Hùng', N'Phát', N'Trí', N'Thảo', N'Đạt', N'An', N'Bình', N'Châu')
    );
GO

-- 4. BƠM DANH MỤC VÀ 100 ĐẦU SÁCH
INSERT INTO NhaXuatBan VALUES (1, N'NXB Trẻ'), (2, N'NXB Giáo Dục');
INSERT INTO TheLoai VALUES (1, N'Công Nghệ'), (2, N'Văn Học');
INSERT INTO TacGia VALUES (1, N'Nguyễn Nhật Ánh'), (2, N'Bill Gates');
GO

DECLARE @j INT = 1;
WHILE @j <= 100
BEGIN
    INSERT INTO DauSach VALUES (@j, CONCAT(N'Đầu Sách Số ', @j), 50000 + (@j * 1000), (@j % 2) + 1, (@j % 2) + 1, (@j % 2) + 1);
    INSERT INTO CuonSach VALUES (CONCAT('BARCODE-', @j), @j, N'Mới', N'Sẵn sàng');
    SET @j = @j + 1;
END
GO

INSERT INTO CauHinh VALUES (1, 5, 14, 5000.00);
GO