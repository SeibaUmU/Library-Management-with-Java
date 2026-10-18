package com.library.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.library.common.BadRequestException;
import com.library.common.NghiepVuException;
import com.library.common.TrangThai;
import com.library.entities.ChiTietPhieuMuon;
import com.library.entities.CuonSach;
import com.library.entities.DatTruoc;
import com.library.entities.DauSach;
import com.library.entities.NguoiDung;
import com.library.entities.PhieuMuon;
import com.library.entities.PhieuPhat;
import com.library.entities.TheDocGia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.LockModeType;
import jakarta.persistence.TypedQuery;

public class PhieuMuonRepository {

    // 1. Đếm số cuốn sách độc giả đang mượn chưa trả
    public long countActiveBorrowingByMaThe(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            String jpql = "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct " +
                    "WHERE ct.phieuMuon.theDocGia.maThe = :maThe AND ct.ngayTraThucTe IS NULL";

            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("maThe", maThe);

            return query.getSingleResult();
        }
    }

    // 4. Đếm số cuốn đang mượn QUÁ HẠN của độc giả (UC-13)
    public long countOverdueByMaThe(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            return em.createQuery(
                    "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct "
                            + "WHERE ct.phieuMuon.theDocGia.maThe = :maThe "
                            + "AND ct.ngayTraThucTe IS NULL "
                            + "AND ct.ngayHenTra < :today",
                    Long.class)
                    .setParameter("maThe", maThe)
                    .setParameter("today", LocalDate.now())
                    .getSingleResult();
        }
    }

    // 5. Đếm đang mượn cho nhiều thẻ cùng lúc (GET_READERS - tránh N+1)
    public Map<Integer, Long> countActiveBorrowingByMaTheList(
            Collection<Integer> maTheList) {
        Map<Integer, Long> ketQua = new HashMap<>();

        if (maTheList == null || maTheList.isEmpty()) {
            return ketQua;
        }

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<Object[]> rows = em.createQuery(
                    "SELECT ct.phieuMuon.theDocGia.maThe, COUNT(ct) "
                            + "FROM ChiTietPhieuMuon ct "
                            + "WHERE ct.ngayTraThucTe IS NULL "
                            + "AND ct.phieuMuon.theDocGia.maThe IN (:ids) "
                            + "GROUP BY ct.phieuMuon.theDocGia.maThe",
                    Object[].class)
                    .setParameter("ids", maTheList)
                    .getResultList();

            for (Object[] r : rows) {
                ketQua.put(
                        ((Number) r[0]).intValue(),
                        ((Number) r[1]).longValue());
            }
        }

        return ketQua;
    }

    // 6. Tra cứu sách đang mượn / lịch sử trả (GET_BORROW_HISTORY - UC-08, UC-02)
    public static class LichSuFilter {
        public boolean dangMuon; // true: chưa trả, false: đã trả
        public Integer maThe;
        public Integer maPhieuMuon;
        public String maCuonSach;
        public String keyword; // tên sách
        public LocalDate tuNgay; // lọc theo ngày mượn
        public LocalDate denNgay;
    }

    private static String buildLichSuWhere(
            LichSuFilter f,
            Map<String, Object> p) {
        StringBuilder w = new StringBuilder(
                f.dangMuon
                        ? " WHERE ct.ngayTraThucTe IS NULL"
                        : " WHERE ct.ngayTraThucTe IS NOT NULL");

        if (f.maThe != null) {
            w.append(" AND t.maThe = :maThe");
            p.put("maThe", f.maThe);
        }
        if (f.maPhieuMuon != null) {
            w.append(" AND pm.maPhieuMuon = :maPM");
            p.put("maPM", f.maPhieuMuon);
        }
        if (f.maCuonSach != null && !f.maCuonSach.trim().isEmpty()) {
            w.append(" AND cs.maCuonSach = :maCS");
            p.put("maCS", f.maCuonSach.trim());
        }
        if (f.keyword != null && !f.keyword.trim().isEmpty()) {
            w.append(" AND LOWER(ds.tenSach) LIKE :kw");
            p.put("kw", "%" + f.keyword.trim().toLowerCase() + "%");
        }
        if (f.tuNgay != null) {
            w.append(" AND pm.ngayMuon >= :tu");
            p.put("tu", f.tuNgay.atStartOfDay());
        }
        if (f.denNgay != null) {
            w.append(" AND pm.ngayMuon < :den");
            p.put("den", f.denNgay.plusDays(1).atStartOfDay());
        }

        return w.toString();
    }

    private static void bind(TypedQuery<?> q, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
    }

    public List<ChiTietPhieuMuon> searchLichSu(
            LichSuFilter f,
            int page,
            int pageSize) {
        Map<String, Object> params = new HashMap<>();
        String where = buildLichSuWhere(f, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            String jpql = "SELECT ct FROM ChiTietPhieuMuon ct "
                    + "JOIN FETCH ct.phieuMuon pm "
                    + "JOIN FETCH pm.theDocGia t "
                    + "JOIN FETCH t.nguoiDung "
                    + "JOIN FETCH ct.cuonSach cs "
                    + "JOIN FETCH cs.dauSach ds"
                    + where
                    + (f.dangMuon
                            ? " ORDER BY ct.ngayHenTra ASC, ct.maCTPM ASC"
                            : " ORDER BY ct.ngayTraThucTe DESC, ct.maCTPM DESC");

            TypedQuery<ChiTietPhieuMuon> query = em.createQuery(jpql, ChiTietPhieuMuon.class);

            bind(query, params);

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public long countLichSu(LichSuFilter f) {
        Map<String, Object> params = new HashMap<>();
        String where = buildLichSuWhere(f, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(ct) FROM ChiTietPhieuMuon ct "
                            + "JOIN ct.phieuMuon pm "
                            + "JOIN pm.theDocGia t "
                            + "JOIN ct.cuonSach cs "
                            + "JOIN cs.dauSach ds"
                            + where,
                    Long.class);

            bind(query, params);

            return query.getSingleResult();
        }
    }
    // =====================================================================
    // HÀM DÙNG CHUNG
    // =====================================================================

    public static final String LOI_KHONG_SAN_SANG = "Cuốn sách không sẵn sàng để mượn";
    public static final String LOI_DA_DAT_TRUOC = "Sách đã được người khác đặt trước, không thể lập phiếu mượn";

    private static String cat(String s, int max) {
        if (s == null) {
            return null;
        }
        return s.length() > max ? s.substring(0, max) : s;
    }

    // Cộng/trừ bộ đếm của đầu sách (UPDATE nguyên tử ở mức DB, an toàn khi nhiều
    // client)
    static void capNhatSoLuong(
            EntityManager em,
            Integer maDauSach,
            int dKhaDung,
            int dTong) {
        em.createQuery(
                "UPDATE DauSach d SET "
                        + "d.soLuongKhaDung = COALESCE(d.soLuongKhaDung, 0) + :dk, "
                        + "d.tongSoLuong = COALESCE(d.tongSoLuong, 0) + :dt "
                        + "WHERE d.maDauSach = :id")
                .setParameter("dk", dKhaDung)
                .setParameter("dt", dTong)
                .setParameter("id", maDauSach)
                .executeUpdate();
    }

    public ChiTietPhieuMuon findChiTietFetch(Integer maCTPM) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<ChiTietPhieuMuon> ds = em.createQuery(
                    "SELECT ct FROM ChiTietPhieuMuon ct "
                            + "JOIN FETCH ct.phieuMuon pm "
                            + "JOIN FETCH pm.theDocGia t "
                            + "JOIN FETCH t.nguoiDung "
                            + "JOIN FETCH ct.cuonSach cs "
                            + "JOIN FETCH cs.dauSach "
                            + "WHERE ct.maCTPM = :id",
                    ChiTietPhieuMuon.class)
                    .setParameter("id", maCTPM)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
        }
    }

    // =====================================================================
    // UC-01: KIỂM TRA & LẬP PHIẾU MƯỢN
    // =====================================================================

    // Trả về null nếu cuốn sách được phép mượn bởi độc giả này, ngược lại là thông
    // báo lỗi.
    // - Cuốn AVAILABLE: được mượn, trừ khi lượt WAITING của người khác >= số cuốn
    // AVAILABLE (5b)
    // - Cuốn RESERVED: chỉ người đang NOTIFIED cho đúng cuốn đó mới được mượn
    private static String lyDoKhongChoMuon(
            EntityManager em,
            CuonSach cs,
            Integer maThe) {
        if (cs == null) {
            return LOI_KHONG_SAN_SANG;
        }

        String tt = cs.getTrangThai();

        if (TrangThai.CUON_RESERVED.equals(tt)) {
            Long n = em.createQuery(
                    "SELECT COUNT(d) FROM DatTruoc d "
                            + "WHERE d.cuonSach.maCuonSach = :ma "
                            + "AND d.theDocGia.maThe = :maThe "
                            + "AND d.trangThai = :tt",
                    Long.class)
                    .setParameter("ma", cs.getMaCuonSach())
                    .setParameter("maThe", maThe)
                    .setParameter("tt", TrangThai.DATTRUOC_NOTIFIED)
                    .getSingleResult();

            return n > 0 ? null : LOI_DA_DAT_TRUOC;
        }

        if (!TrangThai.CUON_AVAILABLE.equals(tt)) {
            return LOI_KHONG_SAN_SANG;
        }

        Integer maDauSach = cs.getDauSach().getMaDauSach();

        Long waitingNguoiKhac = em.createQuery(
                "SELECT COUNT(d) FROM DatTruoc d "
                        + "WHERE d.dauSach.maDauSach = :maDS "
                        + "AND d.theDocGia.maThe <> :maThe "
                        + "AND d.trangThai = :tt",
                Long.class)
                .setParameter("maDS", maDauSach)
                .setParameter("maThe", maThe)
                .setParameter("tt", TrangThai.DATTRUOC_WAITING)
                .getSingleResult();

        Long conSan = em.createQuery(
                "SELECT COUNT(c) FROM CuonSach c "
                        + "WHERE c.dauSach.maDauSach = :maDS "
                        + "AND c.trangThai = :tt",
                Long.class)
                .setParameter("maDS", maDauSach)
                .setParameter("tt", TrangThai.CUON_AVAILABLE)
                .getSingleResult();

        return waitingNguoiKhac >= conSan ? LOI_DA_DAT_TRUOC : null;
    }

    // Dùng cho GET_COPY_BY_BARCODE (kiểm tra trước, không khóa dòng)
    public String kiemTraChoMuon(String maCuonSach, Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {
            CuonSach cs = em.find(CuonSach.class, maCuonSach);
            return lyDoKhongChoMuon(em, cs, maThe);
        }
    }

    // Khi mượn: đóng lượt đặt trước tương ứng của chính độc giả này
    // - mượn cuốn RESERVED: lượt NOTIFIED của cuốn đó
    // - mượn cuốn AVAILABLE: lượt WAITING cũ nhất của đầu sách (nếu có)
    private static void hoanTatDatTruoc(
            EntityManager em,
            Integer maThe,
            Integer maDauSach,
            String maCuonSachGiuCho) {
        TypedQuery<DatTruoc> q;

        if (maCuonSachGiuCho != null) {
            q = em.createQuery(
                    "SELECT d FROM DatTruoc d "
                            + "WHERE d.cuonSach.maCuonSach = :ma "
                            + "AND d.theDocGia.maThe = :maThe "
                            + "AND d.trangThai = :tt",
                    DatTruoc.class)
                    .setParameter("ma", maCuonSachGiuCho)
                    .setParameter("maThe", maThe)
                    .setParameter("tt", TrangThai.DATTRUOC_NOTIFIED);
        } else {
            q = em.createQuery(
                    "SELECT d FROM DatTruoc d "
                            + "WHERE d.dauSach.maDauSach = :maDS "
                            + "AND d.theDocGia.maThe = :maThe "
                            + "AND d.trangThai = :tt "
                            + "ORDER BY d.ngayDat ASC",
                    DatTruoc.class)
                    .setParameter("maDS", maDauSach)
                    .setParameter("maThe", maThe)
                    .setParameter("tt", TrangThai.DATTRUOC_WAITING);
        }

        q.setMaxResults(1);

        for (DatTruoc d : q.getResultList()) {
            d.setTrangThai(TrangThai.DATTRUOC_COMPLETED);
        }
    }

    public static class DongMuon {
        public Integer maCTPM;
        public String maCuonSach;
        public String tenSach;
        public LocalDate ngayHenTra;
    }

    public static class KetQuaPhieuMuon {
        public Integer maPhieuMuon;
        public LocalDateTime ngayMuon;
        public List<DongMuon> chiTiet = new ArrayList<>();
    }

    // Lập phiếu mượn trong 1 transaction. Vi phạm quy tắc -> NghiepVuException
    // (không lưu gì).
    // Việc kiểm tra thẻ (UC-13) và hạn mức do router làm trước khi gọi hàm này.
    public KetQuaPhieuMuon taoPhieuMuon(
            Integer maThe,
            Integer maThuThu,
            List<String> maCuonSachList,
            int soNgayMuonToiDa) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TheDocGia the = em.find(TheDocGia.class, maThe);

            if (the == null) {
                throw new NghiepVuException(
                        "Không tìm thấy thẻ độc giả mã: " + maThe);
            }

            NguoiDung thuThu = em.find(NguoiDung.class, maThuThu);

            LocalDateTime now = LocalDateTime.now();
            LocalDate ngayHenTra = now.toLocalDate().plusDays(soNgayMuonToiDa);

            PhieuMuon pm = new PhieuMuon();
            pm.setTheDocGia(the);
            pm.setThuThu(thuThu);
            pm.setNgayMuon(now);
            pm.setNgayHenTra(ngayHenTra);
            pm.setTrangThai(TrangThai.PHIEUMUON_BORROWING);
            em.persist(pm);

            KetQuaPhieuMuon kq = new KetQuaPhieuMuon();
            kq.maPhieuMuon = pm.getMaPhieuMuon();
            kq.ngayMuon = now;

            // Khóa theo thứ tự mã vạch để hai phiếu cùng lúc không bị deadlock
            List<String> sapXep = new ArrayList<>(maCuonSachList);
            Collections.sort(sapXep);

            for (String ma : sapXep) {

                CuonSach cs = em.find(
                        CuonSach.class,
                        ma,
                        LockModeType.PESSIMISTIC_WRITE);

                String loi = lyDoKhongChoMuon(em, cs, maThe);

                if (loi != null) {
                    throw new NghiepVuException(
                            LOI_KHONG_SAN_SANG.equals(loi) ? loi + ": " + ma : loi);
                }

                DauSach dauSach = cs.getDauSach();
                boolean laCuonGiuCho = TrangThai.CUON_RESERVED.equals(cs.getTrangThai());

                hoanTatDatTruoc(
                        em,
                        maThe,
                        dauSach.getMaDauSach(),
                        laCuonGiuCho ? ma : null);

                cs.setTrangThai(TrangThai.CUON_BORROWED);

                // Cuốn RESERVED trước đó không nằm trong SoLuongKhaDung nên không trừ
                if (!laCuonGiuCho) {
                    capNhatSoLuong(em, dauSach.getMaDauSach(), -1, 0);
                }

                ChiTietPhieuMuon ct = new ChiTietPhieuMuon();
                ct.setPhieuMuon(pm);
                ct.setCuonSach(cs);
                ct.setNgayHenTra(ngayHenTra);
                ct.setSoLanGiaHan(0);
                em.persist(ct);

                DongMuon dong = new DongMuon();
                dong.maCTPM = ct.getMaCTPM();
                dong.maCuonSach = ma;
                dong.tenSach = dauSach.getTenSach();
                dong.ngayHenTra = ngayHenTra;
                kq.chiTiet.add(dong);
            }

            tx.commit();

            return kq;

        } catch (NghiepVuException | BadRequestException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new NghiepVuException("Không thể lập phiếu mượn do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // =====================================================================
    // UC-02: XỬ LÝ TRẢ SÁCH (kèm UC-14: phạt trễ hạn / hỏng / mất)
    // =====================================================================

    public static class YeuCauTra {
        public Integer maCTPM;
        public String tinhTrangKhiTra;
        public String viPham; // null | DAMAGED | LOST
        public Double soTienPhat; // null: dùng giá bìa
        public String ghiChu;
    }

    public static class PhieuPhatTao {
        public Integer maPhieuPhat;
        public String lyDo;
        public double soTienPhat;
    }

    public static class KetQuaTra {
        public Integer maCTPM;
        public String maCuonSach;
        public String tenSach;
        public long soNgayTre;
        public String trangThaiCuonSach;
        public List<PhieuPhatTao> phieuPhat = new ArrayList<>();
        public Integer datTruocDuocThongBao;
    }

    private static void taoPhieuPhat(
            EntityManager em,
            KetQuaTra kq,
            ChiTietPhieuMuon ct,
            CuonSach cs,
            NguoiDung docGia,
            NguoiDung thuThu,
            LocalDateTime now,
            String lyDo,
            double soTien) {
        PhieuPhat pp = new PhieuPhat();
        pp.setChiTietPhieuMuon(ct);
        pp.setCuonSach(cs);
        pp.setNguoiDung(docGia);
        pp.setThuThu(thuThu);
        pp.setNgayLapPhieu(now);
        pp.setLyDo(cat(lyDo, 200));
        pp.setSoTienPhat(soTien);
        pp.setTrangThaiThanhToan(false);
        em.persist(pp);

        PhieuPhatTao t = new PhieuPhatTao();
        t.maPhieuPhat = pp.getMaPhieuPhat();
        t.lyDo = pp.getLyDo();
        t.soTienPhat = soTien;
        kq.phieuPhat.add(t);
    }

    // Lượt đặt trước (RESERVED) đang giữ cuốn này
    private static DatTruoc timDatTruocGiuCuon(EntityManager em, String maCuonSach) {
        List<DatTruoc> ds = em.createQuery(
                "SELECT d FROM DatTruoc d "
                        + "WHERE d.cuonSach.maCuonSach = :ma AND d.trangThai = :tt "
                        + "ORDER BY d.ngayDat ASC",
                DatTruoc.class)
                .setParameter("ma", maCuonSach)
                .setParameter("tt", TrangThai.DATTRUOC_RESERVED)
                .setMaxResults(1)
                .getResultList();

        return ds.isEmpty() ? null : ds.get(0);
    }

    // Cuốn đang giữ chỗ bị mất / hỏng: lượt đặt quay về hàng chờ
    private static void huyGiuCho(EntityManager em, String maCuonSach) {
        DatTruoc dt = timDatTruocGiuCuon(em, maCuonSach);

        if (dt != null) {
            dt.setTrangThai(TrangThai.DATTRUOC_WAITING);
            dt.setCuonSach(null);
        }
    }

    // Cuốn giữ chỗ đã về quầy: RESERVED -> NOTIFIED. Trả về mã đặt trước, null nếu
    // không có
    private static Integer thongBaoDatTruoc(
            EntityManager em,
            String maCuonSach,
            LocalDateTime now) {
        DatTruoc dt = timDatTruocGiuCuon(em, maCuonSach);

        if (dt == null) {
            return null;
        }

        dt.setTrangThai(TrangThai.DATTRUOC_NOTIFIED);
        dt.setNgayThongBao(now);

        return dt.getMaDatTruoc();
    }

    // Trả một hoặc nhiều cuốn trong 1 transaction ("tất cả hoặc không")
    public List<KetQuaTra> traSach(
            Integer maThuThu,
            List<YeuCauTra> danhSach,
            double tienPhatNgay) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            NguoiDung thuThu = em.find(NguoiDung.class, maThuThu);

            List<KetQuaTra> ketQua = new ArrayList<>();
            Map<Integer, PhieuMuon> phieuMuonLienQuan = new LinkedHashMap<>();

            for (YeuCauTra yc : danhSach) {

                ChiTietPhieuMuon ct = em.find(ChiTietPhieuMuon.class, yc.maCTPM);

                if (ct == null || ct.getNgayTraThucTe() != null) {
                    throw new NghiepVuException(
                            "Chi tiết phiếu mượn không tồn tại hoặc đã được trả trước đó: "
                                    + yc.maCTPM);
                }

                CuonSach cs = em.find(
                        CuonSach.class,
                        ct.getCuonSach().getMaCuonSach(),
                        LockModeType.PESSIMISTIC_WRITE);

                PhieuMuon pm = ct.getPhieuMuon();
                DauSach dauSach = cs.getDauSach();
                NguoiDung docGia = pm.getTheDocGia().getNguoiDung();

                LocalDateTime now = LocalDateTime.now();
                LocalDate homNay = now.toLocalDate();

                boolean laMat = TrangThai.VI_PHAM_LOST.equals(yc.viPham);
                boolean laHong = TrangThai.VI_PHAM_DAMAGED.equals(yc.viPham);
                boolean giuCho = TrangThai.CUON_RESERVED.equals(cs.getTrangThai());

                ct.setNgayTraThucTe(now);
                ct.setTrangThaiSachKhiTra(cat(laMat ? "Mất" : yc.tinhTrangKhiTra, 50));

                phieuMuonLienQuan.put(pm.getMaPhieuMuon(), pm);

                KetQuaTra kq = new KetQuaTra();
                kq.maCTPM = ct.getMaCTPM();
                kq.maCuonSach = cs.getMaCuonSach();
                kq.tenSach = dauSach.getTenSach();

                // 1. Phạt trễ hạn (so với hạn trả của từng cuốn)
                LocalDate hanTra = ct.getNgayHenTra() != null
                        ? ct.getNgayHenTra()
                        : pm.getNgayHenTra();

                if (hanTra != null && homNay.isAfter(hanTra)) {
                    long soNgayTre = ChronoUnit.DAYS.between(hanTra, homNay);
                    kq.soNgayTre = soNgayTre;

                    taoPhieuPhat(
                            em, kq, ct, cs, docGia, thuThu, now,
                            "Trễ hạn " + soNgayTre + " ngày",
                            soNgayTre * tienPhatNgay);
                }

                // 2. Phạt hỏng / mất
                if (laMat || laHong) {
                    double tien = yc.soTienPhat != null
                            ? yc.soTienPhat
                            : (dauSach.getGiaTien() != null ? dauSach.getGiaTien() : 0.0);

                    if (tien < 0) {
                        throw new BadRequestException(
                                "Số tiền phạt phải lớn hơn hoặc bằng 0");
                    }

                    String lyDo = (laMat ? "Mất sách" : "Hỏng sách")
                            + (yc.ghiChu != null && !yc.ghiChu.trim().isEmpty()
                                    ? ": " + yc.ghiChu.trim()
                                    : "");

                    taoPhieuPhat(em, kq, ct, cs, docGia, thuThu, now, lyDo, tien);
                }

                // 3. Trạng thái cuốn sách, bộ đếm và lượt đặt trước
                if (laMat) {
                    cs.setTrangThai(TrangThai.CUON_LOST);
                    cs.setTinhTrangVatLy("Mất");
                    capNhatSoLuong(em, dauSach.getMaDauSach(), 0, -1);

                    if (giuCho) {
                        huyGiuCho(em, cs.getMaCuonSach());
                    }
                    kq.trangThaiCuonSach = TrangThai.CUON_LOST;

                } else if (laHong) {
                    cs.setTrangThai(TrangThai.CUON_DAMAGED);
                    cs.setTinhTrangVatLy(cat(yc.tinhTrangKhiTra, 100));

                    if (giuCho) {
                        huyGiuCho(em, cs.getMaCuonSach());
                    }
                    kq.trangThaiCuonSach = TrangThai.CUON_DAMAGED;

                } else {
                    cs.setTinhTrangVatLy(cat(yc.tinhTrangKhiTra, 100));

                    Integer maDatTruoc = giuCho
                            ? thongBaoDatTruoc(em, cs.getMaCuonSach(), now)
                            : null;

                    if (maDatTruoc != null) {
                        // Giữ nguyên RESERVED, cất ở quầy chờ người đặt đến nhận
                        kq.trangThaiCuonSach = TrangThai.CUON_RESERVED;
                        kq.datTruocDuocThongBao = maDatTruoc;
                    } else {
                        cs.setTrangThai(TrangThai.CUON_AVAILABLE);
                        capNhatSoLuong(em, dauSach.getMaDauSach(), 1, 0);
                        kq.trangThaiCuonSach = TrangThai.CUON_AVAILABLE;
                    }
                }

                ketQua.add(kq);
            }

            // 4. Phiếu mượn nào đã trả hết thì đóng lại
            for (PhieuMuon pm : phieuMuonLienQuan.values()) {
                Long conLai = em.createQuery(
                        "SELECT COUNT(c) FROM ChiTietPhieuMuon c "
                                + "WHERE c.phieuMuon.maPhieuMuon = :id "
                                + "AND c.ngayTraThucTe IS NULL",
                        Long.class)
                        .setParameter("id", pm.getMaPhieuMuon())
                        .getSingleResult();

                if (conLai == 0) {
                    pm.setTrangThai(TrangThai.PHIEUMUON_RETURNED);
                }
            }

            tx.commit();

            return ketQua;

        } catch (NghiepVuException | BadRequestException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new NghiepVuException("Không thể xử lý trả sách do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }

    // =====================================================================
    // UC-09: GIA HẠN
    // =====================================================================

    public static class KetQuaGiaHan {
        public Integer maCTPM;
        public String maCuonSach;
        public String tenSach;
        public LocalDate ngayHenTraCu;
        public LocalDate ngayHenTraMoi;
        public int soLanGiaHan;
    }

    // maTheChu != null: độc giả chỉ được gia hạn cuốn của chính mình (null = thủ
    // thư)
    public KetQuaGiaHan giaHan(
            Integer maCTPM,
            Integer maTheChu,
            int soNgayMuonToiDa,
            int soLanGiaHanToiDa) {
        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            ChiTietPhieuMuon ct = em.find(
                    ChiTietPhieuMuon.class,
                    maCTPM,
                    LockModeType.PESSIMISTIC_WRITE);

            if (ct == null || ct.getNgayTraThucTe() != null) {
                throw new NghiepVuException(
                        "Chi tiết phiếu mượn không tồn tại hoặc sách đã được trả");
            }

            PhieuMuon pm = ct.getPhieuMuon();
            CuonSach cs = ct.getCuonSach();
            DauSach dauSach = cs.getDauSach();

            if (maTheChu != null
                    && !maTheChu.equals(pm.getTheDocGia().getMaThe())) {

                throw new NghiepVuException(
                        "FORBIDDEN",
                        "Bạn không có quyền gia hạn cuốn sách này");
            }

            LocalDate hanCu = ct.getNgayHenTra() != null
                    ? ct.getNgayHenTra()
                    : pm.getNgayHenTra();

            // 3a. Đã quá hạn
            if (hanCu != null && LocalDate.now().isAfter(hanCu)) {
                throw new NghiepVuException(
                        "Sách đã quá hạn. Vui lòng mang sách đến quầy thủ thư để xử lý");
            }

            // 3b. Hết số lần gia hạn
            int daGiaHan = ct.getSoLanGiaHan() != null ? ct.getSoLanGiaHan() : 0;

            if (daGiaHan >= soLanGiaHanToiDa) {
                throw new NghiepVuException("Sách đã đạt số lần gia hạn tối đa");
            }

            // 3c. Có người đặt trước
            Long nguoiCho = em.createQuery(
                    "SELECT COUNT(d) FROM DatTruoc d "
                            + "WHERE d.dauSach.maDauSach = :maDS "
                            + "AND d.trangThai IN (:tts)",
                    Long.class)
                    .setParameter("maDS", dauSach.getMaDauSach())
                    .setParameter(
                            "tts",
                            List.of(
                                    TrangThai.DATTRUOC_WAITING,
                                    TrangThai.DATTRUOC_RESERVED))
                    .getSingleResult();

            if (nguoiCho > 0
                    || TrangThai.CUON_RESERVED.equals(cs.getTrangThai())) {

                throw new NghiepVuException(
                        "Sách đã được độc giả khác đặt trước, không thể gia hạn thêm");
            }

            LocalDate hanMoi = (hanCu != null ? hanCu : LocalDate.now())
                    .plusDays(soNgayMuonToiDa);

            ct.setNgayHenTra(hanMoi);
            ct.setSoLanGiaHan(daGiaHan + 1);

            // PhieuMuon.NgayHenTra = hạn muộn nhất trong các cuốn của phiếu
            LocalDate hanMuonNhat = em.createQuery(
                    "SELECT MAX(c.ngayHenTra) FROM ChiTietPhieuMuon c "
                            + "WHERE c.phieuMuon.maPhieuMuon = :id",
                    LocalDate.class)
                    .setParameter("id", pm.getMaPhieuMuon())
                    .getSingleResult();

            if (hanMuonNhat != null) {
                pm.setNgayHenTra(hanMuonNhat);
            }

            KetQuaGiaHan kq = new KetQuaGiaHan();
            kq.maCTPM = ct.getMaCTPM();
            kq.maCuonSach = cs.getMaCuonSach();
            kq.tenSach = dauSach.getTenSach();
            kq.ngayHenTraCu = hanCu;
            kq.ngayHenTraMoi = hanMoi;
            kq.soLanGiaHan = daGiaHan + 1;

            tx.commit();

            return kq;

        } catch (NghiepVuException | BadRequestException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new NghiepVuException("Không thể gia hạn do lỗi hệ thống!");

        } finally {
            em.close();
        }
    }
}
