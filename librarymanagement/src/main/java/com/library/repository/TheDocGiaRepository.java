package com.library.repository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.library.common.BadRequestException;
import com.library.common.TrangThai;
import com.library.entities.TheDocGia;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.TypedQuery;
public class TheDocGiaRepository {

    private final PhieuMuonRepository phieuMuonRepo = new PhieuMuonRepository();
    private final PhieuPhatRepository phieuPhatRepo = new PhieuPhatRepository();
    private final CauHinhRepository cauHinhRepo = new CauHinhRepository();

    // ===== Kết quả UC-13 =====
    public static class KetQuaKiemTra {
        public static final String CARD_LOCKED = "CARD_LOCKED";
        public static final String CARD_EXPIRED = "CARD_EXPIRED";
        public static final String HAS_UNPAID_FINE = "HAS_UNPAID_FINE";
        public static final String HAS_OVERDUE_BOOK = "HAS_OVERDUE_BOOK";
        public static final String BORROW_LIMIT_REACHED = "BORROW_LIMIT_REACHED";

        public boolean dat;
        public String reasonCode;   // null khi đủ điều kiện
        public String message;
        public TheDocGia the;
        public long dangMuon;
        public int soSachToiDa;
    }

    // Trạng thái hiển thị: ACTIVE | LOCKED | EXPIRED | NONE (EXPIRED tính từ NgayHetHan)
    public static String trangThaiHienThi(TheDocGia the) {
        if (the == null) {
            return TrangThai.THE_NONE;
        }
        if (TrangThai.THE_LOCKED.equals(the.getTrangThai())) {
            return TrangThai.THE_LOCKED;
        }
        LocalDate hetHan = the.getNgayHetHan();
        if (hetHan != null && hetHan.isBefore(LocalDate.now())) {
            return TrangThai.THE_EXPIRED;
        }
        return TrangThai.THE_ACTIVE;
    }

    public TheDocGia findById(Integer maThe) {
        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            List<TheDocGia> ds = em.createQuery(
                    "SELECT t FROM TheDocGia t "
                            + "LEFT JOIN FETCH t.nguoiDung "
                            + "WHERE t.maThe = :id",
                    TheDocGia.class
            )
                    .setParameter("id", maThe)
                    .getResultList();

            return ds.isEmpty() ? null : ds.get(0);
        }
    }

    // UC-13. Trả về null nếu không có thẻ.
    // Thứ tự: khóa -> hết hạn -> nợ phạt -> sách quá hạn -> hạn mức
    public KetQuaKiemTra kiemTraDieuKien(Integer maThe) {
        TheDocGia the = findById(maThe);

        if (the == null) {
            return null;
        }

        KetQuaKiemTra kq = new KetQuaKiemTra();
        kq.the = the;
        kq.dangMuon = phieuMuonRepo.countActiveBorrowingByMaThe(maThe);
        kq.soSachToiDa = cauHinhRepo.getCauHinh().getSoSachToiDa();

        String tt = trangThaiHienThi(the);

        if (TrangThai.THE_LOCKED.equals(tt)) {
            return tuChoi(kq, KetQuaKiemTra.CARD_LOCKED, "Thẻ đang bị khóa");
        }
        if (TrangThai.THE_EXPIRED.equals(tt)) {
            return tuChoi(kq, KetQuaKiemTra.CARD_EXPIRED, "Thẻ hết hạn");
        }
        if (phieuPhatRepo.countChuaThanhToanByMaThe(maThe) > 0) {
            return tuChoi(kq, KetQuaKiemTra.HAS_UNPAID_FINE,
                    "Đang nợ tiền phạt chưa nộp");
        }
        if (phieuMuonRepo.countOverdueByMaThe(maThe) > 0) {
            return tuChoi(kq, KetQuaKiemTra.HAS_OVERDUE_BOOK,
                    "Đang có sách quá hạn chưa trả");
        }
        if (kq.dangMuon >= kq.soSachToiDa) {
            return tuChoi(kq, KetQuaKiemTra.BORROW_LIMIT_REACHED,
                    "Đã mượn đủ số lượng tối đa");
        }

        kq.dat = true;
        kq.message = "Thẻ đủ điều kiện mượn sách";
        return kq;
    }

    private static KetQuaKiemTra tuChoi(
            KetQuaKiemTra kq,
            String code,
            String message
    ) {
        kq.dat = false;
        kq.reasonCode = code;
        kq.message = message;
        return kq;
    }

    // ===== Danh sách độc giả (GET_READERS) =====

    private static String buildWhere(
            String keyword,
            String trangThaiThe,
            Map<String, Object> p
    ) {
        StringBuilder w = new StringBuilder(" WHERE 1=1");

        if (keyword != null && !keyword.trim().isEmpty()) {
            w.append(" AND (LOWER(n.hoTen) LIKE :kw"
                    + " OR LOWER(n.email) LIKE :kw"
                    + " OR n.soDienThoai LIKE :kw)");
            p.put("kw", "%" + keyword.trim().toLowerCase() + "%");
        }

        if (trangThaiThe != null && !trangThaiThe.trim().isEmpty()) {
            switch (trangThaiThe) {
                case TrangThai.THE_LOCKED:
                    w.append(" AND t.trangThai = :locked");
                    p.put("locked", TrangThai.THE_LOCKED);
                    break;

                case TrangThai.THE_EXPIRED:
                    w.append(" AND (t.trangThai IS NULL OR t.trangThai <> :locked)"
                            + " AND t.ngayHetHan < :today");
                    p.put("locked", TrangThai.THE_LOCKED);
                    p.put("today", LocalDate.now());
                    break;

                case TrangThai.THE_ACTIVE:
                    w.append(" AND (t.trangThai IS NULL OR t.trangThai <> :locked)"
                            + " AND (t.ngayHetHan IS NULL OR t.ngayHetHan >= :today)");
                    p.put("locked", TrangThai.THE_LOCKED);
                    p.put("today", LocalDate.now());
                    break;

                default:
                    throw new BadRequestException(
                            "Thiếu hoặc sai tham số: trangThaiThe");
            }
        }

        return w.toString();
    }

    private static void bind(TypedQuery<?> q, Map<String, Object> params) {
        for (Map.Entry<String, Object> e : params.entrySet()) {
            q.setParameter(e.getKey(), e.getValue());
        }
    }

    public List<TheDocGia> search(
            String keyword,
            String trangThaiThe,
            int page,
            int pageSize
    ) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhere(keyword, trangThaiThe, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<TheDocGia> query = em.createQuery(
                    "SELECT t FROM TheDocGia t JOIN FETCH t.nguoiDung n"
                            + where
                            + " ORDER BY t.maThe",
                    TheDocGia.class
            );

            bind(query, params);

            query.setFirstResult((page - 1) * pageSize);
            query.setMaxResults(pageSize);

            return query.getResultList();
        }
    }

    public long count(String keyword, String trangThaiThe) {
        Map<String, Object> params = new HashMap<>();
        String where = buildWhere(keyword, trangThaiThe, params);

        try (EntityManager em = EntityManagerUtil.getEntityManager()) {

            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(t) FROM TheDocGia t JOIN t.nguoiDung n" + where,
                    Long.class
            );

            bind(query, params);

            return query.getSingleResult();
        }
    }

    // ===== Gia hạn / khóa / mở khóa thẻ (UPDATE_CARD) =====
    // Trả về thẻ sau khi sửa, hoặc null nếu không tìm thấy
    public TheDocGia capNhatThe(
            Integer maThe,
            LocalDate ngayHetHanMoi,
            String trangThaiMoi
    ) {
        if (trangThaiMoi != null
                && !TrangThai.THE_ACTIVE.equals(trangThaiMoi)
                && !TrangThai.THE_LOCKED.equals(trangThaiMoi)) {

            throw new BadRequestException("Thiếu hoặc sai tham số: trangThai");
        }

        EntityManager em = EntityManagerUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            TheDocGia the = em.find(TheDocGia.class, maThe);

            if (the == null) {
                tx.rollback();
                return null;
            }

            if (ngayHetHanMoi != null) {
                if (the.getNgayCap() != null
                        && !ngayHetHanMoi.isAfter(the.getNgayCap())) {

                    throw new BadRequestException(
                            "Ngày hết hạn phải sau ngày cấp");
                }
                the.setNgayHetHan(ngayHetHanMoi);
            }

            if (trangThaiMoi != null) {
                the.setTrangThai(trangThaiMoi);
            }

            tx.commit();

            return the;

        } catch (BadRequestException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            throw e;

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
            throw new RuntimeException("Lỗi cập nhật thẻ độc giả", e);

        } finally {
            em.close();
        }
    }
}
